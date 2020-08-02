### JobScheduler 是 Spark Streaming 的 Job 总调度者。
JobScheduler 有两个非常重要的成员：JobGenerator 和 ReceiverTracker。
```scala
  def start(): Unit = synchronized {
    if (eventLoop != null) return // scheduler has already been started

    logDebug("Starting JobScheduler")
    eventLoop = new EventLoop[JobSchedulerEvent]("JobScheduler") {
      override protected def onReceive(event: JobSchedulerEvent): Unit = processEvent(event)

      override protected def onError(e: Throwable): Unit = reportError("Error in job scheduler", e)
    }
    eventLoop.start()

    // attach rate controllers of input streams to receive batch completion updates
    for {
      inputDStream <- ssc.graph.getInputStreams
      rateController <- inputDStream.rateController
    } ssc.addStreamingListener(rateController)

    listenerBus.start()
    receiverTracker = new ReceiverTracker(ssc)
    inputInfoTracker = new InputInfoTracker(ssc)

    val executorAllocClient: ExecutorAllocationClient = ssc.sparkContext.schedulerBackend match {
      case b: ExecutorAllocationClient => b.asInstanceOf[ExecutorAllocationClient]
      case _ => null
    }

    executorAllocationManager = ExecutorAllocationManager.createIfEnabled(
      executorAllocClient,
      receiverTracker,
      ssc.conf,
      ssc.graph.batchDuration.milliseconds,
      clock)
    executorAllocationManager.foreach(ssc.addStreamingListener)
    // JobScheduler 将源头输入数据的记录工作委托给 ReceiverTracker。
    receiverTracker.start()
    // JobScheduler 将每个 batch 的 RDD DAG 具体生成工作委托给 JobGenerator
    jobGenerator.start()
    executorAllocationManager.foreach(_.start())
    logInfo("Started JobScheduler")
  }
```

Job运行的线程池jobExecutor
- jobExecutor 的线程池大小，就是能够并行执行的 Job 数。

```scala
private val jobExecutor = ThreadUtils.newDaemonFixedThreadPool(numConcurrentJobs, "streaming-job-executor")

  def submitJobSet(jobSet: JobSet): Unit = {
    if (jobSet.jobs.isEmpty) {
      logInfo("No jobs added for time " + jobSet.time)
    } else {
      listenerBus.post(StreamingListenerBatchSubmitted(jobSet.toBatchInfo))
      jobSets.put(jobSet.time, jobSet)
      jobSet.jobs.foreach(job => jobExecutor.execute(new JobHandler(job)))
      logInfo("Added jobs for time " + jobSet.time)
    }
  }
```

