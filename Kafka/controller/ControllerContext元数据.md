### ControllerContext
- stats: 状态信息
- offlinePartitionCount: 离线分区数量
- shuttingDownBrokerIds: 正在关闭中的Broker列表
- liveBrokers: 当前运行中的Broker列表
- allTopics: 所有topic列表
- partitionAssignments: topic分区的副本分配情况
- partitionLeadershipInfo: 主题分区的leader信息
- partitionsBeingReassigned: 重新分配的分区列表
- partitionStates: 分区状态信息
- replicaStates: 副本状态信息
- topicsToBeDeleted: 待删除的topic列表
```scala
class ControllerContext {
  val stats = new ControllerStats // 状态信息
  var offlinePartitionCount = 0 // 离线分区数量
  var preferredReplicaImbalanceCount = 0
  val shuttingDownBrokerIds = mutable.Set.empty[Int] // 正在关闭中的Broker列表
  private val liveBrokers = mutable.Set.empty[Broker] // 当前运行中的Broker列表
  private val liveBrokerEpochs = mutable.Map.empty[Int, Long] // 当前运行中Broker的Epoch信息
  var epoch: Int = KafkaController.InitialControllerEpoch // zk的controller_epoch节点的值
  var epochZkVersion: Int = KafkaController.InitialControllerEpochZkVersion // controller_epoch的ZNode版本

  val allTopics = mutable.Set.empty[String] // 所有topic列表
  val partitionAssignments = mutable.Map.empty[String, mutable.Map[Int, ReplicaAssignment]] // topic分区的副本分配情况
  private val partitionLeadershipInfo = mutable.Map.empty[TopicPartition, LeaderIsrAndControllerEpoch] // 主题分区的leader信息，如Leader、Isr、Epoch
  val partitionsBeingReassigned = mutable.Set.empty[TopicPartition] // 重新分配的分区列表
  val partitionStates = mutable.Map.empty[TopicPartition, PartitionState] // 分区状态信息
  val replicaStates = mutable.Map.empty[PartitionAndReplica, ReplicaState] // 副本状态信息
  val replicasOnOfflineDirs = mutable.Map.empty[Int, Set[TopicPartition]] // 不可用路径的副本列表

  val topicsToBeDeleted = mutable.Set.empty[String] // 待删除的topic列表
  // 原因是当一个主题排队等待删除时，由于正在进行的分区重新分配，该主题可能不符合删除条件。
  // 因此，在将要删除的主题加入队列与实际开始删除之间可能会有延迟。在此延迟的间隔中，分区可能仍会过渡到或脱离OfflinePartition状态。
  val topicsWithDeletionStarted = mutable.Set.empty[String] // 开始删除的topic列表
  val topicsIneligibleForDeletion = mutable.Set.empty[String] // 不符合删除条件的topic列表
}
```

#### ControllerStats
```scala
private[controller] class ControllerStats extends KafkaMetricsGroup {
  // 每秒Unclean Leader选举次数
  val uncleanLeaderElectionRate = newMeter("UncleanLeaderElectionsPerSec", "elections", TimeUnit.SECONDS)
  // Controller状态的速率和时间
  val rateAndTimeMetrics: Map[ControllerState, KafkaTimer] = ControllerState.values.flatMap { state =>
    state.rateAndTimeMetricName.map { metricName =>
      state -> new KafkaTimer(newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS))
    }
  }.toMap

}
```

#### offlinePartitionCount
```scala
  private def updatePartitionStateMetrics(partition: TopicPartition,
                                          currentState: PartitionState,
                                          targetState: PartitionState): Unit = {
    // topic是否处于删除状态
    if (!isTopicDeletionInProgress(partition.topic)) { 
      if (currentState != OfflinePartition && targetState == OfflinePartition) {
        offlinePartitionCount = offlinePartitionCount + 1
      } else if (currentState == OfflinePartition && targetState != OfflinePartition) {
        offlinePartitionCount = offlinePartitionCount - 1
      }
    }
  }
```

#### shuttingDownBrokerIds
```scala
  // KafkaController.scala  
  private def onBrokerFailure(deadBrokers: Seq[Int]): Unit = {
    info(s"Broker failure callback for ${deadBrokers.mkString(",")}")
    deadBrokers.foreach(controllerContext.replicasOnOfflineDirs.remove)
    val deadBrokersThatWereShuttingDown =
      deadBrokers.filter(id => controllerContext.shuttingDownBrokerIds.remove(id))
    if (deadBrokersThatWereShuttingDown.nonEmpty)
      info(s"Removed ${deadBrokersThatWereShuttingDown.mkString(",")} from list of shutting down brokers.")
    val allReplicasOnDeadBrokers = controllerContext.replicasOnBrokers(deadBrokers.toSet)
    onReplicasBecomeOffline(allReplicasOnDeadBrokers)

    unregisterBrokerModificationsHandler(deadBrokers)
  }
```

#### liveBrokers & liveBrokerEpochs
```scala
  def addLiveBrokers(brokerAndEpochs: Map[Broker, Long]): Unit = {
    liveBrokers ++= brokerAndEpochs.keySet
    liveBrokerEpochs ++= brokerAndEpochs.map { case (broker, brokerEpoch) => (broker.id, brokerEpoch) }
  }
```

#### allTopics
```scala
  def setAllTopics(topics: Set[String]): Unit = {
    allTopics.clear()
    allTopics ++= topics
  }

  def removeTopic(topic: String): Unit = {
    if (!topicsToBeDeleted.contains(topic))
      cleanPreferredReplicaImbalanceMetric(topic)
    topicsToBeDeleted -= topic
    topicsWithDeletionStarted -= topic
    allTopics -= topic
    partitionAssignments.remove(topic).foreach { assignments =>
      assignments.keys.foreach { partition =>
        partitionLeadershipInfo.remove(new TopicPartition(topic, partition))
      }
    }
  }
```

#### partitionAssignments
```scala
  def partitionsOnBroker(brokerId: Int): Set[TopicPartition] = {
    partitionAssignments.flatMap {
      case (topic, topicReplicaAssignment) => topicReplicaAssignment.filter {
        case (_, partitionAssignment) => partitionAssignment.replicas.contains(brokerId)
      }.map {
        case (partition, _) => new TopicPartition(topic, partition)
      }
    }.toSet
  }

  def partitionsForTopic(topic: String): collection.Set[TopicPartition] = {
    partitionAssignments.getOrElse(topic, mutable.Map.empty).map {
      case (partition, _) => new TopicPartition(topic, partition)
    }.toSet
  }
```

#### partitionLeadershipInfo
```scala
  def partitionLeadersOnBroker(brokerId: Int): Set[TopicPartition] = {
    partitionLeadershipInfo.filter { case (topicPartition, leaderIsrAndControllerEpoch) =>
      !isTopicQueuedUpForDeletion(topicPartition.topic) &&
        leaderIsrAndControllerEpoch.leaderAndIsr.leader == brokerId &&
        partitionReplicaAssignment(topicPartition).size > 1
    }.keySet
  }
```

#### replicasOnOfflineDirs
```scala
  def isReplicaOnline(brokerId: Int, topicPartition: TopicPartition, includeShuttingDownBrokers: Boolean = false): Boolean = {
    val brokerOnline = {
      if (includeShuttingDownBrokers) liveOrShuttingDownBrokerIds.contains(brokerId)
      else liveBrokerIds.contains(brokerId)
    }
    brokerOnline && !replicasOnOfflineDirs.getOrElse(brokerId, Set.empty).contains(topicPartition)
  }
```