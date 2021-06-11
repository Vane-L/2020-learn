package com.thread;

/**
 * @Author: wenhongliang
 */
public class LockTest {
    class MySimpleLock {
        private boolean isLocked = false;

        public synchronized void lock() throws InterruptedException {
            while (isLocked) {
                wait();
            }
            isLocked = true;
        }

        public synchronized void unlock() {
            isLocked = false;
            notify();
        }
    }

    // 可重入的意思是线程可以重复获得它已经持有的锁。
    class MyReentrantLock {
        boolean isLocked = false;
        Thread lockedBy = null;
        // 可重入
        int lockedCount = 0;

        public synchronized void lock() throws InterruptedException {
            while (isLocked && lockedBy != Thread.currentThread()) {
                wait();
            }
            isLocked = true;
            // 需要记录同一个线程重复对一个锁对象加锁的次数。
            lockedCount++;
            lockedBy = Thread.currentThread();
        }

        public synchronized void unlock() {
            if (Thread.currentThread() == this.lockedBy) {
                lockedCount--;
                if (lockedCount == 0) {
                    isLocked = false;
                    notify();
                }
            }
        }
    }
}
