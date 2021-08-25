package com.test.wheel;

/**
 * @Author: wenhongliang
 */
public class TimeTask {
    /**
     * 延迟执行时间
     */
    private long delayMs;
    /**
     * 过期时间戳
     */
    private long expireTimestamp;

    private Runnable task;

    protected Bucket bucket;

    protected TimeTask pre;
    protected TimeTask next;

    public TimeTask(long delayMs, Runnable task) {
        this.delayMs = delayMs;
        this.task = task;
        this.bucket = null;
        this.pre = null;
        this.next = null;
        this.expireTimestamp = System.currentTimeMillis() + delayMs;
    }

    public Runnable getTask() {
        return task;
    }

    public long getExpireTimestamp() {
        return expireTimestamp;
    }
}
