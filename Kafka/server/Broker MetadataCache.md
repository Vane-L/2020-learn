### Broker的元数据缓存——MetadataCache
为什么每个broker需要保存相同的数据？
- Broker 能够及时响应客户端发送的元数据请求，也就是处理 Metadata 请求。
    - Metadata 请求是为数不多的能够被集群任意 Broker 处理的请求类型之一，也就是客户端程序能够随意地向任何一个 Broker 发送 Metadata 请求，去获取集群的元数据信息。
- Kafka 的一些重要组件会用到这部分数据。比如副本管理器会使用它来获取 Broker 的节点信息，事务管理器会使用它来获取分区 Leader 副本的信息，等等。
```scala
/**
 *  A cache for the state of each partition. 
 *  This cache is updated through UpdateMetadataRequest from the controller. 
 *  Every broker maintains the same cache, asynchronously.
 */
class MetadataCache(brokerId: Int) extends Logging {

  private val partitionMetadataLock = new ReentrantReadWriteLock()
  // 1. metadataSnapshot 字段保存了实际的元数据信息 
  // 2. every MetadataSnapshot instance is immutable, and updates (performed under a lock) replace the value with a completely new one. 
  // 3. this means reads (which are not under any lock) need to grab the value of this var (into a val) ONCE and retain that read copy for the duration of their operation.
  // 4. multiple reads of this value risk getting different snapshots.
  @volatile private var metadataSnapshot: MetadataSnapshot = MetadataSnapshot(partitionStates = mutable.AnyRefMap.empty,
    controllerId = None, aliveBrokers = mutable.LongMap.empty, aliveNodes = mutable.LongMap.empty)

  this.logIdent = s"[MetadataCache brokerId=$brokerId] "
  private val stateChangeLogger = new StateChangeLogger(brokerId, inControllerContext = false, None)
  
  
  case class MetadataSnapshot(partitionStates: mutable.AnyRefMap[String, mutable.LongMap[UpdateMetadataPartitionState]],
                              controllerId: Option[Int],
                              aliveBrokers: mutable.LongMap[Broker],
                              aliveNodes: mutable.LongMap[collection.Map[ListenerName, Node]])


  def getAllTopics(): Set[String] = {
    getAllTopics(metadataSnapshot)
  }

  def getAllPartitions(): Set[TopicPartition] = {
    metadataSnapshot.partitionStates.flatMap { case (topicName, partitionsAndStates) =>
      partitionsAndStates.keys.map(partitionId => new TopicPartition(topicName, partitionId.toInt))
    }.toSet
  }

  private def getAllTopics(snapshot: MetadataSnapshot): Set[String] = {
    snapshot.partitionStates.keySet
  }

  private def getAllPartitions(snapshot: MetadataSnapshot): Map[TopicPartition, UpdateMetadataPartitionState] = {
    snapshot.partitionStates.flatMap { case (topic, partitionStates) =>
      partitionStates.map { case (partition, state ) => (new TopicPartition(topic, partition.toInt), state) }
    }.toMap
  }

  // 获取指定监听器类型下该主题分区所有副本的 Broker 节点对象，并按照 Broker ID 进行分组
  def getPartitionReplicaEndpoints(tp: TopicPartition, listenerName: ListenerName): Map[Int, Node] = {
    // 使用val局部变量获取当前元数据缓存(无需使用锁)
    val snapshot = metadataSnapshot
    // 获取给定主题分区的数据
    snapshot.partitionStates.get(tp.topic).flatMap(_.get(tp.partition)).map { partitionInfo =>
      val replicaIds = partitionInfo.replicas
      replicaIds.asScala
        .map(replicaId => replicaId.intValue() -> {
          // 获取副本所在的Broker Id
          snapshot.aliveBrokers.get(replicaId.longValue()) match {
            case Some(broker) =>
              // 根据Broker Id去获取对应的Broker节点对象
              broker.getNode(listenerName).getOrElse(Node.noNode())
            case None =>
              Node.noNode()
          }}).toMap
        .filter(pair => pair match {
          case (_, node) => !node.isEmpty
        })
    }.getOrElse(Map.empty[Int, Node])
  }


  // This method returns the deleted TopicPartitions received from UpdateMetadataRequest
  // 两次构造新的MetadataSnapshot对象
  // 第一次：metadataSnapshot = MetadataSnapshot(metadataSnapshot.partitionStates, controllerId, aliveBrokers, aliveNodes)
  // 第二次：metadataSnapshot = MetadataSnapshot(partitionStates, controllerId, aliveBrokers, aliveNodes)
  def updateMetadata(correlationId: Int, updateMetadataRequest: UpdateMetadataRequest): Seq[TopicPartition] = {
    inWriteLock(partitionMetadataLock) {

      val aliveBrokers = new mutable.LongMap[Broker](metadataSnapshot.aliveBrokers.size)
      val aliveNodes = new mutable.LongMap[collection.Map[ListenerName, Node]](metadataSnapshot.aliveNodes.size)
      // 从UpdateMetadataRequest获取controller的broker.id
      val controllerId = updateMetadataRequest.controllerId match {
          case id if id < 0 => None
          case id => Some(id)
        }

      // 遍历所有存活的broker
      updateMetadataRequest.liveBrokers.asScala.foreach { broker =>
        // `aliveNodes` is a hot path for metadata requests for large clusters, so we use java.util.HashMap which
        // is a bit faster than scala.collection.mutable.HashMap. When we drop support for Scala 2.10, we could
        // move to `AnyRefMap`, which has comparable performance.
        val nodes = new java.util.HashMap[ListenerName, Node]
        val endPoints = new mutable.ArrayBuffer[EndPoint]
        broker.endpoints.asScala.foreach { ep =>
          val listenerName = new ListenerName(ep.listener)
          endPoints += new EndPoint(ep.host, ep.port, listenerName, SecurityProtocol.forId(ep.securityProtocol))
          nodes.put(listenerName, new Node(broker.id, ep.host, ep.port))
        }
        // 将Broker加入到存活Broker对象集合
        aliveBrokers(broker.id) = Broker(broker.id, endPoints, Option(broker.rack))
        // 将Broker节点加入到存活节点对象集合
        aliveNodes(broker.id) = nodes.asScala
      }
      aliveNodes.get(brokerId).foreach { listenerMap =>
        val listeners = listenerMap.keySet
        // 如果发现Broker的监听器与其他Broker不一致，则记录错误日志
        if (!aliveNodes.values.forall(_.keySet == listeners))
          error(s"Listeners are not identical across brokers: $aliveNodes")
      }

      // 构造已删除分区数组
      val deletedPartitions = new mutable.ArrayBuffer[TopicPartition]
      // UpdateMetadataRequest请求是否携带任何分区信息
      if (!updateMetadataRequest.partitionStates.iterator.hasNext) {
        // 1. 构造新的MetadataSnapshot对象(metadataSnapshot.partitionStates)
        metadataSnapshot = MetadataSnapshot(metadataSnapshot.partitionStates, controllerId, aliveBrokers, aliveNodes)
      } else {
        // 备份metadata的分区数据
        val partitionStates = new mutable.AnyRefMap[String, mutable.LongMap[UpdateMetadataPartitionState]](metadataSnapshot.partitionStates.size)
        metadataSnapshot.partitionStates.foreach { case (topic, oldPartitionStates) =>
          val copy = new mutable.LongMap[UpdateMetadataPartitionState](oldPartitionStates.size)
          copy ++= oldPartitionStates
          partitionStates += (topic -> copy)
        }
        // 获取UpdateMetadataRequest的所有分区数据
        updateMetadataRequest.partitionStates.asScala.foreach { info =>
          val controllerId = updateMetadataRequest.controllerId
          val controllerEpoch = updateMetadataRequest.controllerEpoch
          val tp = new TopicPartition(info.topicName, info.partitionIndex)
          // 如果分区被删除
          if (info.leader == LeaderAndIsr.LeaderDuringDelete) {
            // 将分区数据从metadata移除
            removePartitionInfo(partitionStates, tp.topic, tp.partition)
            stateChangeLogger.trace(s"Deleted partition $tp from metadata cache in response to UpdateMetadata " +
              s"request sent by controller $controllerId epoch $controllerEpoch with correlation id $correlationId")
            // 将分区数据加入到deletedPartitions
            deletedPartitions += tp
          } else {
            // 将分区数据加入到metadata
            addOrUpdatePartitionInfo(partitionStates, tp.topic, tp.partition, info)
            stateChangeLogger.trace(s"Cached leader info $info for partition $tp in response to " +
              s"UpdateMetadata request sent by controller $controllerId epoch $controllerEpoch with correlation id $correlationId")
          }
        }
        // 2. 构造新的MetadataSnapshot对象(partitionStates)
        metadataSnapshot = MetadataSnapshot(partitionStates, controllerId, aliveBrokers, aliveNodes)
      }
      deletedPartitions
    }
  }
}
```