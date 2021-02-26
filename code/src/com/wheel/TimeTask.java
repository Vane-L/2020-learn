package com.wheel;

/**
 * @Author: wenhongliang
 */
public class TimeTask {
    /**
     * 过期时间戳
     */
    private long expireTimestamp;

    private Runnable task;

    protected Bucket bucket;
    protected TimeTask pre;
    protected TimeTask next;

    public TimeTask(long expireTimestamp, Runnable task) {
        this.bucket = null;
        this.pre = null;
        this.next = null;
        this.task = task;
        this.expireTimestamp = expireTimestamp;
    }

    public Runnable getTask() {
        return task;
    }

    public long getExpireTimestamp() {
        return expireTimestamp;
    }
}
