### DStream -> RDD
DStream通过compute生成RDD，并放入map[Time, RDD]
```scala
  // key 是一个 Time，value 是 RDD 的实例
  @transient
  private[streaming] var generatedRDDs = new HashMap[Time, RDD[T]]()

  private[streaming] final def getOrCompute(time: Time): Option[RDD[T]] = {
    // 如果RDD已经在map里则直接获取，否则生成RDD
    generatedRDDs.get(time).orElse {
      // 校验time
      if (isTimeValid(time)) {
        val rddOption = createRDDWithLocalProperties(time, displayInnerRDDOps = false) {
          SparkHadoopWriterUtils.disableOutputSpecValidation.withValue(true) {
            compute(time) // 通过compute得到RDD实例
          }
        }
        
        rddOption.foreach { case newRDD =>
          // Register the generated RDD for caching and checkpointing
          if (storageLevel != StorageLevel.NONE) {
            newRDD.persist(storageLevel)
            logDebug(s"Persisting RDD ${newRDD.id} for time $time to $storageLevel")
          }
          if (checkpointDuration != null && (time - zeroTime).isMultipleOf(checkpointDuration)) {
            newRDD.checkpoint()
            logInfo(s"Marking RDD ${newRDD.id} for time $time for checkpointing")
          }
          // 放入 generatedRDDs 对应的 time 位置
          generatedRDDs.put(time, newRDD)
        }
        rddOption
      } else {
        None
      }
    }
  }
```

以FilteredDStream为例子
```scala
class FilteredDStream[T: ClassTag](
    parent: DStream[T],
    filterFunc: T => Boolean
  ) extends DStream[T](parent.ssc) {

  override def dependencies: List[DStream[_]] = List(parent)

  override def slideDuration: Duration = parent.slideDuration

  override def compute(validTime: Time): Option[RDD[T]] = {
    parent.getOrCompute(validTime).map(_.filter(filterFunc))
  }
}
```
