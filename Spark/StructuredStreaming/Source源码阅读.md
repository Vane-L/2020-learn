### Source
- 在每个 StreamExecution 的批次最开始，StreamExecution 会向 Source 询问当前 Source 的最新进度，即最新的 offset
    - 由 StreamExecution 调用 Source 的 `def getOffset: Option[Offset]`
- 这个 Offset 给到 StreamExecution 后会被 StreamExecution 持久化到自己的 WAL 里
    - 由 StreamExecution 调用 Source 的 `def getBatch(start: Option[Offset], end: Offset): DataFrame`
    - 由 Source 根据 StreamExecution 所要求的 start offset(Source 在上一个执行批次里提供的最新 offset)、end offset(Source 在这个批次里提供的最新 offset)，提供在 **(start, end]** 区间范围内的数据
- StreamExecution 触发计算逻辑 logicalPlan 的优化与编译
- 把计算结果写出给 Sink(注意这时才会由 Sink 触发发生实际的取数据操作，以及计算过程)
- 在数据完整写出到 Sink 后，StreamExecution 通知 Source 可以废弃数据；然后把成功的批次 id 写入到 batchCommitLog
    - 由 StreamExecution 调用 Source 的 `def commit(end: Offset): Unit`
    - commit() 方法主要是帮助 Source 完成 garbage-collection

实现类
- HDFS-compatible file system
- Kafka
- Rate

```scala
// 继承SparkDataStream是为了兼容v1的API
trait Source extends SparkDataStream {
  def schema: StructType
  // Returns the maximum available offset for this source.
  def getOffset: Option[Offset]
  // Returns the data that is between the offsets (`start`, `end`].
  def getBatch(start: Option[Offset], end: Offset): DataFrame 
  def commit(end: Offset) : Unit = {}

  override def initialOffset(): OffsetV2 = {
    throw new IllegalStateException("should not be called.")
  }
  override def deserializeOffset(json: String): OffsetV2 = {
    throw new IllegalStateException("should not be called.")
  }
  override def commit(end: OffsetV2): Unit = {
    throw new IllegalStateException("should not be called.")
  }
}
```

```scala
public interface SparkDataStream {
  // Returns the initial offset for a streaming query to start reading from.
  Offset initialOffset();
  Offset deserializeOffset(String json);
  void commit(Offset end);
  void stop();
}
```