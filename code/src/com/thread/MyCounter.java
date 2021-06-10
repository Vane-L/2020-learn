package com.thread;

/**
 * @Author: wenhongliang
 */
public class MyCounter {
    long count = 0;

    public synchronized void add(long value) {
        this.count += value;
    }

    static class CounterThread extends Thread {
        protected MyCounter myCounter;

        public CounterThread(MyCounter myCounter) {
            this.myCounter = myCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                myCounter.add(i);
            }
        }
    }

    public static void main(String[] args) {
        MyCounter counter = new MyCounter();
        CounterThread thread1 = new CounterThread(counter);
        CounterThread thread2 = new CounterThread(counter);
        thread1.start();
        thread2.start();
    }
}
