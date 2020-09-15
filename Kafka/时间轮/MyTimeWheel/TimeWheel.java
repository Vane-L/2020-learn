import java.util.Date;
import java.util.concurrent.DelayQueue;

/**
 * @Author: wenhongliang
 */
public class TimeWheel {
    /**
     * 一个时间槽的时间
     */
    private long tickMs;

    /**
     * 一个时间轮大小
     */
    private int wheelSize;
    /**
     * 一圈的时间跨度= tickMs * wheelSize
     */
    private long interval;
    /**
     * 一个槽
     */
    private Bucket[] buckets;

    /**
     * 当前时间戳
     */
    private long currentTimestamp;

    /**
     * 上层时间轮
     */
    private volatile TimeWheel overflowWheel;

    /**
     * 对于一个Timer以及附属的时间轮，都只有一个delayQueue
     */
    private DelayQueue<Bucket> delayQueue;

    /**
     * 初始化时间轮
     */
    public TimeWheel(long tickMs, int wheelSize, long currentTimestamp, DelayQueue<Bucket> delayQueue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.buckets = new Bucket[wheelSize];
        this.currentTimestamp = currentTimestamp - (currentTimestamp % tickMs);
        this.delayQueue = delayQueue;
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new Bucket();
        }
    }

    public boolean addTask(TimeTask timeTask) {
        long expireTimestamp = timeTask.getExpireTimestamp();
        // 延迟执行的时间
        long delayMs = expireTimestamp - currentTimestamp;
        if (delayMs < tickMs) {
            // 已经过期了..
            return false;
        } else {
            // 只有当前时间轮会将任务加入到delayQueue
            if (delayMs < interval) {
                // 在当前层的时间轮
                int bucketIdx = (int) ((expireTimestamp / tickMs) % wheelSize);
                Bucket bucket = buckets[bucketIdx];
                bucket.addTask(timeTask);
                if (bucket.setExpiration((expireTimestamp / tickMs) * tickMs)) {
                    delayQueue.offer(bucket);
                }
            } else {
                // 在上层的时间轮
                TimeWheel overflowWheel = this.getOverflowWheel();
                overflowWheel.addTask(timeTask);
            }
        }
        return true;
    }

    private TimeWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimeWheel(interval, wheelSize, currentTimestamp, delayQueue);
                }
            }
        }
        return overflowWheel;
    }

    public void advanceClock(long timestamp) {
        if (timestamp >= currentTimestamp + tickMs) {
            currentTimestamp = timestamp - (timestamp % tickMs);
            // 上层时间轮也要移动
            if (overflowWheel != null) {
                overflowWheel.advanceClock(timestamp);
            }
        }
    }
}
