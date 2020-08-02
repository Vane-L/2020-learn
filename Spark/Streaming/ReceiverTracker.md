### ReceiverTracker
ReceiverTracker 运行在 driver 端，是一个管理分布在各个 executor 上的 Receiver 的总指挥者。

```scala
  def start(): Unit = synchronized {
    if (isTrackerStarted) {
      throw new SparkException("ReceiverTracker already started")
    }

    if (!receiverInputStreams.isEmpty) {
      endpoint = ssc.env.rpcEnv.setupEndpoint(
        "ReceiverTracker", new ReceiverTrackerEndpoint(ssc.env.rpcEnv))
      if (!skipReceiverLaunch) launchReceivers()
      logInfo("ReceiverTracker started")
      trackerState = Started
    }
  }
  
  // 从ReceiverInputDStreams中获取接收器，将它们作为并行集合分发给worker节点，然后运行它们。
  private def launchReceivers(): Unit = {
    val receivers = receiverInputStreams.map { nis =>
      val rcvr = nis.getReceiver()
      rcvr.setReceiverId(nis.id)
      rcvr
    }

    runDummySparkJob()

    logInfo("Starting " + receivers.length + " receivers")
    endpoint.send(StartAllReceivers(receivers))
  }
```

Receiver
接收数据并存储到Spark Streaming：
- 单条小数据
- 数组形式的块数据
- iterator 形式的块数据
- ByteBuffer 形式的块数据
以上4种方法的实现都是直接将数据转给 ReceiverSupervisor，由 ReceiverSupervisor 来具体负责存储。
```scala
abstract class Receiver[T](val storageLevel: StorageLevel) extends Serializable {
  def onStart(): Unit
  def onStop(): Unit


  def store(dataItem: T): Unit = {
    supervisor.pushSingle(dataItem)
  }
  def store(dataBuffer: ArrayBuffer[T]): Unit = {
    supervisor.pushArrayBuffer(dataBuffer, None, None)
  }
  def store(dataIterator: Iterator[T]): Unit = {
    supervisor.pushIterator(dataIterator, None, None)
  }
  def store(bytes: ByteBuffer): Unit = {
    supervisor.pushBytes(bytes, None, None)
  }

}
```

BlockHandler
ReceivedBlockHandler 是一个接口类，在 executor 端负责对接收到的块数据进行具体的存储和清理
- WriteAheadLogBasedBlockHandler，是先写 WAL，再存储到 executor 的内存或硬盘
```scala
  def storeBlock(blockId: StreamBlockId, block: ReceivedBlock): ReceivedBlockStoreResult = {

    var numRecords = Option.empty[Long]
    // Serialize the block so that it can be inserted into both
    val serializedBlock = block match {
      case ArrayBufferBlock(arrayBuffer) =>
        numRecords = Some(arrayBuffer.size.toLong)
        serializerManager.dataSerialize(blockId, arrayBuffer.iterator)
      case IteratorBlock(iterator) =>
        val countIterator = new CountingIterator(iterator)
        val serializedBlock = serializerManager.dataSerialize(blockId, countIterator)
        numRecords = countIterator.count
        serializedBlock
      case ByteBufferBlock(byteBuffer) =>
        new ChunkedByteBuffer(byteBuffer.duplicate())
      case _ =>
        throw new Exception(s"Could not push $blockId to block manager, unexpected block type")
    }

    // 1. Store the block in block manager
    val storeInBlockManagerFuture = Future {
      val putSucceeded = blockManager.putBytes(
        blockId,
        serializedBlock,
        effectiveStorageLevel,
        tellMaster = true)
      if (!putSucceeded) {
        throw new SparkException(
          s"Could not store $blockId to block manager with storage level $storageLevel")
      }
    }

    // 2. Store the block in write ahead log
    val storeInWriteAheadLogFuture = Future {
      writeAheadLog.write(serializedBlock.toByteBuffer, clock.getTimeMillis())
    }

    // 3. Combine the futures, wait for both to complete, and return the write ahead log record handle
    val combinedFuture = storeInBlockManagerFuture.zip(storeInWriteAheadLogFuture).map(_._2)
    val walRecordHandle = ThreadUtils.awaitResult(combinedFuture, blockStoreTimeout)
    WriteAheadLogBasedStoreResult(blockId, numRecords, walRecordHandle)
  }
```
- BlockManagerBasedBlockHandler，是直接存到 executor 的内存或硬盘
    - BlockManagerBasedBlockHandler 主要是直接存储到 Spark Core 里的 BlockManager 里。
    - BlockManager 将在 **executor** 端接收 Block 数据，而在 **driver** 端维护 Block 的 meta 信息。
```scala
  def storeBlock(blockId: StreamBlockId, block: ReceivedBlock): ReceivedBlockStoreResult = {

    var numRecords: Option[Long] = None

    val putSucceeded: Boolean = block match {
      case ArrayBufferBlock(arrayBuffer) =>
        numRecords = Some(arrayBuffer.size.toLong)
        blockManager.putIterator(blockId, arrayBuffer.iterator, storageLevel,
          tellMaster = true)
      case IteratorBlock(iterator) =>
        val countIterator = new CountingIterator(iterator)
        val putResult = blockManager.putIterator(blockId, countIterator, storageLevel,
          tellMaster = true)
        numRecords = countIterator.count
        putResult
      case ByteBufferBlock(byteBuffer) =>
        blockManager.putBytes(
          blockId, new ChunkedByteBuffer(byteBuffer.duplicate()), storageLevel, tellMaster = true)
      case o =>
        throw new SparkException(
          s"Could not store $blockId to block manager, unexpected block type ${o.getClass.getName}")
    }
    if (!putSucceeded) {
      throw new SparkException(
        s"Could not store $blockId to block manager with storage level $storageLevel")
    }
    BlockManagerBasedStoreResult(blockId, numRecords)
  }
```