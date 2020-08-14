### RequestChannel
- queueSize
- requestQueue
- processors
```scala
class RequestChannel(val queueSize: Int, val metricNamePrefix : String) extends KafkaMetricsGroup {
  import RequestChannel._
  val metrics = new RequestChannel.Metrics
  // 请求队列ArrayBlockingQueue
  private val requestQueue = new ArrayBlockingQueue[BaseRequest](queueSize)
  // Processor 线程池 
  private val processors = new ConcurrentHashMap[Int, Processor]()

  def addProcessor(processor: Processor): Unit = {
    if (processors.putIfAbsent(processor.id, processor) != null)
      warn(s"Unexpected processor with processorId ${processor.id}")

    newGauge(responseQueueSizeMetricName, () => processor.responseQueueSize,
      Map(ProcessorMetricTag -> processor.id.toString))
  }

  def removeProcessor(processorId: Int): Unit = {
    processors.remove(processorId)
    removeMetric(responseQueueSizeMetricName, Map(ProcessorMetricTag -> processorId.toString))
  }
  
  // Request
  def sendRequest(request: RequestChannel.Request): Unit = {
    requestQueue.put(request)
  }
  def receiveRequest(timeout: Long): RequestChannel.BaseRequest =
    requestQueue.poll(timeout, TimeUnit.MILLISECONDS)
  def receiveRequest(): RequestChannel.BaseRequest =
    requestQueue.take()
  
  // Response
  def sendResponse(response: RequestChannel.Response): Unit = {
    if (isTraceEnabled) { // 打印日志
      val requestHeader = response.request.header
      val message = response match {
        case sendResponse: SendResponse =>
          s"Sending ${requestHeader.apiKey} response to client ${requestHeader.clientId} of ${sendResponse.responseSend.size} bytes."
        case _: NoOpResponse =>
          s"Not sending ${requestHeader.apiKey} response to client ${requestHeader.clientId} as it's not required."
        case _: CloseConnectionResponse =>
          s"Closing connection for client ${requestHeader.clientId} due to error during ${requestHeader.apiKey}."
        case _: StartThrottlingResponse =>
          s"Notifying channel throttling has started for client ${requestHeader.clientId} for ${requestHeader.apiKey}"
        case _: EndThrottlingResponse =>
          s"Notifying channel throttling has ended for client ${requestHeader.clientId} for ${requestHeader.apiKey}"
      }
      trace(message)
    }

    val processor = processors.get(response.processor)
    // The processor may be null if it was shutdown. In this case, the connections
    // are closed, so the response is dropped.
    if (processor != null) {
      processor.enqueueResponse(response)
    }
  }

}
```

#### Request
- ShutdownRequest 仅仅起到一个标志位的作用。
    - 当 Broker 进程关闭时，请求处理器 RequestHandler 会发送 ShutdownRequest 到专属的请求处理线程。该线程接收到此请求后，会主动触发一系列的 Broker 关闭逻辑。
- Request是Clients和Broker请求的实现类。
    - val processor: Int
        - Processor线程的序号，记录当前请求是被哪个Processor线程接收处理
        - 当 Request 被后面的 I/O 线程处理完成后，还要依靠 Processor 线程发送 Response 给请求发送方，因此，Request 中必须记录它之前是被哪个 Processor 线程接收的。
        - **Processor 线程仅仅是网络接收线程**，不会执行真正的 Request 请求处理逻辑
    - val context: RequestContext
        - 标识请求上下文信息
    - val startTimeNanos: Long
        - 记录Request对象创建时间，主要用于时间统计指标的计算，以纳秒为单位的时间戳
    - memoryPool: MemoryPool
        - 非阻塞的内存缓冲区，主要是避免Request对象无限使用内存
    - var buffer: ByteBuffer
        - 真正保存Request对象内容的字节缓冲区
    - metrics: RequestChannel.Metrics
        - 监控指标的管理类
```scala
  sealed trait BaseRequest  
  case object ShutdownRequest extends BaseRequest

  class Request(val processor: Int,
                val context: RequestContext,
                val startTimeNanos: Long,
                memoryPool: MemoryPool,
                @volatile private var buffer: ByteBuffer,
                metrics: RequestChannel.Metrics) extends BaseRequest
```

#### Response
- SendResponse : 正常需要发送 Response
- NoResponse : 无需发送 Response
- CloseConnectionResponse : 发送 Response 告知关闭 TCP 连接
- StartThrottlingResponse : 通知某个 TCP 连接通信通道开始被限流
- EndThrottlingResponse : 通知某个 TCP 连接通信通道的限流已结束
```scala
  abstract class Response(val request: Request) {
    locally {
      val nowNs = Time.SYSTEM.nanoseconds
      request.responseCompleteTimeNanos = nowNs
      if (request.apiLocalCompleteTimeNanos == -1L)
        request.apiLocalCompleteTimeNanos = nowNs
    }

    def processor: Int = request.processor

    def responseString: Option[String] = Some("")

    def onComplete: Option[Send => Unit] = None

    override def toString: String
  }
```