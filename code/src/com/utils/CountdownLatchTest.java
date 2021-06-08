package com.utils;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: wenhongliang
 * CountdownLatch是通过一个计数器实现的，当我们在new一个CountdownLatch对象的时候，需要传入计数器的值，该值表示线程的数量
 * 每当一个线程完成任务后，计数器的值就会减1，当计数器的值变为0时，就表示所有线程均已完成任务，然后就可以恢复等待的线程继续执行了。
 * <p>
 * 和CyclicBarrier的区别：
 * CountdownLatch的作用是允许一个或多个线程等待其他线程完成后继续执行。而CyclicBarrier则是允许多个线程相互等待。
 * CountdownLatch的计数器无法被重置，CyclicBarrier的计数器可以被重置后使用。
 */
public class CountdownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start at " + System.currentTimeMillis());
        int num = 10;
        CountDownLatch countDownLatch = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            new Thread(() -> {
                System.out.println("Run at " + countDownLatch.getCount());
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        System.out.println("Done at " + System.currentTimeMillis());
    }
}
