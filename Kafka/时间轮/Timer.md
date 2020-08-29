#### Timer 接口及 SystemTimer
```scala
trait Timer {
  def add(timerTask: TimerTask): Unit
  def advanceClock(timeoutMs: Long): Boolean
  def size: Int
  def shutdown(): Unit
}

@threadsafe
class SystemTimer(executorName: String,
                  tickMs: Long = 1,
                  wheelSize: Int = 20,
                  startMs: Long = Time.SYSTEM.hiResClockMs) extends Timer {

  // 单线程的线程池用于异步执行定时任务
  private[this] val taskExecutor = Executors.newFixedThreadPool(1,
    (runnable: Runnable) => KafkaThread.nonDaemon("executor-" + executorName, runnable))
  // 延迟队列保存所有TimerTaskList对象
  private[this] val delayQueue = new DelayQueue[TimerTaskList]()
  // 总定时任务数
  private[this] val taskCounter = new AtomicInteger(0)
  // 时间轮对象
  private[this] val timingWheel = new TimingWheel(
    tickMs = tickMs,
    wheelSize = wheelSize,
    startMs = startMs,
    taskCounter = taskCounter,
    delayQueue
  )

  private[this] val readWriteLock = new ReentrantReadWriteLock()
  private[this] val readLock = readWriteLock.readLock()
  private[this] val writeLock = readWriteLock.writeLock()

  def add(timerTask: TimerTask): Unit = {
    // 获取读锁
    readLock.lock()
    try {
      // 在没有线程持有写锁的前提下，多个线程能够同时向时间轮添加定时任务
      addTimerTaskEntry(new TimerTaskEntry(timerTask, timerTask.delayMs + Time.SYSTEM.hiResClockMs))
    } finally {
      readLock.unlock()
    }
  }

  private def addTimerTaskEntry(timerTaskEntry: TimerTaskEntry): Unit = {
    // 如果timerTaskEntry已经过期或取消，则返回false
    if (!timingWheel.add(timerTaskEntry)) {

      if (!timerTaskEntry.cancelled)
        // 如果该任务已经过期，则提交到相应的线程池，等待后续执行
        taskExecutor.submit(timerTaskEntry.timerTask)
    }
  }

  private[this] val reinsert = (timerTaskEntry: TimerTaskEntry) => addTimerTaskEntry(timerTaskEntry)

  def advanceClock(timeoutMs: Long): Boolean = {
    // 获取delayQueue中下一个已过期的Bucket
    var bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS)
    if (bucket != null) {
      // 获取写锁(一旦有线程持有写锁，其他任何线程执行add或advanceClock方法时会阻塞)
      writeLock.lock()
      try {
        while (bucket != null) {
          // 推动时间轮向前"滚动"到Bucket的过期时间点
          timingWheel.advanceClock(bucket.getExpiration())
          // 将该Bucket下的所有定时任务重写回到时间轮
          bucket.flush(reinsert)
          // 读取下一个Bucket对象
          bucket = delayQueue.poll()
        }
      } finally {
        writeLock.unlock()
      }
      true
    } else {
      false
    }
  }

  def size: Int = taskCounter.get

  override def shutdown(): Unit = {
    taskExecutor.shutdown()
  }

}
```
#### DelayedOperation类
```scala

abstract class DelayedOperation(override val delayMs: Long,
                                lockOpt: Option[Lock] = None)
  extends TimerTask with Logging {
  // 标识该延迟操作是否完成
  private val completed = new AtomicBoolean(false)
  // 防止多个线程同时检查操作是否可完成时发生锁竞争导致操作最终超时
  private val tryCompletePending = new AtomicBoolean(false)
  // Visible for testing
  private[server] val lock: Lock = lockOpt.getOrElse(new ReentrantLock)

  override def run(): Unit = {
    if (forceComplete())
      onExpiration()
  }

  // Two trigger
  // 1. The operation has been verified to be completable inside tryComplete()
  // 2. The operation has expired and hence needs to be completed right now
  def forceComplete(): Boolean = {
    if (completed.compareAndSet(false, true)) {
      // cancel the timeout timer
      cancel()
      onComplete()
      true
    } else {
      false
    }
  }
  // 检查延迟操作是否已经完成
  def isCompleted: Boolean = completed.get()
  // 强制完成之后执行的过期逻辑回调方法
  def onExpiration(): Unit
  // 完成延迟操作所需的处理逻辑
  def onComplete(): Unit
  // execute the completion logic by calling forceComplete()
  def tryComplete(): Boolean

  // 线程安全版本的 tryComplete 方法
  private[server] def maybeTryComplete(): Boolean = {
    var retry = false
    var done = false
    do {
      if (lock.tryLock()) {
        try {
          tryCompletePending.set(false)
          done = tryComplete()
        } finally {
          lock.unlock()
        }
        // 如果其他线程将maybeTryComplete设置为true，那么retry=true，这就相当于其他线程给了本线程重试的机会
        retry = tryCompletePending.get()
      } else {
        // 设置tryCompletePending=true给持有锁的线程一个重试的机会
        retry = !tryCompletePending.getAndSet(true)
      }
    } while (!isCompleted && retry)
    done
  }
}
```

#### DelayedOperationPurgatory类
```scala

final class DelayedOperationPurgatory[T <: DelayedOperation](purgatoryName: String,
                                                             timeoutTimer: Timer,
                                                             brokerId: Int = 0,
                                                             purgeInterval: Int = 1000, // 用于控制删除线程移除 Bucket 中的过期延迟请求的频率
                                                             reaperEnabled: Boolean = true,
                                                             timerEnabled: Boolean = true)
        extends Logging with KafkaMetricsGroup {

  /* a list of operation watching keys */
  private class WatcherList {
    val watchersByKey = new Pool[Any, Watchers](Some((key: Any) => new Watchers(key)))
    val watchersLock = new ReentrantLock()
    def allWatchers = {
      watchersByKey.values
    }
  }

  private val watcherLists = Array.fill[WatcherList](DelayedOperationPurgatory.Shards)(new WatcherList)
  private def watcherList(key: Any): WatcherList = {
    watcherLists(Math.abs(key.hashCode() % watcherLists.length))
  }

  private[this] val estimatedTotalOperations = new AtomicInteger(0)
  /* background thread expiring operations that have timed out */
  private val expirationReaper = new ExpiredOperationReaper()
  if (reaperEnabled)
    expirationReaper.start()

  def tryCompleteElseWatch(operation: T, watchKeys: Seq[Any]): Boolean = {
    assert(watchKeys.nonEmpty, "The watch key list can't be empty")
    
    var isCompletedByMe = operation.tryComplete()
    if (isCompletedByMe)
      return true

    var watchCreated = false
    for(key <- watchKeys) {
      // If the operation is already completed, stop adding it to the rest of the watcher list.
      if (operation.isCompleted)
        return false
      // 否则，将该operation加入到Key所在的WatcherList
      watchForOperation(key, operation)

      // 设置watchCreated标记，表明该任务已经被加入到WatcherList
      if (!watchCreated) {
        watchCreated = true
        // 更新Purgatory中总请求数
        estimatedTotalOperations.incrementAndGet()
      }
    }
    // 再次尝试完成该延迟请求
    isCompletedByMe = operation.maybeTryComplete()
    if (isCompletedByMe)
      return true

    // 如果依然不能完成此请求，将其加入到过期队列
    if (!operation.isCompleted) {
      if (timerEnabled)
        timeoutTimer.add(operation)
      if (operation.isCompleted) {
        // cancel the timer task
        operation.cancel()
      }
    }

    false
  }

  def checkAndComplete(key: Any): Int = {
    // 获取给定Key的WatcherList
    val wl = watcherList(key)
    // 获取WatcherList中Key对应的Watchers对象实例
    val watchers = inLock(wl.watchersLock) { wl.watchersByKey.get(key) }
    val numCompleted = if (watchers == null)
      0
    else
      // 尝试完成那些已满足完成条件的延迟请求
      watchers.tryCompleteWatched()
    debug(s"Request key $key unblocked $numCompleted $purgatoryName operations")
    numCompleted
  }

  // Return the total size of watch lists the purgatory
  def watched: Int = {
    watcherLists.foldLeft(0) { case (sum, watcherList) => sum + watcherList.allWatchers.map(_.countWatched).sum }
  }

  // Remove the key from watcher lists if its list is empty
  private def removeKeyIfEmpty(key: Any, watchers: Watchers): Unit = {
    val wl = watcherList(key)
    inLock(wl.watchersLock) {
      // if the current key is no longer correlated to the watchers to remove, skip
      if (wl.watchersByKey.get(key) != watchers)
        return

      if (watchers != null && watchers.isEmpty) {
        wl.watchersByKey.remove(key)
      }
    }
  }

  def advanceClock(timeoutMs: Long): Unit = {
    timeoutTimer.advanceClock(timeoutMs)

    // Trigger a purge if the number of completed but still being watched operations is larger than
    // the purge threshold. That number is computed by the difference btw the estimated total number of
    // operations and the number of pending delayed operations.
    if (estimatedTotalOperations.get - numDelayed > purgeInterval) {
      // now set estimatedTotalOperations to delayed (the number of pending operations) since we are going to
      // clean up watchers. Note that, if more operations are completed during the clean up, we may end up with
      // a little overestimated total number of operations.
      estimatedTotalOperations.getAndSet(numDelayed)
      debug("Begin purging watch lists")
      val purged = watcherLists.foldLeft(0) {
        case (sum, watcherList) => sum + watcherList.allWatchers.map(_.purgeCompleted()).sum
      }
      debug("Purged %d elements from watch lists.".format(purged))
    }
  }

  private class ExpiredOperationReaper extends ShutdownableThread("ExpirationReaper-%d-%s".format(brokerId, purgatoryName),false) {
    override def doWork(): Unit = {
      advanceClock(200L)
    }
  }
}
```