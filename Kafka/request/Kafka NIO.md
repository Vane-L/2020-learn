### 网络通信
#### SocketServer
- 主要实现了 Reactor 模式，用于处理外部多个 Clients 的并发请求，并负责将处理结果封装进 Response 中，返还给 Clients。

- SocketServer: 对其他组件的管理和操作
- AbstractServerThread: 抽象基类
- Acceptor: 接收和创建外部 TCP 连接的线程
    - endPoint: broker的信息
    - sendBufferSize: 设置的是 SocketOptions 的 SO_SNDBUF，即用于设置出站（Outbound）网络 I/O 的底层缓冲区大小。该值默认是 Broker 端参数 **socket.send.buffer.bytes** 的值，即 100KB。 
    - recvBufferSize: 设置的是 SocketOptions 的 SO_RCVBUF，即用于设置入站（Inbound）网络 I/O 的底层缓冲区大小。该值默认是 Broker 端参数 **socket.receive.buffer.bytes** 的值，即 100KB。 
```scala
private[kafka] class Acceptor(val endPoint: EndPoint,
                              val sendBufferSize: Int,
                              val recvBufferSize: Int,
                              brokerId: Int,
                              connectionQuotas: ConnectionQuotas,
                              metricPrefix: String) extends AbstractServerThread(connectionQuotas)
  private val nioSelector = NSelector.open()
  private val processors = new ArrayBuffer[Processor]()

  def run(): Unit = {
    // 注册OP_ACCEPT事件，只用于接收websocket的请求
    serverChannel.register(nioSelector, SelectionKey.OP_ACCEPT)
    startupComplete() // 等待Acceptor线程启动完成
    try {
      var currentProcessorIndex = 0
      while (isRunning) {
        try {
          val ready = nioSelector.select(500)
          if (ready > 0) {  // 如果有I/O事件准备就绪
            val keys = nioSelector.selectedKeys()
            val iter = keys.iterator()
            while (iter.hasNext && isRunning) {
              try {
                val key = iter.next
                iter.remove()
                if (key.isAcceptable) {
                  // accept方法创建socket连接
                  accept(key).foreach { socketChannel =>
                    // Assign the channel to the next processor (using round-robin) to which the
                    // channel can be added without blocking. If newConnections queue is full on
                    // all processors, block until the last one is able to accept a connection.
                    var retriesLeft = synchronized(processors.length)
                    var processor: Processor = null
                    do {
                      retriesLeft -= 1
                      // 指定processor线程
                      processor = synchronized {
                        // adjust the index (if necessary) and retrieve the processor atomically for
                        // correct behaviour in case the number of processors is reduced dynamically
                        currentProcessorIndex = currentProcessorIndex % processors.length
                        processors(currentProcessorIndex)
                      }
                      currentProcessorIndex += 1
                    } while (!assignNewConnection(socketChannel, processor, retriesLeft == 0))
                  }
                } else
                  throw new IllegalStateException("Unrecognized key state for acceptor thread.")
              } catch {
                case e: Throwable => error("Error while accepting connection", e)
              }
            }
          }
        }
        catch {
          // We catch all the throwables to prevent the acceptor thread from exiting on exceptions due
          // to a select operation on a specific channel or a bad request. We don't want
          // the broker to stop responding to requests from other clients in these scenarios.
          case e: ControlThrowable => throw e
          case e: Throwable => error("Error occurred", e)
        }
      }
    } finally {
      debug("Closing server socket and selector.")
      CoreUtils.swallow(serverChannel.close(), this, Level.ERROR)
      CoreUtils.swallow(nioSelector.close(), this, Level.ERROR)
      shutdownComplete()
    }
  }
```
- Processor: 处理单个 TCP 连接上所有请求的线程
```scala

private[kafka] class Processor(val id: Int,
                               time: Time,
                               maxRequestSize: Int,
                               requestChannel: RequestChannel,
                               connectionQuotas: ConnectionQuotas,
                               connectionsMaxIdleMs: Long,
                               failedAuthenticationDelayMs: Int,
                               listenerName: ListenerName,
                               securityProtocol: SecurityProtocol,
                               config: KafkaConfig,
                               metrics: Metrics,
                               credentialProvider: CredentialProvider,
                               memoryPool: MemoryPool,
                               logContext: LogContext,
                               connectionQueueSize: Int = ConnectionQueueSize) extends AbstractServerThread(connectionQuotas)
  // 创建的新连接信息
  private val newConnections = new ArrayBlockingQueue[SocketChannel](connectionQueueSize)
  // 临时Response队列(因为有些 Response 回调逻辑要在 Response 被发送回发送方之后，才能执行，因此需要暂存在一个临时队列里面)
  private val inflightResponses = mutable.Map[String, RequestChannel.Response]()
  // Response队列
  private val responseQueue = new LinkedBlockingDeque[RequestChannel.Response]()

  override def run(): Unit = {
    startupComplete()
    try {
      while (isRunning) {
        try {
          // 创建新连接,selector.register(connectionId(channel.socket), channel)
          configureNewConnections()
          // 发送response
          processNewResponses()
          // 执行NIO的poll，获取准备就绪的IO操作,selector.poll(pollTimeout)
          poll()
          // 将Request放入RequestChannel的requestQueue,requestChannel.sendRequest(req)
          processCompletedReceives()
          // 执行send完成callback的逻辑,response.onComplete.foreach(onComplete => onComplete(send))
          processCompletedSends()
          // 处理连接断开，selector.disconnected.keySet
          processDisconnected()
          // 关闭超过配额限制部分的连接,优先关闭selector.lowestPriorityChannel()
          closeExcessConnections()
        } catch {
          // We catch all the throwables here to prevent the processor thread from exiting. We do this because
          // letting a processor exit might cause a bigger impact on the broker. This behavior might need to be
          // reviewed if we see an exception that needs the entire broker to stop. Usually the exceptions thrown would
          // be either associated with a specific socket channel or a bad request. These exceptions are caught and
          // processed by the individual methods above which close the failing channel and continue processing other
          // channels. So this catch block should only ever see ControlThrowables.
          case e: Throwable => processException("Processor got uncaught exception.", e)
        }
      }
    } finally {
      debug(s"Closing selector - processor $id")
      CoreUtils.swallow(closeAll(), this, Level.ERROR)
      shutdownComplete()
    }
  }
```
- ConnectionQuotas: 控制连接数配额的类
- TooManyConnectionsException: 一个异常类，用于标识连接数配额超限情况。


#### KafkaRequestHandlerPool
- I/O 线程池，定义了若干个 I/O 线程，用于执行真实的请求处理逻辑。