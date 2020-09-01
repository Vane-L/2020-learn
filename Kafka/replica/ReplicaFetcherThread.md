### ReplicaFetcherThread
```scala
class ReplicaFetcherThread(name: String,
                           fetcherId: Int, // Follower 拉取的线程 Id，配置项num.replica.fetchers
                           sourceBroker: BrokerEndPoint,
                           brokerConfig: KafkaConfig,
                           failedPartitions: FailedPartitions,
                           replicaMgr: ReplicaManager, // 副本管理器
                           metrics: Metrics,
                           time: Time,
                           quota: ReplicaQuota,
                           leaderEndpointBlockingSend: Option[BlockingSend] = None) // 用于实现同步发送请求的类
                           extends AbstractFetcherThread(...) {

  // 副本所在Broker的Id
  private val replicaId = brokerConfig.brokerId

  // 用于执行请求发送的类
  private val leaderEndpoint = leaderEndpointBlockingSend.getOrElse(
          new ReplicaFetcherBlockingSend(sourceBroker, brokerConfig, metrics, time, fetcherId, s"broker-$replicaId-fetcher-$fetcherId", logContext))

  private val maxWait = brokerConfig.replicaFetchWaitMaxMs // Follower 发送的 FETCH 请求被处理返回前的最长等待时间
  private val minBytes = brokerConfig.replicaFetchMinBytes // 每个 FETCH Response 返回前必须要累积的最少字节数
  private val maxBytes = brokerConfig.replicaFetchResponseMaxBytes // 每个合法 FETCH Response 的最大字节数
  private val fetchSize = brokerConfig.replicaFetchMaxBytes // 单个分区能够获取到的最大字节数

  // process fetched data
  override def processPartitionData(topicPartition: TopicPartition,
                                    fetchOffset: Long,
                                    partitionData: FetchData): Option[LogAppendInfo] = {
    // 从replicaMgr获取指定主题的online分区
    val partition = replicaMgr.nonOfflinePartition(topicPartition).get
    // 获取日志对象
    val log = partition.localLogOrException
    val records = toMemoryRecords(partitionData.records)

    maybeWarnIfOversizedRecords(records, topicPartition)

    if (fetchOffset != log.logEndOffset)
      throw new IllegalStateException("Offset mismatch for partition %s: fetched offset = %d, log end offset = %d.".format(
        topicPartition, fetchOffset, log.logEndOffset))

    if (isTraceEnabled)
      trace("Follower has replica log end offset %d for partition %s. Received %d messages and leader hw %d"
        .format(log.logEndOffset, topicPartition, records.sizeInBytes, partitionData.highWatermark))

    // 主要逻辑：将leader的日志追加到Follower本地日志
    val logAppendInfo = partition.appendRecordsToFollowerOrFutureReplica(records, isFuture = false)

    if (isTraceEnabled)
      trace("Follower has replica log end offset %d after appending %d bytes of messages for partition %s"
        .format(log.logEndOffset, records.sizeInBytes, topicPartition))
    val leaderLogStartOffset = partitionData.logStartOffset

    // 更新Follower副本的高水位值
    val followerHighWatermark = log.updateHighWatermark(partitionData.highWatermark)
    // 尝试更新Follower副本的Log Start Offset值
    log.maybeIncrementLogStartOffset(leaderLogStartOffset)
    if (isTraceEnabled)
      trace(s"Follower set replica high watermark for partition $topicPartition to $followerHighWatermark")

    // Traffic from both in-sync and out of sync replicas are accounted for in replication quota to ensure total replication
    // traffic doesn't exceed quota.
    if (quota.isThrottled(topicPartition))
      quota.record(records.sizeInBytes)

    if (partition.isReassigning && partition.isAddingLocalReplica)
      brokerTopicStats.updateReassignmentBytesIn(records.sizeInBytes)

    brokerTopicStats.updateReplicationBytesIn(records.sizeInBytes)

    logAppendInfo
  }

  override def buildFetch(partitionMap: Map[TopicPartition, PartitionFetchState]): ResultWithPartitions[Option[ReplicaFetch]] = {
    val partitionsWithError = mutable.Set[TopicPartition]()
    val builder = fetchSessionHandler.newBuilder(partitionMap.size, false)
    partitionMap.foreach { case (topicPartition, fetchState) =>
      // 如果消息可获取且副本没有超限
      if (fetchState.isReadyForFetch && !shouldFollowerThrottle(quota, fetchState, topicPartition)) {
        try {
          val logStartOffset = this.logStartOffset(topicPartition)
          builder.add(topicPartition, new FetchRequest.PartitionData(
            fetchState.fetchOffset, logStartOffset, fetchSize, Optional.of(fetchState.currentLeaderEpoch)))
        } catch {
          case _: KafkaStorageException =>
            // The replica has already been marked offline due to log directory failure and the original failure should have already been logged.
            // This partition should be removed from ReplicaFetcherThread soon by ReplicaManager.handleLogDirFailure()
            partitionsWithError += topicPartition
        }
      }
    }

    val fetchData = builder.build()
    val fetchRequestOpt = if (fetchData.sessionPartitions.isEmpty && fetchData.toForget.isEmpty) {
      None
    } else {
      // 构造FETCH请求的Builder对象
      val requestBuilder = FetchRequest.Builder
        .forReplica(fetchRequestVersion, replicaId, maxWait, minBytes, fetchData.toSend)
        .setMaxBytes(maxBytes)
        .toForget(fetchData.toForget)
        .metadata(fetchData.metadata)
      Some(ReplicaFetch(fetchData.sessionPartitions(), requestBuilder))
    }

    ResultWithPartitions(fetchRequestOpt, partitionsWithError)
  }

  override def truncate(tp: TopicPartition, offsetTruncationState: OffsetTruncationState): Unit = {
    val partition = replicaMgr.nonOfflinePartition(tp).get
    val log = partition.localLogOrException
    // 执行截断操作，截断到的位置由offsetTruncationState的offset指定
    partition.truncateTo(offsetTruncationState.offset, isFuture = false)

    if (offsetTruncationState.offset < log.highWatermark)
      warn(s"Truncating $tp to offset ${offsetTruncationState.offset} below high watermark " +
        s"${log.highWatermark}")

    // mark the future replica for truncation only when we do last truncation
    if (offsetTruncationState.truncationCompleted)
      replicaMgr.replicaAlterLogDirsManager.markPartitionsForTruncation(brokerConfig.brokerId, tp,
        offsetTruncationState.offset)
  }
}
```