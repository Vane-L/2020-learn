package com.hashwheel;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wenhongliang
 */
public class TestMain {
    public static void testTimerOverflowWheelLength() throws InterruptedException {
        final HashedWheelTimer timer =
                new HashedWheelTimer(Executors.defaultThreadFactory(), 500, TimeUnit.MILLISECONDS, 4);
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
                System.out.println("abc");   //打印名字
            }
        }, 3000, TimeUnit.MILLISECONDS);
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(final Timeout timeout) throws Exception {
                System.out.println("abc");   //打印名字
            }
        }, 11000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        testTimerOverflowWheelLength();
    }
}
