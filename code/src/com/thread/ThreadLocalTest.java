package com.thread;

/**
 * @Author: wenhongliang
 */
public class ThreadLocalTest {
    static class MyRunnable implements Runnable {
        private ThreadLocal<Integer> threadLocal = new ThreadLocal() {
            @Override
            protected Integer initialValue() {
                return 100;
            }
        };

        @Override
        public void run() {
            threadLocal.set((int) (Math.random() * 100));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MyRunnable myRunnable = new MyRunnable();
        Thread thread1 = new Thread(myRunnable, "Thread1");
        Thread thread2 = new Thread(myRunnable, "Thread2");

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }
}
