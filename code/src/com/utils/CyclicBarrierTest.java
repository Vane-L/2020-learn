package com.utils;

import java.util.concurrent.CyclicBarrier;

/**
 * @Author: wenhongliang
 * CyclicBarrier是一个同步辅助类，它允许一组线程相互等待，直到达到某个公共屏障点。
 * 在涉及一组大小固定的线程的程序里，这些线程必须不时的相互等待。
 * 因为CyclicBarrier在释放等待线程后可以重用，因此成为循环屏障。
 */
public class CyclicBarrierTest {
    private static CyclicBarrier cyclicBarrier;
    private static final Integer THREAD_COUNT = 10;

    static class CyclicBarrierThread implements Runnable {
        @Override
        public void run() {
            try {
                System.out.println(Thread.currentThread().getName() + ":" + cyclicBarrier.getNumberWaiting());
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        cyclicBarrier = new CyclicBarrier(THREAD_COUNT, () -> System.out.println("abc"));

        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(new CyclicBarrierThread());
            thread.start();
        }
    }
}
