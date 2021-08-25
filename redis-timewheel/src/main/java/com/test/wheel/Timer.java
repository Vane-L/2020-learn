package com.test.wheel;

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wenhongliang
 */
public enum Timer {
    INSTANCE;

    private TimeWheel timeWheel;

    // 一个有序的堆，所有的任务都放置在delayQueue
    private DelayQueue<Bucket> delayQueue = new DelayQueue<>();

    private ExecutorService worker;

    private ExecutorService root;

    public static Timer getInstance() {
        return INSTANCE;
    }

    Timer() {
        worker = Executors.newFixedThreadPool(1);
        root = Executors.newFixedThreadPool(1);
        timeWheel = new TimeWheel(1000, 60, System.currentTimeMillis(), delayQueue);
        root.execute(Timer::run);
    }

    private static void run() {
        while (true) {
            // Timer线程定时线程
            INSTANCE.advanceClock(1000);
        }
    }

    public void addTask(TimeTask timeTask) {
        if (!timeWheel.addTask(timeTask)) {
            // Already expired
            worker.submit(timeTask.getTask());
        }
    }

    private void advanceClock(long timeout) {
        try {
            System.out.println("advanceClock..." + new Date());
            // 获取队列中快过期的任务
            Bucket bucket = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (bucket != null) {
                // 更新每层时间轮的currentTimestamp
                timeWheel.advanceClock(bucket.getExpiration());
                // 进行任务的重新插入，实现任务时间轮降层
                bucket.flush(this::addTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("start:" + new Date());
        Timer timer = Timer.getInstance();
        timer.addTask(new TimeTask(TimeUnit.SECONDS.toMillis(1L), new Runnable() {
            @Override
            public void run() {
                System.out.println("1 testing..." + new Date());
            }
        }));
        timer.addTask(new TimeTask(TimeUnit.SECONDS.toMillis(10L), new Runnable() {
            @Override
            public void run() {
                System.out.println("2 testing..." + new Date());
            }
        }));
        timer.addTask(new TimeTask(TimeUnit.MINUTES.toMillis(1L), new Runnable() {
            @Override
            public void run() {
                System.out.println("3 testing..." + new Date());
            }
        }));
        timer.addTask(new TimeTask(TimeUnit.MINUTES.toMillis(10L), new Runnable() {
            @Override
            public void run() {
                System.out.println("3 testing..." + new Date());
            }
        }));
        timer.addTask(new TimeTask(TimeUnit.HOURS.toMillis(1L), new Runnable() {
            @Override
            public void run() {
                System.out.println("3 testing..." + new Date());
            }
        }));
        timer.addTask(new TimeTask(TimeUnit.HOURS.toMillis(10L), new Runnable() {
            @Override
            public void run() {
                System.out.println("3 testing..." + new Date());
            }
        }));
        System.out.println("end:" + new Date());
    }
}
