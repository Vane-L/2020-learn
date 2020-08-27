### TopicDeletionManager主题删除管理器
- DeletionClient
```scala
trait DeletionClient {
  def deleteTopic(topic: String, epochZkVersion: Int): Unit
  def deleteTopicDeletions(topics: Seq[String], epochZkVersion: Int): Unit
  def mutePartitionModifications(topic: String): Unit
  def sendMetadataUpdate(partitions: Set[TopicPartition]): Unit
}
```
- ControllerDeletionClient
```scala
class ControllerDeletionClient(controller: KafkaController, zkClient: KafkaZkClient) extends DeletionClient {
  override def deleteTopic(topic: String, epochZkVersion: Int): Unit = {
    // 删除/brokers/topics/$topic节点
    zkClient.deleteTopicZNode(topic, epochZkVersion)
    // 删除/config/topics/$topic节点
    zkClient.deleteTopicConfigs(Seq(topic), epochZkVersion)
    // 删除/admin/delete_topics/$topic节点
    zkClient.deleteTopicDeletions(Seq(topic), epochZkVersion)
  }

  override def deleteTopicDeletions(topics: Seq[String], epochZkVersion: Int): Unit = {
    // 批量删除/admin/delete_topics/$topics节点
    zkClient.deleteTopicDeletions(topics, epochZkVersion)
  }

  override def mutePartitionModifications(topic: String): Unit = {
    // 取消/brokers/topics/$topic节点数据变更的监听
    controller.unregisterPartitionModificationsHandlers(Seq(topic))
  }

  override def sendMetadataUpdate(partitions: Set[TopicPartition]): Unit = {
    // 向集群Broker发送指定分区的元数据更新请求
    controller.sendUpdateMetadataRequest(controller.controllerContext.liveOrShuttingDownBrokerIds.toSeq, partitions)
  }
}
```
- TopicDeletionManager
```scala
class TopicDeletionManager(config: KafkaConfig,
                           controllerContext: ControllerContext,
                           replicaStateMachine: ReplicaStateMachine, // 副本状态机
                           partitionStateMachine: PartitionStateMachine, // 分区状态机
                           client: DeletionClient) extends Logging{
  // 是否允许删除主题，配置项为delete.topic.enable(默认为true)
  val isDeleteTopicEnabled: Boolean = config.deleteTopicEnable


  private def resumeDeletions(): Unit = {
    // 从元数据缓存中获取要删除的主题列表
    val topicsQueuedForDeletion = Set.empty[String] ++ controllerContext.topicsToBeDeleted
    // 待重试主题列表
    val topicsEligibleForRetry = mutable.Set.empty[String]
    // 待删除主题列表
    val topicsEligibleForDeletion = mutable.Set.empty[String]

    if (topicsQueuedForDeletion.nonEmpty)
      info(s"Handling deletion for topics ${topicsQueuedForDeletion.mkString(",")}")
    // 遍历要删除的主题列表
    topicsQueuedForDeletion.foreach { topic =>
      // if all replicas are marked as deleted successfully, then topic deletion is done
      if (controllerContext.areAllReplicasInState(topic, ReplicaDeletionSuccessful)) {
        // clear up all state for this topic from controller cache and zookeeper
        completeDeleteTopic(topic)
        info(s"Deletion of topic $topic successfully completed")
      } else if (!controllerContext.isAnyReplicaInState(topic, ReplicaDeletionStarted)) {
        // if you come here, then no replica is in TopicDeletionStarted and all replicas are not in
        // TopicDeletionSuccessful. That means, that either given topic haven't initiated deletion
        // or there is at least one failed replica (which means topic deletion should be retried).
        if (controllerContext.isAnyReplicaInState(topic, ReplicaDeletionIneligible)) {
          topicsEligibleForRetry += topic
        }
      }

      // Add topic to the eligible set if it is eligible for deletion.
      // 1. Topic deletion is not already complete
      // 2. Topic deletion is currently not in progress for that topic
      // 3. Topic is currently marked ineligible for deletion
      if (isTopicEligibleForDeletion(topic)) {
        info(s"Deletion of topic $topic (re)started")
        topicsEligibleForDeletion += topic
      }
    }

    // topic deletion retry will be kicked off
    if (topicsEligibleForRetry.nonEmpty) {
      retryDeletionForIneligibleReplicas(topicsEligibleForRetry)
    }

    // topic deletion will be kicked off
    if (topicsEligibleForDeletion.nonEmpty) {
      onTopicDeletion(topicsEligibleForDeletion)
    }
  }
  // 从replicaStateMachine、controllerContext和zookeeper中删除所有状态
  private def completeDeleteTopic(topic: String): Unit = {
    // 注销分区变更监听器，防止删除过程中因分区数据变更导致监听器被触发，引起状态不一致
    client.mutePartitionModifications(topic)
    // 获取该主题下已经被成功删除的副本对象
    val replicasForDeletedTopic = controllerContext.replicasInState(topic, ReplicaDeletionSuccessful)
    // replicaStateMachine更新副本状态为NonExistentReplica
    replicaStateMachine.handleStateChanges(replicasForDeletedTopic.toSeq, NonExistentReplica)
    // 更新controllerContext的待删除主题列表和已开始删除的主题列表
    controllerContext.topicsToBeDeleted -= topic
    controllerContext.topicsWithDeletionStarted -= topic
    // 移除ZooKeeper上该主题的节点
    client.deleteTopic(topic, controllerContext.epochZkVersion)
    // 移除controllerContext中关于该主题的信息
    controllerContext.removeTopic(topic)
  }

  // 
  private def onTopicDeletion(topics: Set[String]): Unit = {
    // 找出给定主题列表中那些尚未开始删除的所有主题
    val unseenTopicsForDeletion = topics -- controllerContext.topicsWithDeletionStarted
    if (unseenTopicsForDeletion.nonEmpty) {
      // 获取到这些主题的所有分区对象
      val unseenPartitionsForDeletion = unseenTopicsForDeletion.flatMap(controllerContext.partitionsForTopic)
      // partitionStateMachine更新分区状态为OfflinePartition和NonExistentPartition
      partitionStateMachine.handleStateChanges(unseenPartitionsForDeletion.toSeq, OfflinePartition)
      partitionStateMachine.handleStateChanges(unseenPartitionsForDeletion.toSeq, NonExistentPartition)
      // 把未开始删除的主题添加到已开启删除操作主题列表中
      controllerContext.beginTopicDeletion(unseenTopicsForDeletion)
    }

    // send update metadata so that brokers stop serving data for topics to be deleted
    client.sendMetadataUpdate(topics.flatMap(controllerContext.partitionsForTopic))

    onPartitionDeletion(topics)
  }

  // 删除分区数据
  private def onPartitionDeletion(topicsToBeDeleted: Set[String]): Unit = {
    // 所有dead的副本
    val allDeadReplicas = mutable.ListBuffer.empty[PartitionAndReplica]
    // 所有重试删除的副本
    val allReplicasForDeletionRetry = mutable.ListBuffer.empty[PartitionAndReplica]
    // 不符合删除条件的所有主题
    val allTopicsIneligibleForDeletion = mutable.Set.empty[String]

    topicsToBeDeleted.foreach { topic =>
      val (aliveReplicas, deadReplicas) = controllerContext.replicasForTopic(topic).partition { r =>
        controllerContext.isReplicaOnline(r.replica, r.topicPartition)
      }
      // 成功删除的副本
      val successfullyDeletedReplicas = controllerContext.replicasInState(topic, ReplicaDeletionSuccessful)
      // 需要重试删除的副本
      val replicasForDeletionRetry = aliveReplicas -- successfullyDeletedReplicas

      allDeadReplicas ++= deadReplicas
      allReplicasForDeletionRetry ++= replicasForDeletionRetry

      if (deadReplicas.nonEmpty) {
        debug(s"Dead Replicas (${deadReplicas.mkString(",")}) found for topic $topic")
        allTopicsIneligibleForDeletion += topic
      }
    }

    // move dead replicas directly to failed state
    replicaStateMachine.handleStateChanges(allDeadReplicas, ReplicaDeletionIneligible)
    // send stop replica to all followers that are not in the OfflineReplica state so they stop sending fetch requests to the leader
    replicaStateMachine.handleStateChanges(allReplicasForDeletionRetry, OfflineReplica)
    replicaStateMachine.handleStateChanges(allReplicasForDeletionRetry, ReplicaDeletionStarted)

    if (allTopicsIneligibleForDeletion.nonEmpty) {
      markTopicIneligibleForDeletion(allTopicsIneligibleForDeletion, reason = "offline replicas")
    }
  }

  /**
   * Halt delete topic if -
   * 1. replicas being down
   * 2. partition reassignment in progress for some partitions of the topic
   */
  def markTopicIneligibleForDeletion(topics: Set[String], reason: => String): Unit = {
    if (isDeleteTopicEnabled) {
      // 停止删除的主题列表
      val newTopicsToHaltDeletion = controllerContext.topicsToBeDeleted & topics
      controllerContext.topicsIneligibleForDeletion ++= newTopicsToHaltDeletion
      if (newTopicsToHaltDeletion.nonEmpty)
        info(s"Halted deletion of topics ${newTopicsToHaltDeletion.mkString(",")} due to $reason")
    }
  }

}
```