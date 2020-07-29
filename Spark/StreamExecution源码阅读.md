### StreamExecution.scala

#### StreamExecution生命周期状态
- INITIALIZING
- ACTIVE 
- TERMINATED
- RECONFIGURING

#### committedOffsets
- 用于记录已处理/已提交的data
- 只能被scheduler thread修改这个值，并且只能原子步骤
- 如果其他线程需要访问这个值时，应该进行浅复制
```
  @volatile
  var committedOffsets = new StreamProgress
```

####  availableOffsets
- 用于记录可用的offset，但不提交到sink
- 只能被scheduler thread修改这个值，并且只能原子步骤
- 如果其他线程需要访问这个值时，应该进行浅复制
```
  @volatile
  var availableOffsets = new StreamProgress
```

#### queryExecutionThread
- 运行stream的微批处理的线程
```
  val queryExecutionThread: QueryExecutionThread =
    new QueryExecutionThread(s"stream execution thread for $prettyIdString") {
      override def run(): Unit = {
        // To fix call site like "run at <unknown>:0", we bridge the call site from the caller
        // thread to this micro batch thread
        sparkSession.sparkContext.setCallSite(callSite)
        runStream()
      }
    }
```

#### offsetLog
`val offsetLog = new OffsetSeqLog(sparkSession, checkpointFile("offsets"))`
- A write-ahead-log that records the offsets that are present in each batch.
- 为了确保批处理包含相同的数据，需要在任何处理**之前**写入日志
- 第N个记录为正在处理的，第N-1个记录为已经提交到sink

#### commitLog
`val commitLog = new CommitLog(sparkSession, checkpointFile("commits"))`
- 记录已经完成的batch id的日志
- 用来检查batch是否完全处理并提交到sink，如果已经有记录则无需再重新处理
- 主要用于实例重启和故障恢复，帮助确定下一个执行的batch

