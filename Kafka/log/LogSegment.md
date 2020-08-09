### LogSegment 日志段

#### Log对象由多个LogSegment组成，而每个LogSegment会在磁盘上创建一组文件
- 包括消息日志文件（.log）、位移索引文件（.index）、时间戳索引文件（.timeindex）以及已中止事务的索引文件（.txnindex）
- 一个主题有很多分区，每个分区对应一个Log对象，在磁盘上对应一个子目录

#### LogSegment
- 日志段主要包括两个组件： log 和 index
    - The log is a **FileRecords** containing the actual messages.
    - The index is an **OffsetIndex** that maps from _logical offsets_ to _physical file positions_. 
- base offset(起始偏移)： any offset in any previous segment < **base offset** <= the least offset of any message in this segment
- A segment with a base offset of [base_offset] would be stored in two files, **a [base_offset].index** and **a [base_offset].log file**

```scala
/**
 * @param log The file records containing log entries
 * @param lazyOffsetIndex The offset index
 * @param lazyTimeIndex The timestamp index
 * @param txnIndex The transaction index
 * @param baseOffset A lower bound on the offsets in this segment (每个文件都是以baseOffset命名)
 * @param indexIntervalBytes The approximate number of bytes between entries in the index
 * @param rollJitterMs The maximum random jitter subtracted from the scheduled segment roll time
 * @param time The time instance
 */
class LogSegment private[log] (val log: FileRecords,
                               val lazyOffsetIndex: LazyIndex[OffsetIndex],
                               val lazyTimeIndex: LazyIndex[TimeIndex],
                               val txnIndex: TransactionIndex,
                               val baseOffset: Long,
                               val indexIntervalBytes: Int,
                               val rollJitterMs: Long,
                               val time: Time) extends Logging {...}
```

#### append方法
- 通过`largestOffset-baseOffset`的相对偏移来判断largestOffset是否合法
- 如果`bytesSinceLastIndexEntry > indexIntervalBytes`，那么需要更新索引文件
```scala
  @nonthreadsafe
  def append(largestOffset: Long, // 消息中最大偏移量
             largestTimestamp: Long, // 消息中最大时间戳
             shallowOffsetOfMaxTimestamp: Long, // 最大时间戳对应的消息偏移量
             records: MemoryRecords): Unit = {
    if (records.sizeInBytes > 0) {
      trace(s"Inserting ${records.sizeInBytes} bytes at end offset $largestOffset at position ${log.sizeInBytes} " +
            s"with largest timestamp $largestTimestamp at shallow offset $shallowOffsetOfMaxTimestamp")
      // 1. 判断日志段是否为空，如果为空需要记录最大时间戳
      val physicalPosition = log.sizeInBytes()
      if (physicalPosition == 0)
        rollingBasedTimestamp = Some(largestTimestamp)
      // 2. 判断最大偏移量是否合法，主要是检查largestOffset-baseOffset的相对偏移是否小于0或大于Int.MaxValue
      ensureOffsetInRange(largestOffset)

      // 3. append the messages
      val appendedBytes = log.append(records)
      trace(s"Appended $appendedBytes to ${log.file} at end offset $largestOffset")
      // 4. Update the in memory max timestamp and corresponding offset.
      if (largestTimestamp > maxTimestampSoFar) {
        maxTimestampSoFar = largestTimestamp
        offsetOfMaxTimestampSoFar = shallowOffsetOfMaxTimestamp
      }
      // 5. append an entry to the index (if needed)
      if (bytesSinceLastIndexEntry > indexIntervalBytes) {
        offsetIndex.append(largestOffset, physicalPosition)
        timeIndex.maybeAppend(maxTimestampSoFar, offsetOfMaxTimestampSoFar)
        bytesSinceLastIndexEntry = 0
      }
      bytesSinceLastIndexEntry += records.sizeInBytes
    }
  }
```


#### read方法
- 通过translateOffset(startOffset)找到物理文件的位置
- 计算真正读取的字节数: `min((maxPosition - startPosition).toInt, adjustedMaxSize)`
```scala
  @threadsafe
  def read(startOffset: Long,
           maxSize: Int,
           maxPosition: Long = size,
           minOneMessage: Boolean = false): FetchDataInfo = {
    if (maxSize < 0)
      throw new IllegalArgumentException(s"Invalid max size $maxSize for log read from segment $log")
    // 1. 通过translateOffset定位到读取的文件起始位置
    val startOffsetAndSize = translateOffset(startOffset)

    // if the start position is already off the end of the log, return null
    if (startOffsetAndSize == null)
      return null

    val startPosition = startOffsetAndSize.position
    val offsetMetadata = LogOffsetMetadata(startOffset, this.baseOffset, startPosition)
    // 2. 计算返回消息的字节大小
    val adjustedMaxSize =
      if (minOneMessage) math.max(maxSize, startOffsetAndSize.size)
      else maxSize

    // return a log segment but with zero size in the case below
    if (adjustedMaxSize == 0)
      return FetchDataInfo(offsetMetadata, MemoryRecords.EMPTY)

    // 3. calculate the length of the message set to read based on whether or not they gave us a maxOffset
    val fetchSize: Int = min((maxPosition - startPosition).toInt, adjustedMaxSize)

    FetchDataInfo(offsetMetadata, log.slice(startPosition, fetchSize),
      firstEntryIncomplete = adjustedMaxSize < startOffsetAndSize.size)
  }
```

### recover方法
- 清空索引文件
- 重新创建索引文件
- 最后，进行文件截断
```scala
  @nonthreadsafe
  def recover(producerStateManager: ProducerStateManager, leaderEpochCache: Option[LeaderEpochFileCache] = None): Int = {
    // 1. 清空索引文件
    offsetIndex.reset()
    timeIndex.reset()
    txnIndex.reset()
    var validBytes = 0
    var lastIndexEntry = 0
    maxTimestampSoFar = RecordBatch.NO_TIMESTAMP
    try {
      for (batch <- log.batches.asScala) {
        batch.ensureValid()
        ensureOffsetInRange(batch.lastOffset)

        // The max timestamp is exposed at the batch level, so no need to iterate the records
        if (batch.maxTimestamp > maxTimestampSoFar) {
          maxTimestampSoFar = batch.maxTimestamp
          offsetOfMaxTimestampSoFar = batch.lastOffset
        }

        // Build offset index
        if (validBytes - lastIndexEntry > indexIntervalBytes) {
          offsetIndex.append(batch.lastOffset, validBytes)
          timeIndex.maybeAppend(maxTimestampSoFar, offsetOfMaxTimestampSoFar)
          lastIndexEntry = validBytes
        }
        validBytes += batch.sizeInBytes()

        if (batch.magic >= RecordBatch.MAGIC_VALUE_V2) {
          leaderEpochCache.foreach { cache =>
            if (batch.partitionLeaderEpoch > 0 && cache.latestEpoch.forall(batch.partitionLeaderEpoch > _))
              cache.assign(batch.partitionLeaderEpoch, batch.baseOffset)
          }
          updateProducerState(producerStateManager, batch)
        }
      }
    } catch {
      case e@ (_: CorruptRecordException | _: InvalidRecordException) =>
        warn("Found invalid messages in log segment %s at byte offset %d: %s. %s"
          .format(log.file.getAbsolutePath, validBytes, e.getMessage, e.getCause))
    }
    // 如果log.sizeInBytes > validBytes，说明写入了一些非法消息，需要截断
    val truncated = log.sizeInBytes - validBytes
    if (truncated > 0)
      debug(s"Truncated $truncated invalid bytes at the end of segment ${log.file.getAbsoluteFile} during recovery")

    log.truncateTo(validBytes)
    offsetIndex.trimToValidSize()
    // A normally closed segment always appends the biggest timestamp ever seen into log segment, we do this as well.
    timeIndex.maybeAppend(maxTimestampSoFar, offsetOfMaxTimestampSoFar, skipFullCheck = true)
    timeIndex.trimToValidSize()
    truncated
  }
```


### log.truncateTo方法
```scala
    public int truncateTo(int targetSize) throws IOException {
        int originalSize = sizeInBytes();
        if (targetSize > originalSize || targetSize < 0)
            throw new KafkaException("Attempt to truncate log segment " + file + " to " + targetSize + " bytes failed, " +
                    " size of this log segment is " + originalSize + " bytes.");
        if (targetSize < (int) channel.size()) {
            channel.truncate(targetSize);
            size.set(targetSize);
        }
        return originalSize - targetSize;
    }
```