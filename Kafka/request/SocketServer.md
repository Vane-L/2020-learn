### SocketServer
```scala
class SocketServer(val config: KafkaConfig,
                   val metrics: Metrics,
                   val time: Time,
                   val credentialProvider: CredentialProvider)
  extends Logging with KafkaMetricsGroup with BrokerReconfigurable

object SocketServer {
  val MetricsGroup = "socket-server-metrics"
  val DataPlaneThreadPrefix = "data-plane"
  val ControlPlaneThreadPrefix = "control-plane"
  val DataPlaneMetricPrefix = ""
  val ControlPlaneMetricPrefix = "ControlPlane"
  // 可重新配置
  val ReconfigurableConfigs = Set(
    KafkaConfig.MaxConnectionsPerIpProp,
    KafkaConfig.MaxConnectionsPerIpOverridesProp,
    KafkaConfig.MaxConnectionsProp)

  val ListenerReconfigurableConfigs = Set(KafkaConfig.MaxConnectionsProp)
}
```
#### data-plane
- plane：负责处理数据类请求，这类请求通常不需要高优先级处理。
    - 请求来自:clients和brokers 
    - 线程模型:
        - 每个listener一个Acceptor线程 : 处理新的连接
        - 每个Acceptor有N个Processor线程 : 从sockets读取请求并放入请求队列
        - M个Handler线程 : 处理请求并返回响应
```scala
  // data-plane
  private val dataPlaneProcessors = new ConcurrentHashMap[Int, Processor]()
  private[network] val dataPlaneAcceptors = new ConcurrentHashMap[EndPoint, Acceptor]()
  // 请求队列长度maxQueuedRequests由参数queued.max.requests，默认500
  val dataPlaneRequestChannel = new RequestChannel(maxQueuedRequests, DataPlaneMetricPrefix, time)

  private def createDataPlaneAcceptorsAndProcessors(dataProcessorsPerListener: Int,
                                                    endpoints: Seq[EndPoint]): Unit = {
    endpoints.foreach { endpoint =>
      connectionQuotas.addListener(config, endpoint.listenerName)
      // 为endpoint创建Acceptor
      val dataPlaneAcceptor = createAcceptor(endpoint, DataPlaneMetricPrefix)
      // 为dataPlaneAcceptor创建多个Processors，具体数值由num.network.threads决定
      addDataPlaneProcessors(dataPlaneAcceptor, endpoint, dataProcessorsPerListener)
      dataPlaneAcceptors.put(endpoint, dataPlaneAcceptor)
      info(s"Created data-plane acceptor and processors for endpoint : ${endpoint.listenerName}")
    }
  }


  def startup(startProcessingRequests: Boolean = true): Unit = {
    this.synchronized {
      connectionQuotas = new ConnectionQuotas(config, time)
      createControlPlaneAcceptorAndProcessor(config.controlPlaneListener)
      createDataPlaneAcceptorsAndProcessors(config.numNetworkThreads, config.dataPlaneListeners)
      if (startProcessingRequests) {
        this.startProcessingRequests()
      }
    }
    // 监控指标
    newGauge(s"${DataPlaneMetricPrefix}NetworkProcessorAvgIdlePercent", () => SocketServer.this.synchronized {
      val ioWaitRatioMetricNames = dataPlaneProcessors.values.asScala.iterator.map { p =>
        metrics.metricName("io-wait-ratio", MetricsGroup, p.metricTags)
      }
      ioWaitRatioMetricNames.map { metricName =>
        Option(metrics.metric(metricName)).fold(0.0)(m => Math.min(m.metricValue.asInstanceOf[Double], 1.0))
      }.sum / dataPlaneProcessors.size
    })
    newGauge(s"${ControlPlaneMetricPrefix}NetworkProcessorAvgIdlePercent", () => SocketServer.this.synchronized {
      val ioWaitRatioMetricName = controlPlaneProcessorOpt.map { p =>
        metrics.metricName("io-wait-ratio", "socket-server-metrics", p.metricTags)
      }
      ioWaitRatioMetricName.map { metricName =>
        Option(metrics.metric(metricName)).fold(0.0)(m => Math.min(m.metricValue.asInstanceOf[Double], 1.0))
      }.getOrElse(Double.NaN)
    })
    newGauge("MemoryPoolAvailable", () => memoryPool.availableMemory)
    newGauge("MemoryPoolUsed", () => memoryPool.size() - memoryPool.availableMemory)
    newGauge(s"${DataPlaneMetricPrefix}ExpiredConnectionsKilledCount", () => SocketServer.this.synchronized {
      val expiredConnectionsKilledCountMetricNames = dataPlaneProcessors.values.asScala.iterator.map { p =>
        metrics.metricName("expired-connections-killed-count", "socket-server-metrics", p.metricTags)
      }
      expiredConnectionsKilledCountMetricNames.map { metricName =>
        Option(metrics.metric(metricName)).fold(0.0)(m => m.metricValue.asInstanceOf[Double])
      }.sum
    })
    newGauge(s"${ControlPlaneMetricPrefix}ExpiredConnectionsKilledCount", () => SocketServer.this.synchronized {
      val expiredConnectionsKilledCountMetricNames = controlPlaneProcessorOpt.map { p =>
        metrics.metricName("expired-connections-killed-count", "socket-server-metrics", p.metricTags)
      }
      expiredConnectionsKilledCountMetricNames.map { metricName =>
        Option(metrics.metric(metricName)).fold(0.0)(m => m.metricValue.asInstanceOf[Double])
      }.getOrElse(0.0)
    })
  }

  // Start processing requests and new connections.
  def startProcessingRequests(authorizerFutures: Map[Endpoint, CompletableFuture[Void]] = Map.empty): Unit = {
    info("Starting socket server acceptors and processors")
    this.synchronized {
      if (!startedProcessingRequests) {
        startControlPlaneProcessorAndAcceptor(authorizerFutures)
        startDataPlaneProcessorsAndAcceptors(authorizerFutures)
        startedProcessingRequests = true
      } else {
        info("Socket server acceptors and processors already started")
      }
    }
    info("Started socket server acceptors and processors")
  }
  // 启动ControlPlane
  private def startControlPlaneProcessorAndAcceptor(authorizerFutures: Map[Endpoint, CompletableFuture[Void]]): Unit = {
    controlPlaneAcceptorOpt.foreach { controlPlaneAcceptor =>
      val endpoint = config.controlPlaneListener.get
      startAcceptorAndProcessors(ControlPlaneThreadPrefix, endpoint, controlPlaneAcceptor, authorizerFutures)
    }
  }
  // 启动DataPlane
  private def startDataPlaneProcessorsAndAcceptors(authorizerFutures: Map[Endpoint, CompletableFuture[Void]]): Unit = {
    // 获取Broker的监听器，默认是PLAINTEXT
    val interBrokerListener = dataPlaneAcceptors.asScala.keySet
      .find(_.listenerName == config.interBrokerListenerName)
      .getOrElse(throw new IllegalStateException(s"Inter-broker listener ${config.interBrokerListenerName} not found, endpoints=${dataPlaneAcceptors.keySet}"))
    val orderedAcceptors = List(dataPlaneAcceptors.get(interBrokerListener)) ++
      dataPlaneAcceptors.asScala.filter { case (k, _) => k != interBrokerListener }.values
    orderedAcceptors.foreach { acceptor =>
      val endpoint = acceptor.endPoint
      startAcceptorAndProcessors(DataPlaneThreadPrefix, endpoint, acceptor, authorizerFutures)
    }
  }

  // 启动Acceptor和Processors
  private def startAcceptorAndProcessors(threadPrefix: String,
                                         endpoint: EndPoint,
                                         acceptor: Acceptor,
                                         authorizerFutures: Map[Endpoint, CompletableFuture[Void]] = Map.empty): Unit = {
    debug(s"Wait for authorizer to complete start up on listener ${endpoint.listenerName}")
    waitForAuthorizerFuture(acceptor, authorizerFutures)
    debug(s"Start processors on listener ${endpoint.listenerName}")
    acceptor.startProcessors(threadPrefix)
    debug(s"Start acceptor thread on listener ${endpoint.listenerName}")
    if (!acceptor.isStarted()) {
      KafkaThread.nonDaemon(
        s"${threadPrefix}-kafka-socket-acceptor-${endpoint.listenerName}-${endpoint.securityProtocol}-${endpoint.port}",
        acceptor
      ).start()
      acceptor.awaitStartup()
    }
    info(s"Started $threadPrefix acceptor and processor(s) for endpoint : ${endpoint.listenerName}")
  }
```


#### control-plane
- Control plane：负责处理控制类请求，这类请求需要高优先级处理。
    - 请求来自:controller,可以通过`control.plane.listener.name`配置
    - 线程模型:
        - 一个Acceptor thread : 处理新的连接
        - 一个Processor thread : 从socket读取请求并放入请求队列
        - 一个Handler thread : 处理请求并返回响应
```scala
  // control-plane
  private var controlPlaneProcessorOpt : Option[Processor] = None
  private[network] var controlPlaneAcceptorOpt : Option[Acceptor] = None
  // 代码设置请求队列长度为20
  val controlPlaneRequestChannelOpt: Option[RequestChannel] = config.controlPlaneListenerName.map(_ =>
    new RequestChannel(20, ControlPlaneMetricPrefix, time))

  private def createControlPlaneAcceptorAndProcessor(endpointOpt: Option[EndPoint]): Unit = {
    endpointOpt.foreach { endpoint =>
      connectionQuotas.addListener(config, endpoint.listenerName)
      // 创建Acceptor
      val controlPlaneAcceptor = createAcceptor(endpoint, ControlPlaneMetricPrefix)
      // 创建Processor线程
      val controlPlaneProcessor = newProcessor(nextProcessorId, controlPlaneRequestChannelOpt.get, connectionQuotas, endpoint.listenerName, endpoint.securityProtocol, memoryPool)
      controlPlaneAcceptorOpt = Some(controlPlaneAcceptor)
      controlPlaneProcessorOpt = Some(controlPlaneProcessor)
      val listenerProcessors = new ArrayBuffer[Processor]()
      listenerProcessors += controlPlaneProcessor
      // 将Processor线程添加到controlPlaneRequestChannelOpt中
      controlPlaneRequestChannelOpt.foreach(_.addProcessor(controlPlaneProcessor))
      nextProcessorId += 1
      controlPlaneAcceptor.addProcessors(listenerProcessors, ControlPlaneThreadPrefix)
      info(s"Created control-plane acceptor and processor for endpoint : ${endpoint.listenerName}")
    }
  }
```