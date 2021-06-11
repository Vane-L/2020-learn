package com.thread;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class BlockingQueueTest {
    class MyBlockingQueue {

        private List queue = new LinkedList();
        private int limit = 10;

        public MyBlockingQueue() {
        }

        public MyBlockingQueue(int limit) {
            this.limit = limit;
        }

        public synchronized void enqueue(Object item) throws InterruptedException {
            while (this.queue.size() == this.limit) {
                wait();
            }
            if (this.queue.isEmpty()) {
                notifyAll();
            }
            this.queue.add(item);
        }

        public synchronized Object dequeue() throws InterruptedException {
            while (this.queue.isEmpty()) {
                wait();
            }
            if (this.queue.size() == this.limit) {
                notifyAll();
            }
            return this.queue.remove(0);
        }
    }
}
