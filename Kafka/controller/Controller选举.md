### Controller节点
在一个Kafka集群，某一时刻只能有一台Broker被选举为**Controller**。

#### ZooKeeper的/controller节点
- brokerid: controller的broker.id
- ephemeralOwner: 临时节点
Kafka集群上所有的Broker都在实时监听ZooKeeper上的这个节点。
- 监听这个节点是否存在。
    - 倘若发现这个节点不存在，Broker会立即抢注该节点，即创建/controller节点。**创建成功的Broker当选为新一届的Controller。**
- 监听这个节点数据是否发生了变更。
    - 一旦发现该节点的内容发生了变化，Broker也会立即启动新一轮的Controller选举。

#### KafkaController
- ElectionTrigger：选举触发器，这里的选举是指主题分区副本的选举，即为哪些分区选择 Leader 副本。
- Object KafkaController：KafkaController伴生对象，定义了一些常量和回调函数类型。
- ControllerEvent：定义Controller事件类型。
- Class KafkaController：定义KafkaController类以及实际的处理逻辑。
```scala
class KafkaController(val config: KafkaConfig, // kafka配置信息
                      zkClient: KafkaZkClient, // zk客户端，与zk交互
                      time: Time, 
                      metrics: Metrics,
                      initialBrokerInfo: BrokerInfo, // broker节点信息
                      initialBrokerEpoch: Long, // 用于隔离old controller的请求
                      tokenManager: DelegationTokenManager, // Delegation token管理的工具类
                      threadNamePrefix: Option[String] = None) 
  extends ControllerEventProcessor with Logging with KafkaMetricsGroup {
  // controller元数据
  val controllerContext = new ControllerContext
  // 通道管理器类，负责Controller向Broker发送请求
  var controllerChannelManager = new ControllerChannelManager(controllerContext, config, time, metrics,
    stateChangeLogger, threadNamePrefix)

  // 线程调度器，当前唯一负责定期执行Leader重选举
  private[controller] val kafkaScheduler = new KafkaScheduler(1)

  // 事件管理器，负责管理事件处理线程
  private[controller] val eventManager = new ControllerEventManager(config.brokerId, this, time,
    controllerContext.stats.rateAndTimeMetrics)

  // 副本状态机，负责副本状态转换
  val replicaStateMachine: ReplicaStateMachine = new ZkReplicaStateMachine(config, stateChangeLogger, controllerContext, zkClient,
    new ControllerBrokerRequestBatch(config, controllerChannelManager, eventManager, controllerContext, stateChangeLogger))
  // 分区状态机，负责分区状态转换
  val partitionStateMachine: PartitionStateMachine = new ZkPartitionStateMachine(config, stateChangeLogger, controllerContext, zkClient,
    new ControllerBrokerRequestBatch(config, controllerChannelManager, eventManager, controllerContext, stateChangeLogger))
  // 主题删除管理器，负责删除主题及日志
  val topicDeletionManager = new TopicDeletionManager(config, controllerContext, replicaStateMachine,
    partitionStateMachine, new ControllerDeletionClient(this, zkClient))
  
  // 监听/controller节点变更的
  private val controllerChangeHandler = new ControllerChangeHandler(eventManager)
  // 监听Broker的数量变更
  private val brokerChangeHandler = new BrokerChangeHandler(eventManager)
  // 监听Broker的数据变更
  private val brokerModificationsHandlers: mutable.Map[Int, BrokerModificationsHandler] = mutable.Map.empty
  // 监听topic数量变更
  private val topicChangeHandler = new TopicChangeHandler(eventManager)
  // 监听主题删除节点/admin/delete_topics的子节点数量变更
  private val topicDeletionHandler = new TopicDeletionHandler(eventManager)
  // 监控主题分区数据变更
  private val partitionModificationsHandlers: mutable.Map[String, PartitionModificationsHandler] = mutable.Map.empty
  // 监听分区副本重分配
  private val partitionReassignmentHandler = new PartitionReassignmentHandler(eventManager)
  // 监听Preferred Leader选举
  private val preferredReplicaElectionHandler = new PreferredReplicaElectionHandler(eventManager)
  // 监听ISR副本集合变更
  private val isrChangeNotificationHandler = new IsrChangeNotificationHandler(eventManager)
  // 监听日志路径变更
  private val logDirEventNotificationHandler = new LogDirEventNotificationHandler(eventManager)

  // 当前Controller的Broker Id
  @volatile private var activeControllerId = -1
  // 离线分区总数 
  @volatile private var offlinePartitionCount = 0
  // 满足Preferred Leader选举条件的总分区数
  @volatile private var preferredReplicaImbalanceCount = 0
  // 总主题数
  @volatile private var globalTopicCount = 0
  // 总主题分区数
  @volatile private var globalPartitionCount = 0
  // 待删除主题数
  @volatile private var topicsToDeleteCount = 0
  // 待删除副本数
  @volatile private var replicasToDeleteCount = 0
  // 暂时无法删除的主题数
  @volatile private var ineligibleTopicsToDeleteCount = 0
  // 暂时无法删除的副本数
  @volatile private var ineligibleReplicasToDeleteCount = 0

  // 单线程调度程序清除过期的令牌
  private val tokenCleanScheduler = new KafkaScheduler(threads = 1, threadNamePrefix = "delegation-token-cleaner")
}
```

#### ControllerChangeHandler监听器
```scala
class ControllerChangeHandler(eventManager: ControllerEventManager) extends ZNodeChangeHandler {
  // ZooKeeper中Controller节点路径，即/controller
  override val path: String = ControllerZNode.path
  
  override def handleCreation(): Unit = eventManager.put(ControllerChange)
  override def handleDeletion(): Unit = eventManager.put(Reelect)
  override def handleDataChange(): Unit = eventManager.put(ControllerChange)
}
  // 如果是ControllerChange事件，仅执行卸任逻辑
  private def processControllerChange(): Unit = {
    maybeResign()
  }
  // 如果是Reelect事件，还需要执行elect方法参与新一轮的选举
  private def processReelect(): Unit = {
    maybeResign()
    elect()
  }
```

#### 选举场景
1. 首次启动
```scala
  def startup() = {
    // 1. 注册ZooKeeper状态变更监听器，用于监听Zookeeper会话过期的
    zkClient.registerStateChangeHandler(new StateChangeHandler {
      override val name: String = StateChangeHandlers.ControllerHandler
      override def afterInitializingSession(): Unit = {
        eventManager.put(RegisterBrokerAndReelect)
      }
      override def beforeInitializingSession(): Unit = {
        val queuedEvent = eventManager.clearAndPut(Expire)
        queuedEvent.awaitProcessing()
      }
    })
    // 2. 放入事件队列
    eventManager.put(Startup)
    // 3. 启动ControllerEventThread线程，开始处理事件队列中的ControllerEvent
    eventManager.start()
  }
  // KafkaController的process方法
  private def processStartup(): Unit = {
    // 注册ControllerChangeHandler ZooKeeper监听器
    zkClient.registerZNodeChangeHandlerAndCheckExistence(controllerChangeHandler)
    // 执行选举
    elect()
  }
```
2. /controller 节点消失
- Broker检测到/controller节点消失时，此时整个集群中没有Controller。
3. /controller 节点数据发生变更
- 如果 Broker 之前是 Controller，那么该 Broker 需要首先执行卸任操作，然后再尝试竞选
- 如果 Broker 之前不是 Controller，那么，该 Broker 直接去竞选新 Controller。

#### 选举处理逻辑
```scala

  private def maybeResign(): Unit = {
    // 判断该Broker之前是否是Controller
    val wasActiveBeforeChange = isActive
    // 注册ControllerChangeHandler监听器
    zkClient.registerZNodeChangeHandlerAndCheckExistence(controllerChangeHandler)
    activeControllerId = zkClient.getControllerId.getOrElse(-1)
    // 如果该Broker之前是Controller，但现在不是Controller
    if (wasActiveBeforeChange && !isActive) {
      // 卸任controller
      onControllerResignation()
    }
  }

  // 卸任controller
  private def onControllerResignation(): Unit = {
    debug("Resigning")
    // de-register listeners
    zkClient.unregisterZNodeChildChangeHandler(isrChangeNotificationHandler.path)
    zkClient.unregisterZNodeChangeHandler(partitionReassignmentHandler.path)
    zkClient.unregisterZNodeChangeHandler(preferredReplicaElectionHandler.path)
    zkClient.unregisterZNodeChildChangeHandler(logDirEventNotificationHandler.path)
    unregisterBrokerModificationsHandler(brokerModificationsHandlers.keySet)

    // shutdown scheduler(取消定期leader重新选举)
    kafkaScheduler.shutdown()
    // 将统计字段全部清0
    offlinePartitionCount = 0
    preferredReplicaImbalanceCount = 0
    globalTopicCount = 0
    globalPartitionCount = 0
    topicsToDeleteCount = 0
    replicasToDeleteCount = 0
    ineligibleTopicsToDeleteCount = 0
    ineligibleReplicasToDeleteCount = 0

    // stop token expiry check scheduler
    if (tokenCleanScheduler.isStarted)
      tokenCleanScheduler.shutdown()

    // 取消分区分配监听器
    unregisterPartitionReassignmentIsrChangeHandlers()
    // 关闭分区状态机
    partitionStateMachine.shutdown()
    // 取消主题变更监听器
    zkClient.unregisterZNodeChildChangeHandler(topicChangeHandler.path)
    // 取消分区变更监听器
    unregisterPartitionModificationsHandlers(partitionModificationsHandlers.keys.toSeq)
    // 取消主题删除监听器
    zkClient.unregisterZNodeChildChangeHandler(topicDeletionHandler.path)
    // 关闭副本状态机
    replicaStateMachine.shutdown()
    // 取消broker变更监听器
    zkClient.unregisterZNodeChildChangeHandler(brokerChangeHandler.path)
    // 关闭Controller通道管理器
    controllerChannelManager.shutdown()
    // 清空元数据
    controllerContext.resetContext()
    info("Resigned")
  }

  private def elect(): Unit = {
    // 获取当前Controller的broker.id，如果Controller不存在标记为-1
    activeControllerId = zkClient.getControllerId.getOrElse(-1)
    // 如果当前Controller已经选出来了，直接返回即可
    if (activeControllerId != -1) {
      debug(s"Broker $activeControllerId has been elected as the controller, so stopping the election process.")
      return
    }

    try {
      // 注册Controller相关信息
      val (epoch, epochZkVersion) = zkClient.registerControllerAndIncrementControllerEpoch(config.brokerId)
      controllerContext.epoch = epoch
      controllerContext.epochZkVersion = epochZkVersion
      activeControllerId = config.brokerId

      info(s"${config.brokerId} successfully elected as the controller. Epoch incremented to ${controllerContext.epoch} " +
        s"and epoch zk version is now ${controllerContext.epochZkVersion}")
      // 执行当选Controller的后续逻辑
      onControllerFailover()
    } catch {
      case e: ControllerMovedException =>
        // 执行卸任
        maybeResign() 

        if (activeControllerId != -1)
          debug(s"Broker $activeControllerId was elected as controller instead of broker ${config.brokerId}", e)
        else
          warn("A controller has been elected but just resigned, this will result in another round of election", e)

      case t: Throwable =>
        error(s"Error while electing or becoming controller on broker ${config.brokerId}. " +
          s"Trigger controller movement immediately", t)
        triggerControllerMove()
    }
  }
```