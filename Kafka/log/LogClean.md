### CleanerConfig 清理Log的配置
```scala
case class CleanerConfig(numThreads: Int = 1,                           // 用于日志清理的线程数
                         dedupeBufferSize: Long = 4*1024*1024L,         // 用于日志重复数据删除的内存
                         dedupeBufferLoadFactor: Double = 0.9d,         // 用于日志重复数据删除的负载因子
                         ioBufferSize: Int = 1024*1024,                 // I/O Buffer大小
                         maxMessageSize: Int = 32*1024*1024,            // 最大消息大小
                         maxIoBytesPerSecond: Double = Double.MaxValue, // 允许所有clean线程执行的最大读写I/O
                         backOffMs: Long = 15 * 1000,                   // 等待时间ms
                         enableCleaner: Boolean = true,                 // 是否启用cleaner
                         hashAlgorithm: String = "MD5") {               // key的hash算法
}
```

### LogCleaningState 日志清理过程的状态
```scala
private[log] sealed trait LogCleaningState
private[log] case object LogCleaningInProgress extends LogCleaningState
private[log] case object LogCleaningAborted extends LogCleaningState
private[log] case class LogCleaningPaused(pausedCount: Int) extends LogCleaningState
```

### LogToClean 需要被清理的Log
```scala
private case class LogToClean(topicPartition: TopicPartition,  // 主题分区
                              log: Log,
                              firstDirtyOffset: Long,          // 本次清理的起始点，此前被清理，此后做key合并
                              uncleanableOffset: Long,  
                              needCompactionNow: Boolean = false) extends Ordered[LogToClean] {
  val cleanBytes = log.logSegments(-1, firstDirtyOffset).map(_.size.toLong).sum
  val (firstUncleanableOffset, cleanableBytes) = LogCleanerManager.calculateCleanableBytes(log, firstDirtyOffset, uncleanableOffset)
  val totalBytes = cleanBytes + cleanableBytes
  val cleanableRatio = cleanableBytes / totalBytes.toDouble
  override def compare(that: LogToClean): Int = math.signum(this.cleanableRatio - that.cleanableRatio).toInt
}
```

### LogCleanerManager管理清理Log的状态和转换
```scala
def abortCleaning(topicAndPartition: TopicAndPartition)
def abortAndPauseCleaning(topicAndPartition: TopicAndPartition)
def resumeCleaning(topicAndPartition: TopicAndPartition)
def checkCleaningAborted(topicAndPartition: TopicAndPartition) 
```

### LogCleanerManager选出最需要清理的日志
```scala
  def grabFilthiestCompactedLog(time: Time, preCleanStats: PreCleanStats = new PreCleanStats()): Option[LogToClean] = {
    inLock(lock) {
      val now = time.milliseconds
      this.timeOfLastRun = now
      val lastClean = allCleanerCheckpoints
      // 找出所有LogToClean对象
      val dirtyLogs = logs.filter {
        case (_, log) => log.config.compact  // match logs that are marked as compacted
      }.filterNot {
        case (topicPartition, log) =>
          // skip any logs already in-progress and uncleanable partitions
          inProgress.contains(topicPartition) || isUncleanablePartition(log, topicPartition)
      }.map {
        case (topicPartition, log) => // create a LogToClean instance for each
          try {
            val lastCleanOffset = lastClean.get(topicPartition)
            val (firstDirtyOffset, firstUncleanableDirtyOffset) = cleanableOffsets(log, lastCleanOffset, now)
            val compactionDelayMs = maxCompactionDelay(log, firstDirtyOffset, now)
            preCleanStats.updateMaxCompactionDelay(compactionDelayMs)

            LogToClean(topicPartition, log, firstDirtyOffset, firstUncleanableDirtyOffset, compactionDelayMs > 0)
          } catch {
            case e: Throwable => throw new LogCleaningException(log,
              s"Failed to calculate log cleaning stats for partition $topicPartition", e)
          }
      }.filter(ltc => ltc.totalBytes > 0) // skip any empty logs

      this.dirtiestLogCleanableRatio = if (dirtyLogs.nonEmpty) dirtyLogs.max.cleanableRatio else 0
      // and must meet the minimum threshold for dirty byte ratio or have some bytes required to be compacted
      val cleanableLogs = dirtyLogs.filter { ltc =>
        (ltc.needCompactionNow && ltc.cleanableBytes > 0) || ltc.cleanableRatio > ltc.log.config.minCleanableRatio
      }
      if(cleanableLogs.isEmpty) {
        None
      } else {
        preCleanStats.recordCleanablePartitions(cleanableLogs.size)
        // 取出cleanableRatio最大的LogToClean，最需要进行清理
        val filthiest = cleanableLogs.max
        inProgress.put(filthiest.topicPartition, LogCleaningInProgress)
        Some(filthiest)
      }
    }
  }
```

### LogCleaner清理日志
```scala
  private[log] def cleanSegments(log: Log,
                                 segments: Seq[LogSegment],
                                 map: OffsetMap,
                                 deleteHorizonMs: Long,
                                 stats: CleanerStats,
                                 transactionMetadata: CleanedTransactionMetadata): Unit = {
    // create a new segment with a suffix appended to the name of the log and indexes
    val cleaned = LogCleaner.createNewCleanedSegment(log, segments.head.baseOffset)
    transactionMetadata.cleanedIndex = Some(cleaned.txnIndex)

    try {
      // clean segments into the new destination segment
      val iter = segments.iterator
      var currentSegmentOpt: Option[LogSegment] = Some(iter.next())
      val lastOffsetOfActiveProducers = log.lastRecordsOfActiveProducers

      while (currentSegmentOpt.isDefined) {
        val currentSegment = currentSegmentOpt.get
        val nextSegmentOpt = if (iter.hasNext) Some(iter.next()) else None
        // 获取清理的起始偏移 & 结束偏移
        val startOffset = currentSegment.baseOffset
        val upperBoundOffset = nextSegmentOpt.map(_.baseOffset).getOrElse(map.latestOffset + 1)
        val abortedTransactions = log.collectAbortedTransactions(startOffset, upperBoundOffset)
        transactionMetadata.addAbortedTransactions(abortedTransactions)

        val retainDeletesAndTxnMarkers = currentSegment.lastModified > deleteHorizonMs
        info(s"Cleaning $currentSegment in log ${log.name} into ${cleaned.baseOffset} " +
          s"with deletion horizon $deleteHorizonMs, " +
          s"${if(retainDeletesAndTxnMarkers) "retaining" else "discarding"} deletes.")

        try {
          // 清理当前段，并拷贝到目的段
          cleanInto(log.topicPartition, currentSegment.log, cleaned, map, retainDeletesAndTxnMarkers, log.config.maxMessageSize,
            transactionMetadata, lastOffsetOfActiveProducers, stats)
        } catch {
          case e: LogSegmentOffsetOverflowException =>
            // Split the current segment. It's also safest to abort the current cleaning process, so that we retry from
            // scratch once the split is complete.
            info(s"Caught segment overflow error during cleaning: ${e.getMessage}")
            log.splitOverflowedSegment(currentSegment)
            throw new LogCleaningAbortedException()
        }
        currentSegmentOpt = nextSegmentOpt
      }

      cleaned.onBecomeInactiveSegment()
      // flush new segment to disk before swap
      cleaned.flush()

      // update the modification date to retain the last modified date of the original files
      val modified = segments.last.lastModified
      cleaned.lastModified = modified

      // swap in new segment
      info(s"Swapping in cleaned segment $cleaned for segment(s) $segments in log $log")
      // 用新的段替代旧的段
      log.replaceSegments(List(cleaned), segments)
    } catch {
      case e: LogCleaningAbortedException =>
        try cleaned.deleteIfExists()
        catch {
          case deleteException: Exception =>
            e.addSuppressed(deleteException)
        } finally throw e
    }
  }
```