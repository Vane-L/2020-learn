### KafkaRequestHandler
- id: I/O线程序号
- brokerId: Broker序号，即*broker.id*值
- totalHandlerThreads: I/O线程池大小，即*num.io.threads*值
- requestChannel: 请求处理通道
- apis: 真正实现请求处理逻辑的类
```scala
class KafkaRequestHandler(id: Int,
                          brokerId: Int,
                          val aggregateIdleMeter: Meter,
                          val totalHandlerThreads: AtomicInteger,
                          val requestChannel: RequestChannel,
                          apis: KafkaApis,
                          time: Time) extends Runnable

  def run(): Unit = {
    while (!stopped) {
      val startSelectTime = time.nanoseconds
      val req = requestChannel.receiveRequest(300)
      val endTime = time.nanoseconds
      val idleTime = endTime - startSelectTime
      aggregateIdleMeter.mark(idleTime / totalHandlerThreads.get)

      req match {
        // 关闭线程请求
        case RequestChannel.ShutdownRequest =>
          debug(s"Kafka request handler $id on broker $brokerId received shut down command")
          shutdownComplete.countDown()
          return
        // 普通请求
        case request: RequestChannel.Request =>
          try {
            // 更新移出请求队列时间
            request.requestDequeueTimeNanos = endTime
            trace(s"Kafka request handler $id on broker $brokerId handling request $request")
            // 真正处理请求!
            apis.handle(request)
          } catch {
            case e: FatalExitError => // 出现严重错误，立即关闭线程
              shutdownComplete.countDown()
              Exit.exit(e.statusCode)
            case e: Throwable => error("Exception when handling request", e)
          } finally {
            request.releaseBuffer() // 释放请求对象占用的内存缓冲区资源
          }
        case null => // continue
      }
    }
    shutdownComplete.countDown()
  }
```

### KafkaRequestHandlerPool
- brokerId: Broker序号，即*broker.id*值
- requestChannel: 请求处理通道
- apis: 真正实现请求处理逻辑的类
- numThreads: I/O线程池**初始大小**，即*num.io.threads*值
```scala
class KafkaRequestHandlerPool(val brokerId: Int,
                              val requestChannel: RequestChannel,
                              val apis: KafkaApis,
                              time: Time,
                              numThreads: Int,
                              requestHandlerAvgIdleMetricName: String,
                              logAndThreadNamePrefix : String)
  // I/O线程池初始大小，支持动态修改
  private val threadPoolSize: AtomicInteger = new AtomicInteger(numThreads)
  // I/O线程池
  val runnables = new mutable.ArrayBuffer[KafkaRequestHandler](numThreads)

  def createHandler(id: Int): Unit = synchronized {
    // 创建KafkaRequestHandler并加入到runnables
    runnables += new KafkaRequestHandler(id, brokerId, aggregateIdleMeter, threadPoolSize, requestChannel, apis, time)
    // 启动KafkaRequestHandler线程
    KafkaThread.daemon(logAndThreadNamePrefix + "-kafka-request-handler-" + id, runnables(id)).start()
  }

  // 重新设置I/O线程池数
  def resizeThreadPool(newSize: Int): Unit = synchronized {
    val currentSize = threadPoolSize.get
    info(s"Resizing request handler thread pool size from $currentSize to $newSize")
    if (newSize > currentSize) { // 如果newSize大于当前线程数，新建handler
      for (i <- currentSize until newSize) {
        createHandler(i)
      }
    } else if (newSize < currentSize) { // 如果newSize小于当前线程数，从runnables移除handler并stop
      for (i <- 1 to (currentSize - newSize)) {
        runnables.remove(currentSize - i).stop()
      }
    }
    // 更新threadPoolSize
    threadPoolSize.set(newSize)
  }

  def shutdown(): Unit = synchronized {
    info("shutting down")
    for (handler <- runnables)
      handler.initiateShutdown() // requestChannel.sendShutdownRequest()
    for (handler <- runnables)
      handler.awaitShutdown() // shutdownComplete.await()
    info("shut down completely")
  }
```