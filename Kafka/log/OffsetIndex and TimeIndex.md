### OffsetIndex<OFFSET,LOCATION>
> An index that maps offsets to physical file locations for a particular log segment. 
> 将偏移量映射到特定日志段的物理文件位置的索引。

```scala
  private def relativeOffset(buffer: ByteBuffer, n: Int): Int = buffer.getInt(n * entrySize)

  private def physical(buffer: ByteBuffer, n: Int): Int = buffer.getInt(n * entrySize + 4)
  
  override protected def parseEntry(buffer: ByteBuffer, n: Int): OffsetPosition = {
    OffsetPosition(baseOffset + relativeOffset(buffer, n), physical(buffer, n))
  }
```
- 追加offset和location到索引文件
```scala
  def append(offset: Long, position: Int): Unit = {
    inLock(lock) {
      require(!isFull, "Attempt to append to a full index (size = " + _entries + ").")
      if (_entries == 0 || offset > _lastOffset) {
        trace(s"Adding index entry $offset => $position to ${file.getAbsolutePath}")
        // 插入相对offset
        mmap.putInt(relativeOffset(offset))
        // 插入物理位置
        mmap.putInt(position)
        _entries += 1
        _lastOffset = offset
        require(_entries * entrySize == mmap.position(), entries + " entries but file position in index is " + mmap.position() + ".")
      } else {
        throw new InvalidOffsetException(s"Attempt to append an offset ($offset) to position $entries no larger than" +
          s" the last offset appended (${_lastOffset}) to ${file.getAbsolutePath}.")
      }
    }
  }
```
- 查找targetOffset对应的物理文件位置
```scala
  def lookup(targetOffset: Long): OffsetPosition = {
    maybeLock(lock) {
      val idx = mmap.duplicate
      val slot = largestLowerBoundSlotFor(idx, targetOffset, IndexSearchType.KEY)
      if(slot == -1)
        OffsetPosition(baseOffset, 0)
      else
        parseEntry(idx, slot)
    }
  }
```


### TimeIndex<TIMESTAMP, OFFSET>
> An index that maps from the timestamp to the logical offsets of the messages in a segment.
> 从时间戳到段中消息的逻辑偏移量的索引。
```scala
  private def timestamp(buffer: ByteBuffer, n: Int): Long = buffer.getLong(n * entrySize)

  private def relativeOffset(buffer: ByteBuffer, n: Int): Int = buffer.getInt(n * entrySize + 8)

  override def parseEntry(buffer: ByteBuffer, n: Int): TimestampOffset = {
    TimestampOffset(timestamp(buffer, n), baseOffset + relativeOffset(buffer, n))
  }
```
- 追加时间索引
    - 只有时间戳和offset都大于最后一个记录的时间戳和offset才可以追加
```scala
  def maybeAppend(timestamp: Long, offset: Long, skipFullCheck: Boolean = false): Unit = {
    inLock(lock) {
      if (!skipFullCheck)
        require(!isFull, "Attempt to append to a full time index (size = " + _entries + ").")
      // We do not throw exception when the offset equals to the offset of last entry. That means we are trying
      // to insert the same time index entry as the last entry.
      // If the timestamp index entry to be inserted is the same as the last entry, we simply ignore the insertion
      // because that could happen in the following two scenarios:
      // 1. A log segment is closed.
      // 2. LogSegment.onBecomeInactiveSegment() is called when an active log segment is rolled.
      if (_entries != 0 && offset < lastEntry.offset)
        throw new InvalidOffsetException(s"Attempt to append an offset ($offset) to slot ${_entries} no larger than" +
          s" the last offset appended (${lastEntry.offset}) to ${file.getAbsolutePath}.")
      if (_entries != 0 && timestamp < lastEntry.timestamp)
        throw new IllegalStateException(s"Attempt to append a timestamp ($timestamp) to slot ${_entries} no larger" +
          s" than the last timestamp appended (${lastEntry.timestamp}) to ${file.getAbsolutePath}.")
      // We only append to the time index when the timestamp is greater than the last inserted timestamp.
      // If all the messages are in message format v0, the timestamp will always be NoTimestamp. In that case, the time
      // index will be empty.
      if (timestamp > lastEntry.timestamp) {
        trace(s"Adding index entry $timestamp => $offset to ${file.getAbsolutePath}.")
        // 插入时间戳
        mmap.putLong(timestamp)
        // 插入相对offset
        mmap.putInt(relativeOffset(offset))
        _entries += 1
        _lastEntry = TimestampOffset(timestamp, offset)
        require(_entries * entrySize == mmap.position(), _entries + " entries but file position in index is " + mmap.position() + ".")
      }
    }
  }
```
- 查找targetTimestamp对应的物理文件位置
```scala
  def lookup(targetTimestamp: Long): TimestampOffset = {
    maybeLock(lock) {
      val idx = mmap.duplicate
      val slot = largestLowerBoundSlotFor(idx, targetTimestamp, IndexSearchType.KEY)
      if (slot == -1)
        TimestampOffset(RecordBatch.NO_TIMESTAMP, baseOffset)
      else
        parseEntry(idx, slot)
    }
  }
```