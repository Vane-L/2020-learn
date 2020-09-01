### AbstractFetcherThread 抽象类
```scala
abstract class AbstractFetcherThread(name: String, // 线程名
                                     clientId: String,
                                     val sourceBroker: BrokerEndPoint, // 数据源Broker地址，即Leader副本所在的Broker
                                     failedPartitions: FailedPartitions, // 处理过程中报错的分区
                                     fetchBackOffMs: Int = 0, // 获取操作重试的时间间隔，单位为毫秒，配置项replica.fetch.backoff.ms
                                     isInterruptible: Boolean = true, // 线程是否允许被中断
                                     val brokerTopicStats: BrokerTopicStats) //BrokerTopicStats's lifecycle managed by ReplicaManager
  extends ShutdownableThread(name, isInterruptible) {

  // type类似于一个快捷方式
  // 表示获取的消息数据
  type FetchData = FetchResponse.PartitionData[Records]
  // 表示Leader Epoch数据
  type EpochData = OffsetsForLeaderEpochRequest.PartitionData

  // process fetched data
  protected def processPartitionData(topicPartition: TopicPartition, // 读取的目标分区
                                     fetchOffset: Long,       // 读取的目标位移值
                                     partitionData: FetchData // 读取到的分区消息数据
                                     ): Option[LogAppendInfo] // 写入已读取消息数据的元数据
  // 用于把指定分区副本截断到哪个位移值
  protected def truncate(topicPartition: TopicPartition, truncationState: OffsetTruncationState): Unit

  // 构建ReplicaFetch请求
  protected def buildFetch(partitionMap: Map[TopicPartition, PartitionFetchState]): ResultWithPartitions[Option[ReplicaFetch]]
  

  // 循环执行截断操作和获取数据操作
  override def doWork(): Unit = {
    maybeTruncate() // 执行副本截断操作
    maybeFetch()    // 执行消息获取操作
  }

  private def maybeTruncate(): Unit = {
    val (partitionsWithEpochs, partitionsWithoutEpochs) = fetchTruncatingPartitions()
    if (partitionsWithEpochs.nonEmpty) {
      truncateToEpochEndOffsets(partitionsWithEpochs) // 将日志截断到Leader Epoch值对应的位移值处
    }
    if (partitionsWithoutEpochs.nonEmpty) {
      truncateToHighWatermark(partitionsWithoutEpochs) // 将日志截断到高水位值处
    }
  }

  private def truncateToEpochEndOffsets(latestEpochsForPartitions: Map[TopicPartition, EpochData]): Unit = {
    val endOffsets = fetchEpochEndOffsets(latestEpochsForPartitions)
    //Ensure we hold a lock during truncation.
    inLock(partitionMapLock) {
      //Check no leadership and no leader epoch changes happened whilst we were unlocked, fetching epochs
      val epochEndOffsets = endOffsets.filter { case (tp, _) =>
        val curPartitionState = partitionStates.stateValue(tp)
        val partitionEpochRequest = latestEpochsForPartitions.getOrElse(tp, {
          throw new IllegalStateException(
            s"Leader replied with partition $tp not requested in OffsetsForLeaderEpoch request")
        })
        // 获取currentLeaderEpoch
        val leaderEpochInRequest = partitionEpochRequest.currentLeaderEpoch.get
        curPartitionState != null && leaderEpochInRequest == curPartitionState.currentLeaderEpoch
      }

      val ResultWithPartitions(fetchOffsets, partitionsWithError) = maybeTruncateToEpochEndOffsets(epochEndOffsets, latestEpochsForPartitions)
      handlePartitionsWithErrors(partitionsWithError, "truncateToEpochEndOffsets")
      // 更新这组分区的分区读取状态
      updateFetchOffsetAndMaybeMarkTruncationComplete(fetchOffsets)
    }
  }

  private[server] def truncateToHighWatermark(partitions: Set[TopicPartition]): Unit = inLock(partitionMapLock) {
    val fetchOffsets = mutable.HashMap.empty[TopicPartition, OffsetTruncationState]

    for (tp <- partitions) {
      val partitionState = partitionStates.stateValue(tp)
      if (partitionState != null) {
        // 取出高水位值(分区的最大可读取位移值就是高水位值)
        val highWatermark = partitionState.fetchOffset
        val truncationState = OffsetTruncationState(highWatermark, truncationCompleted = true)

        info(s"Truncating partition $tp to local high watermark $highWatermark")
        // 执行截断到高水位值
        if (doTruncate(tp, truncationState))
          fetchOffsets.put(tp, truncationState)
      }
    }
    // 更新这组分区的分区读取状态
    updateFetchOffsetAndMaybeMarkTruncationComplete(fetchOffsets)
  }

  private def maybeFetch(): Unit = {
    val fetchRequestOpt = inLock(partitionMapLock) {
      // 构造FetchRequest
      val ResultWithPartitions(fetchRequestOpt, partitionsWithError) = buildFetch(partitionStates.partitionStateMap.asScala)
      // 处理出错分区，即将这个分区加入到Map末尾并等待后续重试
      handlePartitionsWithErrors(partitionsWithError, "maybeFetch")

      if (fetchRequestOpt.isEmpty) {
        trace(s"There are no active partitions. Back off for $fetchBackOffMs ms before sending a fetch request")
        partitionMapCond.await(fetchBackOffMs, TimeUnit.MILLISECONDS)
      }

      fetchRequestOpt
    }
    // 发送FETCH请求给对应的Leader副本，并处理相应的Response
    fetchRequestOpt.foreach { case ReplicaFetch(sessionPartitions, fetchRequest) =>
      processFetchRequest(sessionPartitions, fetchRequest)
    }
  }

}
```
#### PartitionData 拉取的消息数据
```scala
public static final class PartitionData<T extends BaseRecords> {
        public final Errors error;
        public final long highWatermark; // 高水位
        public final long lastStableOffset; // 最新LSO值，属于 Kafka 事务
        public final long logStartOffset; // 最新Log Start Offset值
        public final Optional<Integer> preferredReadReplica; // 期望的Read Replica
        public final List<AbortedTransaction> abortedTransactions; // 已终止的事务列表
        public final T records; // 消息集合
}
```
#### PartitionFetchState 状态类
分区状态
- 截断中，Truncating
- 被推迟，Delayed
- 可获取，ReadyForFetch
副本状态
- 截断中，Truncating
- 获取中，Fetching
```scala
/**
 * case class to keep partition offset and its state(truncatingLog, delayed)
 * This represents a partition as being either:
 * (1) 截断中 Truncating its log, for example having recently become a follower
 * (2) 被推迟 Delayed, for example due to an error, where we subsequently back off a bit
 * (3) 可获取 ReadyForFetch, the is the active state where the thread is actively fetching data.
 */
case class PartitionFetchState(fetchOffset: Long,
                               lag: Option[Long],
                               currentLeaderEpoch: Int,
                               delay: Option[DelayedItem],
                               state: ReplicaState) {
  // 分区可获取的：副本处于Fetching且未被推迟执行
  def isReadyForFetch: Boolean = state == Fetching && !isDelayed
  // 副本处于ISR：没有lag
  def isReplicaInSync: Boolean = lag.isDefined && lag.get <= 0
  // 分区处于截断中：副本处于Truncating状态且未被推迟执行
  def isTruncating: Boolean = state == Truncating && !isDelayed
  // 分区被推迟：存在未过期的延迟任务
  def isDelayed: Boolean = delay.exists(_.getDelay(TimeUnit.MILLISECONDS) > 0)

}
sealed trait ReplicaState
case object Truncating extends ReplicaState // 截断中
case object Fetching extends ReplicaState   // 获取中
```