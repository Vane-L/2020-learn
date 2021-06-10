### Java同步块
- Java中的同步块用synchronized标记。同步块在Java中是同步在某个对象上。
- 所有同步在一个对象上的同步块在同时只能被一个线程进入并执行操作。所有其他等待进入该同步块的线程将被阻塞，直到执行该同步块中的线程退出。

实例方法同步
- 实例方法同步是同步在**拥有该方法的对象**上。
- 只有一个线程能够在实例方法同步块中运行。
- 如果有多个实例存在，那么一个线程一次可以在一个实例同步块中执行操作。
```java
public synchronized void add(int value){
    this.count += value;
 }
```

静态方法同步
- 静态方法的同步是指同步在**该方法所在的类对象**上。
- 因为在Java虚拟机中一个类只能对应一个类对象，所以同时**只允许一个线程执行同一个类中的静态同步方法**。
- 不管类中的哪个静态同步方法被调用，一个类只能由一个线程同时执行。
```java
public static synchronized void add(int value){
    count += value;
 }
```

实例方法中的同步块
- 注意Java同步块构造器用括号将对象括起来。
- 在同步构造器中用括号括起来的对象叫做**监视器对象**。同步实例方法使用调用方法本身的实例作为监视器对象。
- 一次只有一个线程能够在同步于同一个监视器对象的Java方法内执行。
- 以下代码，每次只有一个线程能够在两个同步块中任意一个方法内执行。如果第二个同步块不是同步在this实例对象上，那么两个方法可以被线程同时执行。
```java
public class MyClass {

   public synchronized void log1(String msg1, String msg2){
      log.writeln(msg1);
      log.writeln(msg2);
   }

   public void log2(String msg1, String msg2){
      synchronized(this){
         log.writeln(msg1);
         log.writeln(msg2);
      }
   }
 }
```

静态方法中的同步块
- 静态方法中的同步块同步在**该方法所属的类对象**上。
- 以下代码，这两个方法不允许同时被线程访问。如果第二个同步块不是同步在MyClass.class这个对象上。那么这两个方法可以同时被线程访问。
```java
public class MyClass {
    public static synchronized void log1(String msg1, String msg2){
       log.writeln(msg1);
       log.writeln(msg2);
    }

    public static void log2(String msg1, String msg2){
       synchronized(MyClass.class){
          log.writeln(msg1);
          log.writeln(msg2);
       }
    }
  }
```