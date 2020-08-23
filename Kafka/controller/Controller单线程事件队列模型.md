### 单线程事件队列模型
- Controller端有多个线程向**事件队列**写入不同种类的事件
- 在**事件队列**的另一端，只有一个ControllerEventThread线程专门负责处理队列中的事件

#### ControllerEventProcessor
- Controller端的事件处理器接口
```scala
trait ControllerEventProcessor {
  // 接收一个Controller事件，并进行处理
  def process(event: ControllerEvent): Unit 
  // 接收一个Controller事件，并抢占队列之前的事件进行优先处理
  def preempt(event: ControllerEvent): Unit 
}
```
#### ControllerEvent
- ControllerEvent是事件队列中被处理的对象
```scala
sealed trait ControllerEvent {
  def state: ControllerState
}
sealed abstract class ControllerState {
  def value: Byte
  def rateAndTimeMetricName: Option[String] =
    if (hasRateAndTimeMetric) Some(s"${toString}RateAndTimeMs") else None
  protected def hasRateAndTimeMetric: Boolean = true
}
```
#### ControllerEventManager
- 用于创建和管理ControllerEventThread

```scala
object ControllerEventManager {
  val ControllerEventThreadName = "controller-event-thread"
  val EventQueueTimeMetricName = "EventQueueTimeMs"
  val EventQueueSizeMetricName = "EventQueueSize"
}
class ControllerEventManager(controllerId: Int,
                             processor: ControllerEventProcessor,
                             time: Time,
                             rateAndTimeMetrics: Map[ControllerState, KafkaTimer]) extends KafkaMetricsGroup {
  @volatile private var _state: ControllerState = ControllerState.Idle // 初始状态空闲
  private val putLock = new ReentrantLock() // ControllerEvent锁
  private val queue = new LinkedBlockingQueue[QueuedEvent] // ControllerEvent队列
  private[controller] val thread = new ControllerEventThread(ControllerEventThreadName) // ControllerEventThread

  def state: ControllerState = _state

  def start(): Unit = thread.start()

  def close(): Unit = {
    try {
      thread.initiateShutdown()
      clearAndPut(ShutdownEventThread)
      thread.awaitShutdown()
    } finally {
      removeMetric(EventQueueTimeMetricName)
      removeMetric(EventQueueSizeMetricName)
    }
  }
  // 插入到事件队列
  // LinkedBlockingQueue维护了一个putLock和一个takeLock，专门保护读写操作
  def put(event: ControllerEvent): QueuedEvent = inLock(putLock) {
    val queuedEvent = new QueuedEvent(event, time.milliseconds())
    queue.put(queuedEvent)
    queuedEvent
  }
  // 需要先执行高优先级的抢占式事件
  def clearAndPut(event: ControllerEvent): QueuedEvent = inLock(putLock) {
    // 优先处理抢占式事件
    queue.asScala.foreach(_.preempt(processor))
    // 清空事件队列
    queue.clear()
    put(event)
  }
}
```
##### QueuedEvent
- **事件队列**上的事件对象
```scala
class QueuedEvent(val event: ControllerEvent, // 表示Controller事件
                  val enqueueTimeMs: Long) { // 表示Controller事件被放入到事件队列的时间戳
  // 标识事件是否开始被处理
  val processingStarted = new CountDownLatch(1)
  // 标识事件是否被处理过
  val spent = new AtomicBoolean(false)
  // 处理事件
  def process(processor: ControllerEventProcessor): Unit = {
    if (spent.getAndSet(true))
      return
    processingStarted.countDown()
    processor.process(event)
  }
  // 抢占式处理事件
  def preempt(processor: ControllerEventProcessor): Unit = {
    if (spent.getAndSet(true))
      return
    processor.preempt(event)
  }
  // 阻塞等待事件被处理完成
  def awaitProcessing(): Unit = {
    processingStarted.await()
  }
}
```
#### ControllerEventThread
- 专属的事件处理线程，唯一的作用是处理不同种类的ControllerEvent
```scala
  class ControllerEventThread(name: String) extends ShutdownableThread(name = name, isInterruptible = false) {
    logIdent = s"[ControllerEventThread controllerId=$controllerId] "

    override def doWork(): Unit = {
      // 从队列中获取待处理的ControllerEvent，否则等待
      val dequeued = queue.take()
      dequeued.event match {
        case ShutdownEventThread => // 如果是ShutdownEventThread则忽略
        case controllerEvent =>
          _state = controllerEvent.state
          eventQueueTimeHist.update(time.milliseconds() - dequeued.enqueueTimeMs)
          try {
            def process(): Unit = dequeued.process(processor)
            rateAndTimeMetrics.get(state) match {
              case Some(timer) => timer.time { process() }  // process()处理事件
              case None => process()
            }
          } catch {
            case e: Throwable => error(s"Uncaught error processing event $controllerEvent", e)
          }
          _state = ControllerState.Idle
      }
    }
  }
```