### Scala Collections
- ArrayBuffer : an indexed, mutable sequence
```scala
import scala.collection.mutable.ArrayBuffer
val nums = ArrayBuffer(1, 2, 3)
nums += 4
nums += 5 += 6
nums ++= List(7, 8, 9)
nums -= 9
nums -= 7 -= 8
nums --= Array(5, 6)
```

- List : a linear (linked list), immutable sequence
```scala
val ints = List.range(1, 10)
```

- Vector : an indexed, immutable sequence
```scala
val a = Vector(1,2,3)
```

- Map : the base Map (key/value pairs) class
```scala
val states = collection.mutable.Map("AK" -> "Alaska")
```

- Set : the base Set class
```scala
val set = scala.collection.mutable.Set(1, 2, 3, 4, 5)
```

