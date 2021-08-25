package com.test.demo.constant;

/**
 * @Author: wenhongliang
 */
public class RedisKey {
    /**
     * Redis锁
     */
    public static final String CHANNEL_TMP_LOCK = "hashed-wheel-channelTmpLock";

    /***
     * 任务回收
     */
    public static final String TASK_RECYCLE = "hashed-wheel-task-recycle";

    /***
     * 任务回收锁
     */
    public static final String TASK_RECYCLE_LOCK = "hashed-wheel-task-recycle-lock";

}
