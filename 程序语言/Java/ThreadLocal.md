### ThreadLocal
get方法
```java
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}

// 获取线程的threadLocals
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```
set方法
```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
```
### ThreadLocalMap
getEntry方法
```java
// 从ThreadLocalMap获取entry
private Entry getEntry(ThreadLocal<?> key) {
    int i = key.threadLocalHashCode & (table.length - 1);
    Entry e = table[i];
    if (e != null && e.get() == key)
        return e;
    else
        // 如果没找到，就从下一个位置查找，i = nextIndex(i, len);
        return getEntryAfterMiss(key, i, e);
}
// 通过开放定址法，继续从ThreadLocalMap获取entry
private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
    Entry[] tab = table;
    int len = tab.length;

    while (e != null) {
        ThreadLocal<?> k = e.get();
        if (k == key)
            return e;
        if (k == null)
            // 防止内存泄漏
            expungeStaleEntry(i);
        else
            // 开放地址法，return ((i + 1 < len) ? i + 1 : 0);
            i = nextIndex(i, len);
        e = tab[i];
    }
    return null;
}
```
set方法
- 防止内存泄漏：replaceStaleEntry 和 cleanSomeSlots
```java
private void set(ThreadLocal<?> key, Object value) {

    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
    //采用线性探测法寻找合适位置
    for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
        ThreadLocal<?> k = e.get();

        if (k == key) {
            // 覆盖旧的value
            e.value = value;
            return;
        }

        if (k == null) {
            // key == null，但是e != null，说明之前的ThreadLocal对象已经被回收了
            replaceStaleEntry(key, value, i);
            return;
        }
    }
    // ThreadLocal对应的key实例不存在，则新实例一个
    tab[i] = new Entry(key, value);
    int sz = ++size;
    // 如果没有清理陈旧的 Entry 并且数组中的元素大于了阈值，则进行 rehash
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```