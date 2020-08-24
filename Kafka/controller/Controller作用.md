### Controller作用
#### 集群成员管理
- 成员数量管理
    - 每个Broker启动时，会在ZooKeeper的/brokers/ids节点下创建一个名为broker.id参数值的**临时节点**
    - 当Broker正常关闭或意外退出时，ZooKeeper上对应的**临时节点**会自动消失
```scala
class BrokerChangeHandler(eventManager: ControllerEventManager) extends ZNodeChildChangeHandler {
  // ZNode is /brokers/ids
  override val path: String = BrokerIdsZNode.path

  override def handleChildChange(): Unit = {
    eventManager.put(BrokerChange)
  }
}

  private def processBrokerChange(): Unit = {
    // 如果该Broker不是Controller，无权处理，直接返回
    if (!isActive) return
    // 获取ZooKeeper的集群Broker和Epoch
    val curBrokerAndEpochs = zkClient.getAllBrokerAndEpochsInCluster
    val curBrokerIdAndEpochs = curBrokerAndEpochs map { case (broker, epoch) => (broker.id, epoch) }
    val curBrokerIds = curBrokerIdAndEpochs.keySet
    // 获取ControllerContext的Broker列表
    val liveOrShuttingDownBrokerIds = controllerContext.liveOrShuttingDownBrokerIds
    // 比较两个列表，获取新增Broker列表(A-B) & 待移除Broker列表(B-A)
    val newBrokerIds = curBrokerIds -- liveOrShuttingDownBrokerIds
    val deadBrokerIds = liveOrShuttingDownBrokerIds -- curBrokerIds
    // 已重启的Broker列表(A^B中Epoch值变更的Broker)
    val bouncedBrokerIds = (curBrokerIds & liveOrShuttingDownBrokerIds)
      .filter(brokerId => curBrokerIdAndEpochs(brokerId) > controllerContext.liveBrokerIdAndEpochs(brokerId))
    val newBrokerAndEpochs = curBrokerAndEpochs.filter { case (broker, _) => newBrokerIds.contains(broker.id) }
    val bouncedBrokerAndEpochs = curBrokerAndEpochs.filter { case (broker, _) => bouncedBrokerIds.contains(broker.id) }
    val newBrokerIdsSorted = newBrokerIds.toSeq.sorted
    val deadBrokerIdsSorted = deadBrokerIds.toSeq.sorted
    val liveBrokerIdsSorted = curBrokerIds.toSeq.sorted
    val bouncedBrokerIdsSorted = bouncedBrokerIds.toSeq.sorted
    info(s"Newly added brokers: ${newBrokerIdsSorted.mkString(",")}, " +
      s"deleted brokers: ${deadBrokerIdsSorted.mkString(",")}, " +
      s"bounced brokers: ${bouncedBrokerIdsSorted.mkString(",")}, " +
      s"all live brokers: ${liveBrokerIdsSorted.mkString(",")}")
    // 为每个新增Broker创建通道管理器和请求发送线程
    newBrokerAndEpochs.keySet.foreach(controllerChannelManager.addBroker)
    // 为每个已重启的Broker移除它们的通道管理器和RequestSendThread，并重新添加它们
    bouncedBrokerIds.foreach(controllerChannelManager.removeBroker)
    bouncedBrokerAndEpochs.keySet.foreach(controllerChannelManager.addBroker)
    // 为每个待移除Broker移除对应的配套资源
    deadBrokerIds.foreach(controllerChannelManager.removeBroker)
    // 为新增Broker执行更新controllerContext和启动Broker
    if (newBrokerIds.nonEmpty) {
      controllerContext.addLiveBrokersAndEpochs(newBrokerAndEpochs)
      onBrokerStartup(newBrokerIdsSorted)
    }
    // 为已重启Broker执行重添加逻辑
    if (bouncedBrokerIds.nonEmpty) {
      controllerContext.removeLiveBrokers(bouncedBrokerIds)
      onBrokerFailure(bouncedBrokerIdsSorted)
      controllerContext.addLiveBrokersAndEpochs(bouncedBrokerAndEpochs)
      onBrokerStartup(bouncedBrokerIdsSorted)
    }
    // 为待移除Broker执行移除ControllerContext和终止Broker
    if (deadBrokerIds.nonEmpty) {
      controllerContext.removeLiveBrokers(deadBrokerIds)
      onBrokerFailure(deadBrokerIdsSorted)
    }

    if (newBrokerIds.nonEmpty || deadBrokerIds.nonEmpty || bouncedBrokerIds.nonEmpty) {
      info(s"Updated broker epochs cache: ${controllerContext.liveBrokerIdAndEpochs}")
    }
  }
```
##### broker启动
```scala
  private def onBrokerStartup(newBrokers: Seq[Int]): Unit = {
    info(s"New broker startup callback for ${newBrokers.mkString(",")}")
    // 移除元数据中新增Broker对应的副本集合
    newBrokers.foreach(controllerContext.replicasOnOfflineDirs.remove)
    val newBrokersSet = newBrokers.toSet
    val existingBrokers = controllerContext.liveOrShuttingDownBrokerIds -- newBrokers
    // Send update metadata request to all the existing brokers in the cluster so that they know about the new brokers
    // via this update. No need to include any partition states in the request since there are no partition state changes.
    sendUpdateMetadataRequest(existingBrokers.toSeq, Set.empty)
    // Send update metadata request to all the new brokers in the cluster with a full set of partition states for initialization.
    // In cases of controlled shutdown leaders will not be elected when a new broker comes up. So at least in the
    // common controlled shutdown case, the metadata will reach the new brokers faster.
    sendUpdateMetadataRequest(newBrokers, controllerContext.partitionLeadershipInfo.keySet)
    // the very first thing to do when a new broker comes up is send it the entire list of partitions that it is
    // supposed to host. Based on that the broker starts the high watermark threads for the input list of partitions
    val allReplicasOnNewBrokers = controllerContext.replicasOnBrokers(newBrokersSet)
    replicaStateMachine.handleStateChanges(allReplicasOnNewBrokers.toSeq, OnlineReplica)
    // when a new broker comes up, the controller needs to trigger leader election for all new and offline partitions
    // to see if these brokers can become leaders for some/all of those
    partitionStateMachine.triggerOnlinePartitionStateChange()
    // check if reassignment of some partitions need to be restarted
    maybeResumeReassignments { (_, assignment) =>
      assignment.targetReplicas.exists(newBrokersSet.contains)
    }
    // check if topic deletion needs to be resumed. If at least one replica that belongs to the topic being deleted exists
    // on the newly restarted brokers, there is a chance that topic deletion can resume
    val replicasForTopicsToBeDeleted = allReplicasOnNewBrokers.filter(p => topicDeletionManager.isTopicQueuedUpForDeletion(p.topic))
    if (replicasForTopicsToBeDeleted.nonEmpty) {
      info(s"Some replicas ${replicasForTopicsToBeDeleted.mkString(",")} for topics scheduled for deletion " +
        s"${controllerContext.topicsToBeDeleted.mkString(",")} are on the newly restarted brokers " +
        s"${newBrokers.mkString(",")}. Signaling restart of topic deletion for these topics")
      topicDeletionManager.resumeDeletionForTopics(replicasForTopicsToBeDeleted.map(_.topic))
    }
    // 为新增Broker注册BrokerModificationsHandler监听器
    registerBrokerModificationsHandler(newBrokers)
  }
```
##### broker终止
```scala
  private def onBrokerFailure(deadBrokers: Seq[Int]): Unit = {
    info(s"Broker failure callback for ${deadBrokers.mkString(",")}")
    // 为每个待移除Broker，删除元数据对象中的相关项
    deadBrokers.foreach(controllerContext.replicasOnOfflineDirs.remove)
    // 将待移除Broker从元数据对象中移除已关闭的Broker
    val deadBrokersThatWereShuttingDown =
      deadBrokers.filter(id => controllerContext.shuttingDownBrokerIds.remove(id))
    if (deadBrokersThatWereShuttingDown.nonEmpty)
      info(s"Removed ${deadBrokersThatWereShuttingDown.mkString(",")} from list of shutting down brokers.")
    // 找出待移除Broker上的所有副本对象，执行onReplicasBecomeOffline操作
    val allReplicasOnDeadBrokers = controllerContext.replicasOnBrokers(deadBrokers.toSet)
    onReplicasBecomeOffline(allReplicasOnDeadBrokers)
    // 注销BrokerModificationsHandler监听器
    unregisterBrokerModificationsHandler(deadBrokers)
  }
```
- 成员信息管理
```scala
class BrokerModificationsHandler(eventManager: ControllerEventManager, brokerId: Int) extends ZNodeChangeHandler {
  // ZNode is /brokers/ids/id
  override val path: String = BrokerIdZNode.path(brokerId)

  override def handleDataChange(): Unit = {
    eventManager.put(BrokerModifications(brokerId))
  }
}

  private def processBrokerModification(brokerId: Int): Unit = {
    // 是否为controller
    if (!isActive) return
    // 获取Broker的metadata
    val newMetadataOpt = zkClient.getBroker(brokerId)
    val oldMetadataOpt = controllerContext.liveOrShuttingDownBroker(brokerId)
    if (newMetadataOpt.nonEmpty && oldMetadataOpt.nonEmpty) {
      val oldMetadata = oldMetadataOpt.get
      val newMetadata = newMetadataOpt.get
      if (newMetadata.endPoints != oldMetadata.endPoints) {
        info(s"Updated broker metadata: $oldMetadata -> $newMetadata")
        // 更新元数据缓存
        controllerContext.updateBrokerMetadata(oldMetadata, newMetadata)
        // 执行onBrokerUpdate方法处理Broker更新
        onBrokerUpdate(brokerId)
      }
    }
  }

  private def onBrokerUpdate(updatedBrokerId: Int): Unit = {
    info(s"Broker info update callback for $updatedBrokerId")
    // 给集群所有Broker发送UpdateMetadataRequest
    sendUpdateMetadataRequest(controllerContext.liveOrShuttingDownBrokerIds.toSeq, Set.empty)
  }

  private[controller] def sendUpdateMetadataRequest(brokers: Seq[Int], partitions: Set[TopicPartition]): Unit = {
    try {
      brokerRequestBatch.newBatch()
      brokerRequestBatch.addUpdateMetadataRequestForBrokers(brokers, partitions)
      brokerRequestBatch.sendRequestsToBrokers(epoch)
    } catch {
      case e: IllegalStateException =>
        handleIllegalState(e)
    }
  }
```
- 主题变更
```scala
class TopicChangeHandler(eventManager: ControllerEventManager) extends ZNodeChildChangeHandler {
  // ZNode is /brokers/topics
  override val path: String = TopicsZNode.path

  override def handleChildChange(): Unit = eventManager.put(TopicChange)
}

  private def processTopicChange(): Unit = {
    // 是否为controller
    if (!isActive) return
    // 从ZooKeeper获取所有主题
    val topics = zkClient.getAllTopicsInCluster
    // 新增的主题
    val newTopics = topics -- controllerContext.allTopics
    // 待删除的主题
    val deletedTopics = controllerContext.allTopics -- topics
    // 更新controllerContext的所有主题
    controllerContext.allTopics = topics
    // 为新增主题注册分区变更监听器
    registerPartitionModificationsHandlers(newTopics.toSeq)
    // 获取新增主题的副本分配情况
    val addedPartitionReplicaAssignment = zkClient.getFullReplicaAssignmentForTopics(newTopics)
    // 清除controllerContext的已删除主题
    deletedTopics.foreach(controllerContext.removeTopic)
    // 为新增主题更新元数据缓存中的副本分配条目
    addedPartitionReplicaAssignment.foreach {
      case (topicAndPartition, newReplicaAssignment) => controllerContext.updatePartitionFullReplicaAssignment(topicAndPartition, newReplicaAssignment)
    }
    info(s"New topics: [$newTopics], deleted topics: [$deletedTopics], new partition replica assignment " +
      s"[$addedPartitionReplicaAssignment]")
    // 新增主题的分区和副本状态变更为online
    if (addedPartitionReplicaAssignment.nonEmpty)
      onNewPartitionCreation(addedPartitionReplicaAssignment.keySet)
  }

  private def onNewPartitionCreation(newPartitions: Set[TopicPartition]): Unit = {
    info(s"New partition creation callback for ${newPartitions.mkString(",")}")
    partitionStateMachine.handleStateChanges(newPartitions.toSeq, NewPartition)
    replicaStateMachine.handleStateChanges(controllerContext.replicasForPartition(newPartitions).toSeq, NewReplica)
    partitionStateMachine.handleStateChanges(
      newPartitions.toSeq,
      OnlinePartition,
      Some(OfflinePartitionLeaderElectionStrategy(false))
    )
    replicaStateMachine.handleStateChanges(controllerContext.replicasForPartition(newPartitions).toSeq, OnlineReplica)
  }
```
- 主题删除
```scala
class TopicDeletionHandler(eventManager: ControllerEventManager) extends ZNodeChildChangeHandler {
  // ZNode is /admin/delete_topics
  override val path: String = DeleteTopicsZNode.path

  override def handleChildChange(): Unit = eventManager.put(TopicDeletion)
}

  private def processTopicDeletion(): Unit = {
    // 是否为controller
    if (!isActive) return
    // 从ZooKeeper获取待删除主题列表
    var topicsToBeDeleted = zkClient.getTopicDeletions.toSet
    debug(s"Delete topics listener fired for topics ${topicsToBeDeleted.mkString(",")} to be deleted")
    // 不存在的主题列表
    val nonExistentTopics = topicsToBeDeleted -- controllerContext.allTopics
    if (nonExistentTopics.nonEmpty) {
      warn(s"Ignoring request to delete non-existing topics ${nonExistentTopics.mkString(",")}")
      // 删除不存在主题的zk节点
      zkClient.deleteTopicDeletions(nonExistentTopics.toSeq, controllerContext.epochZkVersion)
    }
    topicsToBeDeleted --= nonExistentTopics
    // 是否支持删除(配置项delete.topic.enable)
    if (config.deleteTopicEnable) {
      if (topicsToBeDeleted.nonEmpty) {
        info(s"Starting topic deletion for topics ${topicsToBeDeleted.mkString(",")}")
        // mark topic ineligible for deletion if other state changes are in progress
        topicsToBeDeleted.foreach { topic =>
          val partitionReassignmentInProgress =
            controllerContext.partitionsBeingReassigned.map(_.topic).contains(topic)
          if (partitionReassignmentInProgress)
            // 如果正在重分配，则标记为不可删除
            topicDeletionManager.markTopicIneligibleForDeletion(Set(topic),
              reason = "topic reassignment in progress")
        }
        // add topic to deletion list
        topicDeletionManager.enqueueTopicsForDeletion(topicsToBeDeleted)
      }
    } else {
      // If delete topic is disabled remove entries under zookeeper path : /admin/delete_topics
      info(s"Removing $topicsToBeDeleted since delete topic is disabled")
      zkClient.deleteTopicDeletions(topicsToBeDeleted.toSeq, controllerContext.epochZkVersion)
    }
  }
```