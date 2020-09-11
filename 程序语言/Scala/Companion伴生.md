### Companion伴生
在 Scala 语言里，在一个源代码文件中同时定义相同名字的 **class** 和 **object** 的用法被称为 **_Companion伴生_**
- **Class对象**被称为伴生类，它和 Java 中的类是一样的
- **Object对象**是一个单例对象，用于保存一些静态变量或静态方法
ps: 如果用 Java 来实现的话，我们必须要编写两个类—— LogSegment 和 LogSegmentUtils。