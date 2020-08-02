### WriteAheadLog框架
WriteAheadLog 是多条 log 的集合，每条具体的 log 的引用就是一个 LogRecordHandle。
- WriteAheadLog 基于文件的具体实现是 FileBasedWriteAheadLog

```scala
public abstract class WriteAheadLog {
  // 写入一条 log，将返回一个指向这条 log 的句柄引用
  public abstract WriteAheadLogRecordHandle write(ByteBuffer record, long time);
  // 给定一条 log 的句柄引用，读出这条 log
  public abstract ByteBuffer read(WriteAheadLogRecordHandle handle);
  // 读取全部 log
  public abstract Iterator<ByteBuffer> readAll();
  // 清理过期的 log record
  public abstract void clean(long threshTime, boolean waitForCompletion);
  // 关闭log
  public abstract void close();
}
```
FileBasedWriteAheadLog
- WriteAheadLogRecordHandle 基于文件的具体实现是 FileBasedWriteAheadLogSegment
- rolling 配置项
    - FileBasedWriteAheadLog 是rolling write，具体实现是把 log 写到一个文件里，然后每隔一段时间就关闭已有文件，产生一些新文件继续写，也就是 
    - rolling 写的好处是单个文件不会太大，而且删除不用的旧数据特别方便
    - rolling 的间隔是参数 spark.streaming.receiver.writeAheadLog.rollingIntervalSecs（默认 = 60 秒）
- WAL 目录：{checkpointDir}/receivedData/{receiverId}
    - {checkpointDir} 在 ssc.checkpoint(checkpointDir) 指定的
    - {receiverId} 是 Receiver 的 id
    - 在这个 WAL 目录里，不同的 rolling log 文件的命名规则是 **log-{startTime}-{stopTime}**
- FileBasedWriteAheadLog.currentLogWriter
    - 一个 LogWriter 对应一个 log file，而且 log 文件本身是 rolling 的，那么前一个 log 文件写完成后，对应的 writer 就会 close() ，然后而由新的 writer 负责写新的文件
    - 如果currentLogWriter == null 或者 currentTime > currentLogWriterStopTime ，就会指定新的 currentLogWriter
```scala
  // 写方法
  def write(byteBuffer: ByteBuffer, time: Long): FileBasedWriteAheadLogSegment = synchronized {
    var fileSegment: FileBasedWriteAheadLogSegment = null
    var failures = 0
    var lastException: Exception = null
    var succeeded = false
    while (!succeeded && failures < maxFailures) {
      try {
        fileSegment = getLogWriter(time).write(byteBuffer)
        if (closeFileAfterWrite) {
          resetWriter()
        }
        succeeded = true
      } catch {
        case ex: Exception =>
          lastException = ex
          logWarning("Failed to write to write ahead log")
          resetWriter()
          failures += 1
      }
    }
    if (fileSegment == null) {
      logError(s"Failed to write to write ahead log after $failures failures")
      throw lastException
    }
    fileSegment
  }

  private def getLogWriter(currentTime: Long): FileBasedWriteAheadLogWriter = synchronized {
    if (currentLogWriter == null || currentTime > currentLogWriterStopTime) {
      resetWriter()
      currentLogPath.foreach {
        pastLogs += LogInfo(currentLogWriterStartTime, currentLogWriterStopTime, _)
      }
      currentLogWriterStartTime = currentTime
      currentLogWriterStopTime = currentTime + (rollingIntervalSecs * 1000L)
      val newLogPath = new Path(logDirectory,
        timeToLogFile(currentLogWriterStartTime, currentLogWriterStopTime))
      currentLogPath = Some(newLogPath.toString)
      currentLogWriter = new FileBasedWriteAheadLogWriter(currentLogPath.get, hadoopConf)
    }
    currentLogWriter
  }
  // 读方法
  def read(segment: WriteAheadLogRecordHandle): ByteBuffer = {
    val fileSegment = segment.asInstanceOf[FileBasedWriteAheadLogSegment]
    var reader: FileBasedWriteAheadLogRandomReader = null
    var byteBuffer: ByteBuffer = null
    try {
      reader = new FileBasedWriteAheadLogRandomReader(fileSegment.path, hadoopConf)
      byteBuffer = reader.read(fileSegment)
    } finally {
      reader.close()
    }
    byteBuffer
  }
```
FileBasedWriteAheadLogWriter 
```scala
private[streaming] class FileBasedWriteAheadLogWriter(path: String, hadoopConf: Configuration) extends Closeable {
  private lazy val stream = HdfsUtils.getOutputStream(path, hadoopConf)
  private var nextOffset = stream.getPos()
  private var closed = false

  def write(data: ByteBuffer): FileBasedWriteAheadLogSegment = synchronized {
    assertOpen()
    data.rewind() // Rewind to ensure all data in the buffer is retrieved
    val lengthToWrite = data.remaining()
    val segment = new FileBasedWriteAheadLogSegment(path, nextOffset, lengthToWrite)
    stream.writeInt(lengthToWrite)
    Utils.writeByteBuffer(data, stream: OutputStream)
    flush()
    nextOffset = stream.getPos()
    segment
  }

  private def flush(): Unit = {
    stream.hflush()
    // Useful for local file system where hflush/sync does not work (HADOOP-7844)
    stream.getWrappedStream.flush()
  }
}
```
FileBasedWriteAheadLogRandomReader 和 FileBasedWriteAheadLogReader
```scala
private[streaming] class FileBasedWriteAheadLogRandomReader(path: String, conf: Configuration) extends Closeable {
  private val instream = HdfsUtils.getInputStream(path, conf)
  private var closed = (instream == null) // the file may be deleted as we're opening the stream

  def read(segment: FileBasedWriteAheadLogSegment): ByteBuffer = synchronized {
    assertOpen()
    // seek 到这条 log 所在的 offset
    instream.seek(segment.offset)
    val nextLength = instream.readInt()
    HdfsUtils.checkState(nextLength == segment.length,
      s"Expected message length to be ${segment.length}, but was $nextLength")
    val buffer = new Array[Byte](nextLength)
    instream.readFully(buffer)
    // 以 ByteBuffer 返回具体的内容
    ByteBuffer.wrap(buffer)
  }
}

private[streaming] class FileBasedWriteAheadLogReader(path: String, conf: Configuration)
  extends Iterator[ByteBuffer] with Closeable with Logging {

  private val instream = HdfsUtils.getInputStream(path, conf)
  private var closed = (instream == null) // the file may be deleted as we're opening the stream
  private var nextItem: Option[ByteBuffer] = None

  override def hasNext: Boolean = synchronized {
    if (closed) {
      return false
    }

    if (nextItem.isDefined) { // handle the case where hasNext is called without calling next
      true
    } else {
      try {
        // 读出来下一条，如果有，就说明还有 hasNext
        val length = instream.readInt()
        val buffer = new Array[Byte](length)
        instream.readFully(buffer)
        nextItem = Some(ByteBuffer.wrap(buffer))
        logTrace("Read next item " + nextItem.get)
        true
      } catch {
        case e: EOFException =>
          logDebug("Error reading next item, EOF reached", e)
          close()
          false
        case e: IOException =>
          logWarning("Error while trying to read data. If the file was deleted, " +
            "this should be okay.", e)
          close()
          if (HdfsUtils.checkFileExists(path, conf)) {
            // If file exists, this could be a legitimate error
            throw e
          } else {
            // File was deleted. This can occur when the daemon cleanup thread takes time to
            // delete the file during recovery.
            false
          }

        case e: Exception =>
          logWarning("Error while trying to read data from HDFS.", e)
          close()
          throw e
      }
    }
  }
  
  override def next(): ByteBuffer = synchronized {
    // 直接返回在 hasNext() 方法里实际读出来的数据
    val data = nextItem.getOrElse {
      close()
      throw new IllegalStateException(
        "next called without calling hasNext or after hasNext returned false")
    }
    nextItem = None // Ensure the next hasNext call loads new data.
    data
  }  
}
```