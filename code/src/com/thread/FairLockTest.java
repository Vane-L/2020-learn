package com.thread;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class FairLockTest {
    class MyLock {

        private boolean isLocked = false;
        private Thread lockingThread = null;

        public synchronized void lock() throws InterruptedException {
            while (isLocked) {
                wait();
            }
            isLocked = true;
            lockingThread = Thread.currentThread();
        }

        public synchronized void unlock() {
            if (this.lockingThread != Thread.currentThread()) {
                throw new IllegalMonitorStateException("Calling thread has not locked this lock");
            }
            isLocked = false;
            lockingThread = null;
            notify();
        }
    }

    class MyFairLock {
        private boolean isLocked = false;
        private Thread lockingThread = null;
        private List<QueueObject> waitingThreads = new ArrayList<>();

        public void lock() throws InterruptedException {
            QueueObject object = new QueueObject();
            synchronized (this) {
                waitingThreads.add(object);
            }

            while (true) {
                synchronized (this) {
                    boolean isLockedForThisThread = isLocked || waitingThreads.get(0) != object;
                    if (!isLockedForThisThread) {
                        isLocked = true;
                        waitingThreads.remove(object);
                        lockingThread = Thread.currentThread();
                        return;
                    }
                }
                try {
                    object.doWait();
                } catch (InterruptedException e) {
                    synchronized (this) {
                        waitingThreads.remove(object);
                    }
                    throw e;
                }
            }
        }

        public synchronized void unlock() {
            if (this.lockingThread != Thread.currentThread()) {
                throw new IllegalMonitorStateException("Calling thread has not locked this lock");
            }
            isLocked = false;
            lockingThread = null;
            if (!waitingThreads.isEmpty()) {
                waitingThreads.get(0).doNotify();
            }
        }
    }

    class QueueObject {
        private boolean isNotified = false;

        public synchronized void doWait() throws InterruptedException {
            while (!isNotified) {
                this.wait();
            }
            this.isNotified = false;
        }

        public synchronized void doNotify() {
            this.isNotified = true;
            this.notify();
        }

        public boolean equals(Object o) {
            return this == o;
        }
    }

}
