package com.utils;

import java.util.concurrent.Semaphore;

/**
 * @Author: wenhongliang
 * Semophore是一个控制访问多个共享资源的计数器，和CountdownLatch一样，本质上是一个共享锁，维护了一个许可集。
 * 以停车场为例：假设停车场有5个停车位，一开始车位都空着，先后来了3辆车，车位够，安排进去停车。然后又来了3辆车，这时由于只有两个车位，所以只能停两辆，有1辆需要在外面等候，直到停车场有空位。
 * 从程序来看，停车场就是信号量Semophore，许可集为5，车辆为线程，每来一辆车，许可数-1，但必须>=0，否则线程就要阻塞（车辆等待）。如果有一辆车开出，则许可数+1，然后唤醒一个线程（可以放进一辆车）。
 */
public class SemaphoreTest {

    public static void main(String[] args) {
        //假设有3个停车位,这时候同时来了5辆车，只有3辆车可以进去停车，其余2辆车需要等待有空余车位之后才能进去停车。
        Parking parking = new Parking(3);
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(parking);
            thread.start();
        }
    }

    static class Parking implements Runnable {
        private Semaphore semaphore;

        Parking(int count) {
            semaphore = new Semaphore(count);
        }

        @Override
        public void run() {
            try {
                //获取信号量
                semaphore.acquire();
                long time = (long) (Math.random() * 10 + 1);
                System.out.println(Thread.currentThread().getName() + "进入停车场停车，停车时间：" + time + "秒");
                //模拟停车时间
                Thread.sleep(time);
                System.out.println(Thread.currentThread().getName() + "开出停车场...");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //释放信号量（跟lock的用法差不多）
                semaphore.release();
            }
        }
    }

}
