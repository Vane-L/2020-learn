### StateStore
Fault-tolerance model:
容错模型：
- 每次更新都在提交之前会写入增量文件
- StateStore负责增量文件的管理、崩溃和清理
- 多次提交相同版本可能会覆盖。
- 最新版本的文件用于可恢复，因为它保证重新执行RDD

实现类
- HDFSBackedStateStore
- MemoryStateStore

```scala
trait StateStore {
  def id: StateStoreId
  def version: Long
  def get(key: UnsafeRow): UnsafeRow
  def put(key: UnsafeRow, value: UnsafeRow): Unit
  def remove(key: UnsafeRow): Unit
  def getRange(start: Option[UnsafeRow], end: Option[UnsafeRow]): Iterator[UnsafeRowPair] = {
    iterator()
  }
  def commit(): Long
  def abort(): Unit
  def iterator(): Iterator[UnsafeRowPair]
  def metrics: StateStoreMetrics
  def hasCommitted: Boolean
}
```

```scala
case class StateStoreId(
    checkpointRootLocation: String,
    operatorId: Long,
    partitionId: Int,
    storeName: String = StateStoreId.DEFAULT_STORE_NAME) {

  def storeCheckpointLocation(): Path = {
    if (storeName == StateStoreId.DEFAULT_STORE_NAME) {
      // For reading state store data that was generated before store names were used (Spark <= 2.2)
      new Path(checkpointRootLocation, s"$operatorId/$partitionId")
    } else {
      new Path(checkpointRootLocation, s"$operatorId/$partitionId/$storeName")
    }
  }
}
```