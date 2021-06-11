package com.thread;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class ReadWriteLockTest {
    class MyReadWriteLock {
        private int readers = 0;
        private int writers = 0;
        private int writeRequests = 0;

        public synchronized void lockRead() throws InterruptedException {
            while (writers > 0 || writeRequests > 0) {
                wait();
            }
            readers++;
        }

        public synchronized void unlockRead() {
            readers--;
            notifyAll();
        }

        public synchronized void lockWrite() throws InterruptedException {
            writeRequests++;
            while (readers > 0 || writers > 0) {
                wait();
            }
            writeRequests--;
            writers++;
        }

        public synchronized void unlockWrite() {
            writers--;
            notifyAll();
        }
    }

    // 可重入的意思是线程可以重复获得它已经持有的锁。
    class MyReentrantReadWriteLock {
        // 读锁重入
        private Map<Thread, Integer> readingThreads = new HashMap<>();
        private int writers = 0;
        private int writeRequests = 0;
        // 写锁重入
        private int writeAccesses = 0;
        private Thread writingThread = null;
        // 读锁升级
        private int readers = 0;

        /* 读锁 */
        public synchronized void lockRead() throws InterruptedException {
            Thread callingThread = Thread.currentThread();
            while (!canGrantReadAccess(callingThread)) {
                wait();
            }
            readingThreads.put(callingThread, readingThreads.getOrDefault(callingThread, 0) + 1);
        }

        private boolean canGrantReadAccess(Thread callingThread) {
            // 写锁降级到读锁
            if (writingThread == callingThread) return true;
            if (writingThread != null) return false;
            if (readingThreads.get(callingThread) != null) return true;
            if (writeRequests > 0) return false;
            return true;
        }

        public synchronized void unlockRead() {
            Thread callingThread = Thread.currentThread();
            int accessCount = readingThreads.getOrDefault(callingThread, 0);
            if (accessCount == 1) {
                readingThreads.remove(callingThread);
            } else {
                readingThreads.put(callingThread, accessCount - 1);
            }
            notifyAll();
        }

        /* 写锁 */
        public synchronized void lockWrite() throws InterruptedException {
            writeRequests++;
            Thread callingThread = Thread.currentThread();
            while (!canGrantWriteAccess(callingThread)) {
                wait();
            }
            writeRequests--;
            writeAccesses++;
            writingThread = callingThread;
        }

        private boolean canGrantWriteAccess(Thread callingThread) {
            // 读锁升级到写锁
            if (readingThreads.size() == 1 && readingThreads.get(callingThread) != null) return true;
            if (readingThreads.size() > 0) return false;
            if (writingThread == null) return true;
            if (writingThread != callingThread) return false;
            return true;
        }

        public synchronized void unlockWrite() {
            writeAccesses--;
            if (writeAccesses == 0) {
                writingThread = null;
            }
            notifyAll();
        }
    }
}
