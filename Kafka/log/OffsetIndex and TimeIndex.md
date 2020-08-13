### OffsetIndex<OFFSET,LOCATION>
> An index that maps offsets to physical file locations for a particular log segment. 

```scala
  // 相对位移Int:4个字节 + 物理文件位置Int:4个字节  
  override def entrySize = 8

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
      // 索引文件是否已经写满
      require(!isFull, "Attempt to append to a full index (size = " + _entries + ").")
      // offset需要比当前索引都要大，为了维护索引的单调递增
      if (_entries == 0 || offset > _lastOffset) {
        trace(s"Adding index entry $offset => $position to ${file.getAbsolutePath}")
        mmap.putInt(relativeOffset(offset)) // 插入相对offset
        mmap.putInt(position) // 插入物理位置
        _entries += 1 // 更新索引个数
        _lastOffset = offset // 更新最大位移值
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
      // 私有变量复制整个索引映射区
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

```scala
  // 时间戳Long:8个字节 + 相对位移Int:4个字节  
  override def entrySize = 12

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
        // 索引文件是否已经写满
        require(!isFull, "Attempt to append to a full time index (size = " + _entries + ").")
      // We do not throw exception when the offset equals to the offset of last entry. That means we are trying
      // to insert the same time index entry as the last entry.
      // If the timestamp index entry to be inserted is the same as the last entry, we simply ignore the insertion
      // because that could happen in the following two scenarios:
      // 1. A log segment is closed.
      // 2. LogSegment.onBecomeInactiveSegment() is called when an active log segment is rolled.
      // 索引需要单调递增
      if (_entries != 0 && offset < lastEntry.offset)
        throw new InvalidOffsetException(s"Attempt to append an offset ($offset) to slot ${_entries} no larger than" +
          s" the last offset appended (${lastEntry.offset}) to ${file.getAbsolutePath}.")
      // 时间戳需要单调递增
      if (_entries != 0 && timestamp < lastEntry.timestamp)
        throw new IllegalStateException(s"Attempt to append a timestamp ($timestamp) to slot ${_entries} no larger" +
          s" than the last timestamp appended (${lastEntry.timestamp}) to ${file.getAbsolutePath}.")
      // We only append to the time index when the timestamp is greater than the last inserted timestamp.
      // If all the messages are in message format v0, the timestamp will always be NoTimestamp. In that case, the time
      // index will be empty.
      if (timestamp > lastEntry.timestamp) {
        trace(s"Adding index entry $timestamp => $offset to ${file.getAbsolutePath}.")
        mmap.putLong(timestamp) // 插入时间戳
        mmap.putInt(relativeOffset(offset)) // 插入相对offset
        _entries += 1 // 更新索引个数
        _lastEntry = TimestampOffset(timestamp, offset) // 更新最大位移值
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