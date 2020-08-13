### AbstractIndex
- var file: File (索引文件，注意var file说明索引文件可以修改)
- val baseOffset: Long (索引对象对应日志段对象的起始位移值)
- val maxIndexSize: Int = -1 (索引文件的最大长度，是Broker端参数*segment.index.bytes*的值，即 10MB。)
- val writable: Boolean (打开方式是读写/可读)

```scala
  // Length of the index file
  @volatile
  private var _length: Long = _

  protected def entrySize: Int
  // OffsetIndex(Int起始位移+Int相对位移) : override def entrySize = 8
  // TimeIndex(Long时间戳+Int相对位移) : override def entrySize = 12

  protected def _warmEntries: Int = 8192 / entrySize

  @volatile
  private[this] var _maxEntries: Int = mmap.limit() / entrySize

  @volatile
  protected var _entries: Int = mmap.position() / entrySize

  def isFull: Boolean = _entries >= _maxEntries
```

#### MMap
Kafka **mmaps index files into memory**, and all the read / write operations of the index is through **OS page cache**. 
- This avoids blocked disk I/O in most cases.
```scala
  @volatile
  protected var mmap: MappedByteBuffer = {
    // 创建索引文件
    val newlyCreated = file.createNewFile()
    // 以writable指定读写/只读方式打开索引文件
    val raf = if (writable) new RandomAccessFile(file, "rw") else new RandomAccessFile(file, "r")
    try {
      /* pre-allocate the file if necessary */
      if(newlyCreated) {
        if(maxIndexSize < entrySize)
          throw new IllegalArgumentException("Invalid max index size: " + maxIndexSize)
        // 设置索引文件长度，roundDownToExactMultiple计算的是不超过maxIndexSize的最大整数倍entrySize
        // private def roundDownToExactMultiple(number: Int, factor: Int) = factor * (number / factor) 
        raf.setLength(roundDownToExactMultiple(maxIndexSize, entrySize))
      }

      /* memory-map the file */
      _length = raf.length()
      val idx = {
        if (writable)
          raf.getChannel.map(FileChannel.MapMode.READ_WRITE, 0, _length)
        else
          raf.getChannel.map(FileChannel.MapMode.READ_ONLY, 0, _length)
      }
      /* set the position in the index for the next entry */
      if(newlyCreated)
        idx.position(0)
      else
        // if this is a pre-existing index, assume it is valid and set position to last entry
        idx.position(roundDownToExactMultiple(idx.limit(), entrySize))
      idx
    } finally {
      // 关闭打开索引文件句柄
      CoreUtils.swallow(raf.close(), AbstractIndex)
    }
  }
```

#### 优化后的二分查找
Here, we use a more cache-friendly lookup algorithm:
    if (target > indexEntry[end - N]) // if the target is in the last N entries of the index
      binarySearch(end - N, end)
    else
      binarySearch(begin, end - N)

##### _warmEntries = 8192
   1. This number is **small enough** to guarantee all the pages of the "warm" section is touched in every warm-section lookup. 
      - So that, the entire warm section is really "warm".
      - When doing warm-section lookup, following 3 entries are always touched: indexEntry(end), indexEntry(end-N), and indexEntry((end*2 -N)/2). 
      - If page size >= 4096, all the warm-section pages (3 or fewer) are touched, when we touch those 3 entries. 
      - As of 2018, 4096 is the smallest page size for all the processors (x86-32, x86-64, MIPS, SPARC, Power, ARM etc.).
   2. This number is **large enough** to guarantee most of the in-sync lookups are in the warm-section. 
      - With default Kafka settings, **8KB index** corresponds to about **4MB (offset index)** or **2.7MB (time index)** log messages.
  
```scala
  private def indexSlotRangeFor(idx: ByteBuffer, target: Long, searchEntity: IndexSearchEntity): (Int, Int) = {
    // check if the index is empty
    if(_entries == 0)
      return (-1, -1)

    def binarySearch(begin: Int, end: Int) : (Int, Int) = {
      // binary search for the entry
      var lo = begin
      var hi = end
      while(lo < hi) {
        val mid = (lo + hi + 1) >>> 1
        val found = parseEntry(idx, mid)
        val compareResult = compareIndexEntry(found, target, searchEntity)
        if(compareResult > 0)
          hi = mid - 1
        else if(compareResult < 0)
          lo = mid
        else
          return (mid, mid)
      }
      (lo, if (lo == _entries - 1) -1 else lo + 1)
    }

    // 确认热区首个索引项位于哪个槽。_warmEntries就是所谓的分割线，目前固定为8192字节处 
    // 如果是OffsetIndex，_warmEntries = 8192 / 8 = 1024，即第1024个槽 
    // 如果是TimeIndex，_warmEntries = 8192 / 12 = 682，即第682个槽
    val firstHotEntry = Math.max(0, _entries - 1 - _warmEntries)
    // check if the target offset is in the warm section of the index
    if(compareIndexEntry(parseEntry(idx, firstHotEntry), target, searchEntity) < 0) {
      return binarySearch(firstHotEntry, _entries - 1)
    }

    // check if the target offset is smaller than the least offset
    if(compareIndexEntry(parseEntry(idx, 0), target, searchEntity) > 0)
      return (-1, 0)

    binarySearch(0, firstHotEntry)
  }
```