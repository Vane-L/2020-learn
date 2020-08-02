### JobGenerator
-> ssc.start()                             
    -> scheduler.start()                 
        -> jobGenerator.start() 
```scala
  def start(): Unit = synchronized {
    if (eventLoop != null) return // generator has already been started

    // Call checkpointWriter here to initialize it before eventLoop uses it to avoid a deadlock.
    // See SPARK-10125
    checkpointWriter

    eventLoop = new EventLoop[JobGeneratorEvent]("JobGenerator") {
      override protected def onReceive(event: JobGeneratorEvent): Unit = processEvent(event)

      override protected def onError(e: Throwable): Unit = {
        jobScheduler.reportError("Error in job generator", e)
      }
    }
    // 启动 RPC 处理线程
    eventLoop.start()
    // checkpoint是否存在
    if (ssc.isCheckpointPresent) {
      restart()
    } else {
      startFirstTime()
    }
  }
```
checkpoint存在,调用restart方法
- 先处理失败前未完成的batch
- 重启timer线程
```scala
  private def restart(): Unit = {
    if (clock.isInstanceOf[ManualClock]) {
      val lastTime = ssc.initialCheckpoint.checkpointTime.milliseconds
      val jumpTime = ssc.sc.conf.get(StreamingConf.MANUAL_CLOCK_JUMP)
      clock.asInstanceOf[ManualClock].setTime(lastTime + jumpTime)
    }

    val batchDuration = ssc.graph.batchDuration
    // 在checkpointTime和restartTime之间进行批处理    
    val checkpointTime = ssc.initialCheckpoint.checkpointTime
    val restartTime = new Time(timer.getRestartTime(graph.zeroTime.milliseconds))
    val downTimes = checkpointTime.until(restartTime, batchDuration)
    logInfo("Batches during down time (" + downTimes.size + " batches): " + downTimes.mkString(", "))
    // 失败前未处理的batch
    val pendingTimes = ssc.initialCheckpoint.pendingTimes.sorted(Time.ordering)
    logInfo("Batches pending processing (" + pendingTimes.length + " batches): " + pendingTimes.mkString(", "))
    // Reschedule jobs for these times
    val timesToReschedule = (pendingTimes ++ downTimes).filter { _ < restartTime }
      .distinct.sorted(Time.ordering)
    logInfo("Batches to reschedule (" + timesToReschedule.length + " batches): " + timesToReschedule.mkString(", "))
    timesToReschedule.foreach { time =>
      jobScheduler.receiverTracker.allocateBlocksToBatch(time) // allocate received blocks to batch
      jobScheduler.submitJobSet(JobSet(time, graph.generateJobs(time)))
    }

    // Restart the timer
    timer.start(restartTime.milliseconds)
    logInfo("Restarted JobGenerator at " + restartTime)
  }
```

checkpoint不存在,调用startFirstTime方法
- 启动graph线程
- 启动timer线程
```scala
  private def startFirstTime(): Unit = {
    val startTime = new Time(timer.getStartTime())
    graph.start(startTime - graph.batchDuration)
    timer.start(startTime.milliseconds)
    logInfo("Started JobGenerator at " + startTime)
  }
```

generateJobs方法
- eventLoop 收到消息时，会在一个消息处理的线程池里，执行processEvent(event)
- `jobScheduler.receiverTracker.allocateBlocksToBatch(time)` ：receiverTracker将批量分配块到本次batch
    - 每个块数据的 meta 信息，将被划入一个、且只被划入一个 batch
- `graph.generateJobs(time)`：graph生成RDD实例
    - DStreamGraph.generateJobs(time) 遍历结束的返回值是 Seq[Job]
- `jobScheduler.inputInfoTracker.getInfo(time)`：获取本次batch的块数据meta信息
- `jobScheduler.submitJobSet(JobSet(time, jobs, streamIdToInputInfos))`：将jobs提交给 JobScheduler 异步执行
    - 将 time、Seq[job]、块数据的 meta 信息这三者包装为一个 JobSet，然后调用jobScheduler.submitJobSet提交
- `eventLoop.post(DoCheckpoint(time, clearCheckpointDataLater = false))` ：对当前状态执行checkpoint  

```scala
  private def generateJobs(time: Time): Unit = {
    ssc.sparkContext.setLocalProperty(RDD.CHECKPOINT_ALL_MARKED_ANCESTORS, "true")
    Try {
      jobScheduler.receiverTracker.allocateBlocksToBatch(time) // allocate received blocks to batch
      graph.generateJobs(time) // generate jobs using allocated block
    } match {
      case Success(jobs) =>
        val streamIdToInputInfos = jobScheduler.inputInfoTracker.getInfo(time)
        jobScheduler.submitJobSet(JobSet(time, jobs, streamIdToInputInfos))
      case Failure(e) =>
        jobScheduler.reportError("Error generating jobs for time " + time, e)
        PythonDStream.stopStreamingContextIfPythonProcessIsDead(e)
    }
    eventLoop.post(DoCheckpoint(time, clearCheckpointDataLater = false))
  }
```