### Sink
把计算结果写出给 Sink
- 具体是由 StreamExecution 调用 `Sink.addBatch(batchId: Long, data: DataFrame)`
- 注意这时才会由 Sink 触发发生实际的取数据操作，以及计算过程
- 通常 Sink 直接可以直接把 _data: DataFrame_ 的数据写出，并在完成后记录下 _batchId: Long_
- 在故障恢复时，分两种情况讨论：
    - 如果上次执行在本步 结束前即失效，那么本次执行里 sink 应该完整写出计算结果
    - 如果上次执行在本步 结束后才失效，那么本次执行里 sink 可以重新写出计算结果并覆盖上次结果，也可以跳过写出计算结果

实现类
- HDFS-compatible file system，具体实现是 FileStreamSink extends Sink
- Kafka sink，具体实现是 KafkaSink extends Sink
- Foreach sink，具体实现是 ForeachSink extends Sink


```scala
// 继承Table是为了兼容v1的API
trait Sink extends Table {
  // 添加批量数据，batchId相同，data只会添加一次
  def addBatch(batchId: Long, data: DataFrame): Unit

  override def name: String = {
    throw new IllegalStateException("should not be called.")
  }
  override def schema: StructType = {
    throw new IllegalStateException("should not be called.")
  }
  override def capabilities: util.Set[TableCapability] = {
    throw new IllegalStateException("should not be called.")
  }
}
```

```scala
public interface Table {
  String name();
  // Returns the schema of this table. 
  StructType schema();
  // 返回物理分区
  default Transform[] partitioning() {
    return new Transform[0];
  }
  default Map<String, String> properties() {
    return Collections.emptyMap();
  }
  Set<TableCapability> capabilities();
}
```