package com.hashwheel;

import java.util.Set;

/**
 * @Author: wenhongliang
 */
// 双向链表
public class HashedWheelBucket {
    /**
     * Used for the linked-list datastructure
     */
    private HashedWheelTimeout head;
    private HashedWheelTimeout tail;

    /**
     * Add {@link HashedWheelTimeout} to this bucket.
     */
    void addTimeout(HashedWheelTimeout timeout) {
        assert timeout.bucket == null;
        timeout.bucket = this;
        if (head == null) {
            head = tail = timeout;
        } else {
            tail.next = timeout;
            timeout.prev = tail;
            tail = timeout;
        }
    }

    /**
     * Expire all {@link HashedWheelTimeout}s for the given {@code deadline}.
     */
    void expireTimeouts(long deadline) {
        HashedWheelTimeout timeout = head;

        // process all timeouts
        while (timeout != null) {
            HashedWheelTimeout next = timeout.next;
            if (timeout.remainingRounds <= 0) {
                next = remove(timeout);
                if (timeout.deadline <= deadline) {
                    // 任务到期了，执行任务
                    timeout.expire();
                } else {
                    // The timeout was placed into a wrong slot. This should never happen.
                    throw new IllegalStateException(String.format("timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
                }
            } else if (timeout.isCancelled()) {
                next = remove(timeout);
            } else {
                timeout.remainingRounds--;
            }
            timeout = next;
        }
    }

    public HashedWheelTimeout remove(HashedWheelTimeout timeout) {
        HashedWheelTimeout next = timeout.next;
        // remove timeout that was either processed or cancelled by updating the linked-list
        if (timeout.prev != null) {
            timeout.prev.next = next;
        }
        if (timeout.next != null) {
            timeout.next.prev = timeout.prev;
        }

        if (timeout == head) {
            // if timeout is also the tail we need to adjust the entry too
            if (timeout == tail) {
                tail = null;
                head = null;
            } else {
                head = next;
            }
        } else if (timeout == tail) {
            // if the timeout is the tail modify the tail to be the prev node.
            tail = timeout.prev;
        }
        // null out prev, next and bucket to allow for GC.
        timeout.prev = null;
        timeout.next = null;
        timeout.bucket = null;
        timeout.timer.pendingTimeouts.decrementAndGet();
        return next;
    }

    /**
     * Clear this bucket and return all not expired / cancelled {@link Timeout}s.
     */
    void clearTimeouts(Set<Timeout> set) {
        for (; ; ) {
            HashedWheelTimeout timeout = pollTimeout();
            if (timeout == null) {
                return;
            }
            if (timeout.isExpired() || timeout.isCancelled()) {
                continue;
            }
            set.add(timeout);
        }
    }

    private HashedWheelTimeout pollTimeout() {
        HashedWheelTimeout head = this.head;
        if (head == null) {
            return null;
        }
        HashedWheelTimeout next = head.next;
        if (next == null) {
            tail = this.head = null;
        } else {
            this.head = next;
            next.prev = null;
        }

        // null out prev and next to allow for GC.
        head.next = null;
        head.prev = null;
        head.bucket = null;
        return head;
    }
}
