### Log日志
- case class LogOffsetSnapshot：封装分区所有位移元数据的容器类
- case class LogReadInfo：封装读取日志返回的数据以及元数据
- case class CompletedTxn：记录已完成事务的元数据，用于构建事务索引
- object LogMetricNames：监控指标

- object LogAppendInfo：定义了一些工厂方法，用于创建特定的LogAppendInfo实例
- case class LogAppendInfo：保存了一组待写入消息的元数据信息
- case class RollParams：定义用于控制logSegment是否Roll切分的数据结构
- object RollParams：定义了一些工厂方法
- class Log： 日志是由一系列的有base offset的LogSegments组成
- object Log：日志的辅助变量和方法

#### class Log
- logStartOffset变量
    - 什么时候被更新：用户删除消息、broker的日志保留、broker的日志截断
    - 影响什么操作：
        - 删除日志，只能删除nextOffset <= log's logStartOffset的LogSegment
        - ListOffsetRequest响应日志最早的offset，保证 logStartOffset <= log's highWatermark
    - **Log End Offset(LEO)**，它是表示日志下一条待插入消息的位移值
    - **Log Start Offset** ，它表示日志当前对外可见的最早一条消息的位移值  
        
```scala
@threadsafe
class Log(@volatile var dir: File, 
          @volatile var config: LogConfig,
          @volatile var logStartOffset: Long,
          @volatile var recoveryPoint: Long,
          scheduler: Scheduler,
          brokerTopicStats: BrokerTopicStats,
          val time: Time,
          val maxProducerIdExpirationMs: Int,
          val producerIdExpirationCheckIntervalMs: Int,
          val topicPartition: TopicPartition,
          val producerStateManager: ProducerStateManager,
          logDirFailureChannel: LogDirFailureChannel) extends Logging with KafkaMetricsGroup{}
```
- 初始化Log对象
```scala
  locally {
    val startMs = time.milliseconds

    // 1. 创建日志目录
    Files.createDirectories(dir.toPath)
    // 2. 初始化LeaderEpochCache(new LeaderEpochCheckpointFile && new LeaderEpochFileCache)
    initializeLeaderEpochCache()
    
    // 3. 加载segments(对分区日志路径遍历两次！！)
    val nextOffset = loadSegments()

    // 计算下一条消息的offset
    nextOffsetMetadata = LogOffsetMetadata(nextOffset, activeSegment.baseOffset, activeSegment.size)

    leaderEpochCache.foreach(_.truncateFromEnd(nextOffsetMetadata.messageOffset))
    
    // 4. 更新LogStartOffset
    updateLogStartOffset(math.max(logStartOffset, segments.firstEntry.getValue.baseOffset))

    // The earliest leader epoch may not be flushed during a hard failure. Recover it here.
    leaderEpochCache.foreach(_.truncateFromStart(logStartOffset))

    // Any segment loading or recovery code must not use producerStateManager, so that we can build the full state here from scratch.
    if (!producerStateManager.isEmpty)
      throw new IllegalStateException("Producer state must be empty during log initialization")
    loadProducerState(logEndOffset, reloadFromCleanShutdown = hasCleanShutdownFile)

    info(s"Completed load of log with ${segments.size} segments, log start offset $logStartOffset and " +
      s"log end offset $logEndOffset in ${time.milliseconds() - startMs} ms")
  }
```
- loadSegments
    - We create swap files for two cases:
        - (1) Log cleaning where multiple segments are merged into one
        - (2) Log splitting where one segment is split into multiple.
```scala
  private def loadSegments(): Long = {
    // 1. 移除各种临时文件(.swap .cleaned .deleted)和.swap文件相关的索引文件
    val swapFiles = removeTempFilesAndCollectSwapFiles()

    // 2. 重新加载索引和日志文件
    retryOnOffsetOverflow {
      logSegments.foreach(_.close())
      segments.clear()
      loadSegmentFiles()
    }

    // 3. 完成swap操作(.swap renamed to .deleted before .swap restored as the new segment file) 
    completeSwapOperations(swapFiles)

    if (!dir.getAbsolutePath.endsWith(Log.DeleteDirSuffix)) {
      val nextOffset = retryOnOffsetOverflow {
        // 4. recoverLog
        recoverLog()
      }

      // reset the index size of the currently active log segment to allow more entries
      activeSegment.resizeIndexes(config.maxIndexSize)
      nextOffset
    } else {
       if (logSegments.isEmpty) {
          addSegment(LogSegment.open(dir = dir,
            baseOffset = 0,
            config,
            time = time,
            fileAlreadyExists = false,
            initFileSize = this.initFileSize,
            preallocate = false))
       }
      0
    }
  }
```

#### object Log  
```scala
object Log {
  val LogFileSuffix = ".log"
  val IndexFileSuffix = ".index"
  val TimeIndexFileSuffix = ".timeindex"
  val ProducerSnapshotFileSuffix = ".snapshot" // kafka为幂等型或事务型Producer所做的快照文件
  /** an (aborted) txn index */
  val TxnIndexFileSuffix = ".txnindex"
  /** a file that is scheduled to be deleted */
  val DeletedFileSuffix = ".deleted"
  /** A temporary file that is being used for log cleaning */
  val CleanedFileSuffix = ".cleaned"
  /** A temporary file used when swapping files into the log */
  val SwapFileSuffix = ".swap"
  
  /** Clean shutdown file that indicates the broker was cleanly shutdown in 0.8 and higher. */
  val CleanShutdownFile = ".kafka_cleanshutdown"

  /** a directory that is scheduled to be deleted */
  val DeleteDirSuffix = "-delete"
  /** a directory that is used for future partition */
  val FutureDirSuffix = "-future"
}
```