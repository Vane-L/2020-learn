package com.test.wheel;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @Author: wenhongliang
 */
public class Bucket implements Delayed {
    /**
     * 当前槽的过期时间
     */
    private AtomicLong expiration = new AtomicLong(-1L);
    /**
     * 根节点
     */
    private TimeTask root = new TimeTask(-1L, null);

    // 为什么要双向循环链表？ 因为插入和删除都O(1)
    {
        root.next = root;
        root.pre = root;
    }

    public long getExpiration() {
        return expiration.get();
    }

    public boolean setExpiration(long expire) {
        return expiration.getAndSet(expire) != expire;
    }

    public synchronized void addTask(TimeTask timeTask) {
        if (timeTask.bucket == null) {
            timeTask.bucket = this;

            TimeTask tail = root.pre;

            timeTask.next = root;
            timeTask.pre = tail;

            tail.next = timeTask;
            root.pre = timeTask;
        }
    }

    public synchronized void removeTask(TimeTask timeTask) {
        if (timeTask.bucket.equals(this)) {
            timeTask.next.pre = timeTask.pre;
            timeTask.pre.next = timeTask.next;
            timeTask.bucket = null;
            timeTask.next = null;
            timeTask.pre = null;
        }
    }

    public synchronized void flush(Consumer<TimeTask> flush) {
        // 执行该bucket下的所有task
        TimeTask timeTask = root.next;
        while (!timeTask.equals(root)) {
            this.removeTask(timeTask);
            flush.accept(timeTask);
            timeTask = root.next;
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, expiration.get() - System.currentTimeMillis());
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof Bucket) {
            return Long.compare(expiration.get(), ((Bucket) o).expiration.get());
        }
        return 0;
    }
}
