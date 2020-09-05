### ISR管理
```scala
  def startup(): Unit = {
    // isr-expiration线程
    scheduler.schedule("isr-expiration", maybeShrinkIsr _, period = config.replicaLagTimeMaxMs / 2, unit = TimeUnit.MILLISECONDS)
    // isr-change-propagation线程
    scheduler.schedule("isr-change-propagation", maybePropagateIsrChanges _, period = 2500L, unit = TimeUnit.MILLISECONDS)
    ......
  }
```
- maybeShrinkIsr 方法，作用是周期地查看ISR中的副本集合是否需要收缩
    - 周期线程的频率是replicaLagTimeMaxMs/2，而判断Follower副本是否需要被移除ISR的条件是滞后程度是否超过了replicaLagTimeMaxMs值
    - 因此理论上，滞后程度小于 1.5 倍的replicaLagTimeMaxMs 值的 Follower 副本，依然有可能在 ISR 中，不会被移除
```scala
  private def maybeShrinkIsr(): Unit = {
    trace("Evaluating ISR list of partitions to see which replicas can be removed from the ISR")

    // Shrink ISRs for non offline partitions
    allPartitions.keys.foreach { topicPartition =>
      nonOfflinePartition(topicPartition).foreach(_.maybeShrinkIsr())
    }
  }

  // Partition.maybeShrinkIsr
  def maybeShrinkIsr(): Unit = {
    // 判断是否需要执行ISR收缩
    val needsIsrUpdate = inReadLock(leaderIsrUpdateLock) {
      needsShrinkIsr()
    }
    val leaderHWIncremented = needsIsrUpdate && inWriteLock(leaderIsrUpdateLock) {
      leaderLogIfLocal match {
        case Some(leaderLog) =>
          // 获取不一致同步的副本列表
          val outOfSyncReplicaIds = getOutOfSyncReplicas(replicaLagTimeMaxMs)
          if (outOfSyncReplicaIds.nonEmpty) {
            // 计算收缩后的ISR列表
            val newInSyncReplicaIds = inSyncReplicaIds -- outOfSyncReplicaIds
            assert(newInSyncReplicaIds.nonEmpty)
            info("Shrinking ISR from %s to %s. Leader: (highWatermark: %d, endOffset: %d). Out of sync replicas: %s."
              .format(inSyncReplicaIds.mkString(","),
                newInSyncReplicaIds.mkString(","),
                leaderLog.highWatermark,
                leaderLog.logEndOffset,
                outOfSyncReplicaIds.map { replicaId =>
                  s"(brokerId: $replicaId, endOffset: ${getReplicaOrException(replicaId).logEndOffset})"
                }.mkString(" ")
              )
            )

            // 更新zk和cache的ISR
            shrinkIsr(newInSyncReplicaIds)

            // 尝试更新Leader副本的高水位值
            maybeIncrementLeaderHW(leaderLog)
          } else {
            false
          }

        case None => false // do nothing if no longer leader
      }
    }

    // 如果Leader副本的高水位值抬升了，尝试解锁延迟请求
    if (leaderHWIncremented)
      tryCompleteDelayedRequests()
  }
```
- maybePropagateIsrChanges 方法，作用是定期向集群 Broker 传播 ISR 的变更
```scala
  /**
   * This function periodically runs to see if ISR needs to be propagated. It propagates ISR when:
   * 1. There is ISR change not propagated yet.
   * 2. There is no ISR Change in the last five seconds, or it has been more than 60 seconds since the last ISR propagation.
   * This allows an occasional ISR change to be propagated within a few seconds, and avoids overwhelming controller and
   * other brokers when large amount of ISR change occurs.
   */
  def maybePropagateIsrChanges(): Unit = {
    val now = System.currentTimeMillis()
    isrChangeSet synchronized {
      if (isrChangeSet.nonEmpty && // 1. 存在尚未被传播的ISR变更
        (lastIsrChangeMs.get() + ReplicaManager.IsrChangePropagationBlackOut < now ||        // 2.1 最近5秒没有ISR变更 
          lastIsrPropagationMs.get() + ReplicaManager.IsrChangePropagationInterval < now)) { // 2.2 比上次ISR变更超过60秒
        // 创建znode
        zkClient.propagateIsrChanges(isrChangeSet)
        // 清空isrChangeSet
        isrChangeSet.clear()
        // 更新最近ISR变更时间
        lastIsrPropagationMs.set(now)
      }
    }
  }
```