### Source
实现类
- HDFS-compatible file system
- Kafka
- Rate

```scala
trait Source extends SparkDataStream {
  def schema: StructType
  // Returns the maximum available offset for this source.
  def getOffset: Option[Offset]
  // Returns the data that is between the offsets (`start`, `end`].
  def getBatch(start: Option[Offset], end: Offset): DataFrame 
  def commit(end: Offset) : Unit = {}
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