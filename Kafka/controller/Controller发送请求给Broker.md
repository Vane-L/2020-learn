### Controller会给集群中的所有Broker机器发送网络请求。

#### 发什么？
1. LeaderAndIsrRequest: 告诉Broker相关主题各个分区的Leader副本位于哪台Broker上、ISR中的副本都在哪些Broker上。
2. StopReplicaRequest: 告知指定Broker停止它上面的副本对象，该请求甚至还能删除副本底层的日志数据。主要的场景是分区副本迁移和删除主题。
3. UpdateMetadataRequest: 告知Broker更新元数据缓存。集群上的所有元数据变更首先发生在Controller端，然后再经由这个请求广播给集群上的所有Broker。

##### 抽象类AbstractControlRequest
```scala
public abstract class AbstractControlRequest extends AbstractRequest {
    public static final long UNKNOWN_BROKER_EPOCH = -1L;
    public static abstract class Builder<T extends AbstractRequest> extends AbstractRequest.Builder<T> {
        protected final int controllerId; // controller所在的brokerID
        protected final int controllerEpoch; // controller的版本
        protected final long brokerEpoch; // broker的版本
        protected Builder(ApiKeys api, short version, int controllerId, int controllerEpoch, long brokerEpoch) {
            super(api, version);
            this.controllerId = controllerId;
            this.controllerEpoch = controllerEpoch;
            this.brokerEpoch = brokerEpoch;
        }
    }
    protected AbstractControlRequest(ApiKeys api, short version) {
        super(api, version);
    }
    public abstract int controllerId();

    public abstract int controllerEpoch();

    public abstract long brokerEpoch();
}

public class LeaderAndIsrRequest extends AbstractControlRequest 
public class StopReplicaRequest extends AbstractControlRequest 
public class UpdateMetadataRequest extends AbstractControlRequest 

LEADER_AND_ISR(4, "LeaderAndIsr", true, LeaderAndIsrRequestData.SCHEMAS, LeaderAndIsrResponseData.SCHEMAS)
STOP_REPLICA(5, "StopReplica", true, StopReplicaRequestData.SCHEMAS, StopReplicaResponseData.SCHEMAS)
UPDATE_METADATA(6, "UpdateMetadata", true, UpdateMetadataRequestData.SCHEMAS, UpdateMetadataResponseData.SCHEMAS)
```

#### 怎么发？
- Controller为集群中的每个Broker都创建一个对应的RequestSendThread线程。
- Broker从RequestSendThread线程持续地从阻塞队列中获取待发送的请求。
##### QueueItem
- ApiKeys
- AbstractControlRequest.Builder: "<:"符号，在Scala中表示上边界的意思，即字段request必须是AbstractControlRequest的子类
- AbstractResponse
```scala
case class QueueItem(apiKey: ApiKeys, request: AbstractControlRequest.Builder[_ <: AbstractControlRequest],
                     callback: AbstractResponse => Unit, enqueueTimeMs: Long)                               
```
#### RequestSendThread
###### 参数说明
- controllerId: Controller的BrokerID
- controllerContext: Controller元数据信息
- queue: 请求阻塞队列,BlockingQueue[QueueItem]
- networkClient: 执行发送的网络I/O类
- brokerNode: Broker节点
- config: kafka config
###### 处理流程
- 从阻塞队列获取请求
- 更新监控指标
- 创建连接
- 发送请求并等待结果
- 调用callback
- **只有接收到Response，并执行完回调逻辑之后，该线程才能从阻塞队列中取出下一个待发送请求进行处理**
```scala
class RequestSendThread(val controllerId: Int, 
                        val controllerContext: ControllerContext,
                        val queue: BlockingQueue[QueueItem],
                        val networkClient: NetworkClient,
                        val brokerNode: Node,
                        val config: KafkaConfig,
                        val time: Time,
                        val requestRateAndQueueTimeMetrics: Timer,
                        val stateChangeLogger: StateChangeLogger,
                        name: String) extends ShutdownableThread(name = name) {

  override def doWork(): Unit = {
    def backoff(): Unit = pause(100, TimeUnit.MILLISECONDS)
    // 从阻塞队列获取QueueItem
    val QueueItem(apiKey, requestBuilder, callback, enqueueTimeMs) = queue.take()
    requestRateAndQueueTimeMetrics.update(time.milliseconds() - enqueueTimeMs, TimeUnit.MILLISECONDS)

    var clientResponse: ClientResponse = null
    try {
      var isSendSuccessful = false
      while (isRunning && !isSendSuccessful) {
        try {
          // 如果没有创建与目标Broker的TCP连接或连接暂时不可用
          if (!brokerReady()) {
            isSendSuccessful = false
            backoff()
          }
          else {
            val clientRequest = networkClient.newClientRequest(brokerNode.idString, requestBuilder,
              time.milliseconds(), true)
            // 发送请求并等待接收Response
            clientResponse = NetworkClientUtils.sendAndReceive(networkClient, clientRequest, time)
            isSendSuccessful = true
          }
        } catch {
          case e: Throwable => // if the send was not successful, reconnect to broker and resend the message
            warn(s"Controller $controllerId epoch ${controllerContext.epoch} fails to send request $requestBuilder " +
              s"to broker $brokerNode. Reconnecting to broker.", e)
            networkClient.close(brokerNode.idString)
            isSendSuccessful = false
            backoff()
        }
      }
      if (clientResponse != null) {
        val requestHeader = clientResponse.requestHeader
        val api = requestHeader.apiKey
        // 此Response的请求类型必须是LeaderAndIsrRequest或StopReplicaRequest或UpdateMetadataRequest
        if (api != ApiKeys.LEADER_AND_ISR && api != ApiKeys.STOP_REPLICA && api != ApiKeys.UPDATE_METADATA)
          throw new KafkaException(s"Unexpected apiKey received: $apiKey")

        val response = clientResponse.responseBody
        stateChangeLogger.withControllerEpoch(controllerContext.epoch).trace(s"Received response " +
          s"${response.toString(requestHeader.apiVersion)} for request $api with correlation id " +
          s"${requestHeader.correlationId} sent to broker $brokerNode")
        // callback不为null则处理回调
        if (callback != null) {
          callback(response)
        }
      }
    } catch {
      case e: Throwable =>
        error(s"Controller $controllerId fails to send a request to broker $brokerNode", e)
        // If there is any socket error (eg, socket timeout), the connection is no longer usable and needs to be recreated.
        networkClient.close(brokerNode.idString)
    }
  }
}
```
#### ControllerChannelManager
- 管理Controller与集群Broker之间的连接，并为每个Broker创建RequestSendThread线程实例
- 将要发送的请求放入到指定Broker的阻塞队列中，等待该Broker的RequestSendThread线程进行处理。
```scala
class ControllerChannelManager(controllerContext: ControllerContext,
                               config: KafkaConfig,
                               time: Time,
                               metrics: Metrics,
                               stateChangeLogger: StateChangeLogger,
                               threadNamePrefix: Option[String] = None){
  // broker信息
  protected val brokerStateInfo = new HashMap[Int, ControllerBrokerStateInfo]
  private val brokerLock = new Object

  // 从元数据信息中找到集群的Broker列表，然后依次为它们调用addNewBroker方法，依次启动brokerStateInfo的RequestSendThread线程
  def startup() = {
    controllerContext.liveOrShuttingDownBrokers.foreach(addNewBroker)
    brokerLock synchronized {
      brokerStateInfo.foreach(brokerState => startRequestSendThread(brokerState._1))
    }
  }
  // 对brokerStateInfo的broker依次调用removeExistingBroker方法
  def shutdown() = {
    brokerLock synchronized {
      brokerStateInfo.values.toList.foreach(removeExistingBroker)
    }
  }
  // 发送请求给某个broker
  def sendRequest(brokerId: Int, request: AbstractControlRequest.Builder[_ <: AbstractControlRequest],
                  callback: AbstractResponse => Unit = null): Unit = {
    brokerLock synchronized {
      val stateInfoOpt = brokerStateInfo.get(brokerId)
      stateInfoOpt match {
        case Some(stateInfo) =>
          // 往阻塞队列发送QueueItem
          stateInfo.messageQueue.put(QueueItem(request.apiKey, request, callback, time.milliseconds()))
        case None =>
          warn(s"Not sending request $request to broker $brokerId, since it is offline.")
      }
    }
  }
  // 添加Broker到brokerStateInfo
  def addBroker(broker: Broker): Unit = {
    brokerLock synchronized {
      if (!brokerStateInfo.contains(broker.id)) {
        addNewBroker(broker) // 添加新broker
        startRequestSendThread(broker.id) // 启动broker的RequestSendThread线程
      }
    }
  }

  private def addNewBroker(broker: Broker): Unit = {
    // 创建阻塞队列
    val messageQueue = new LinkedBlockingQueue[QueueItem]
    debug(s"Controller ${config.brokerId} trying to connect to broker ${broker.id}")
    val controllerToBrokerListenerName = config.controlPlaneListenerName.getOrElse(config.interBrokerListenerName)
    val controllerToBrokerSecurityProtocol = config.controlPlaneSecurityProtocol.getOrElse(config.interBrokerSecurityProtocol)
    // 获取Broker节点信息
    val brokerNode = broker.node(controllerToBrokerListenerName)
    val logContext = new LogContext(s"[Controller id=${config.brokerId}, targetBrokerId=${brokerNode.idString}] ")
    val (networkClient, reconfigurableChannelBuilder) = {
      val channelBuilder = ChannelBuilders.clientChannelBuilder( controllerToBrokerSecurityProtocol, 
        JaasContext.Type.SERVER, config, controllerToBrokerListenerName,config.saslMechanismInterBrokerProtocol, 
        time, config.saslInterBrokerHandshakeRequestEnable, logContext )
      val reconfigurableChannelBuilder = channelBuilder match {
        case reconfigurable: Reconfigurable =>
          config.addReconfigurable(reconfigurable)
          Some(reconfigurable)
        case _ => None
      }
      // 创建NIO Selector用于网络数据传输
      val selector = new Selector(NetworkReceive.UNLIMITED, Selector.NO_IDLE_TIMEOUT_MS, metrics, time,
        "controller-channel", Map("broker-id" -> brokerNode.idString).asJava, false, channelBuilder, logContext)
      // 创建NetworkClient实例
      val networkClient = new NetworkClient(selector, new ManualMetadataUpdater(Seq(brokerNode).asJava),
        config.brokerId.toString, 1, 0, 0, Selectable.USE_DEFAULT_BUFFER_SIZE, Selectable.USE_DEFAULT_BUFFER_SIZE,
        config.requestTimeoutMs, ClientDnsLookup.DEFAULT, time, false, new ApiVersions, logContext)
      (networkClient, reconfigurableChannelBuilder)
    }
    val threadName = threadNamePrefix match {
      case None => s"Controller-${config.brokerId}-to-broker-${broker.id}-send-thread"
      case Some(name) => s"$name:Controller-${config.brokerId}-to-broker-${broker.id}-send-thread"
    }

    val requestRateAndQueueTimeMetrics = newTimer(
      RequestRateAndQueueTimeMetricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS, brokerMetricTags(broker.id)
    )
    // 创建RequestSendThread实例
    val requestThread = new RequestSendThread(config.brokerId, controllerContext, messageQueue, networkClient,
      brokerNode, config, time, requestRateAndQueueTimeMetrics, stateChangeLogger, threadName)
    requestThread.setDaemon(false)

    val queueSizeGauge = newGauge(QueueSizeMetricName, () => messageQueue.size, brokerMetricTags(broker.id))
    // 创建ControllerBrokerStateInfo实例并将其加入到brokerStateInfo统一管理
    brokerStateInfo.put(broker.id, ControllerBrokerStateInfo(networkClient, brokerNode, messageQueue,
      requestThread, queueSizeGauge, requestRateAndQueueTimeMetrics, reconfigurableChannelBuilder))
  }

  protected def startRequestSendThread(brokerId: Int): Unit = {
    val requestThread = brokerStateInfo(brokerId).requestSendThread
    if (requestThread.getState == Thread.State.NEW)
      requestThread.start()
  }

  // 移除broker
  def removeBroker(brokerId: Int): Unit = {
    brokerLock synchronized {
      removeExistingBroker(brokerStateInfo(brokerId))
    }
  }
  // 先requestSendThread.shutdown，再networkClient.close
  private def removeExistingBroker(brokerState: ControllerBrokerStateInfo): Unit = {
    try {
      brokerState.reconfigurableChannelBuilder.foreach(config.removeReconfigurable)
      brokerState.requestSendThread.shutdown()
      brokerState.networkClient.close()
      brokerState.messageQueue.clear()
      removeMetric(QueueSizeMetricName, brokerMetricTags(brokerState.brokerNode.id))
      removeMetric(RequestRateAndQueueTimeMetricName, brokerMetricTags(brokerState.brokerNode.id))
      brokerStateInfo.remove(brokerState.brokerNode.id)
    } catch {
      case e: Throwable => error("Error while removing broker by the controller", e)
    }
  }
}
case class ControllerBrokerStateInfo(networkClient: NetworkClient, // 发送网络I/O的client
                                     brokerNode: Node, // 目标broker节点，发给谁
                                     messageQueue: BlockingQueue[QueueItem], // 请求消息阻塞队列，发什么
                                     requestSendThread: RequestSendThread, // 发送请求的线程，怎么发
                                     queueSizeGauge: Gauge[Int], 
                                     requestRateAndTimeMetrics: Timer,
                                     reconfigurableChannelBuilder: Option[Reconfigurable])
```
