package com.thread;

/**
 * @Author: wenhongliang
 */
public class ThreadTest {
    public static void main(String[] args) {
        System.out.println("Start");
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ":run()");
        }).run();
        System.out.println("End");
        System.out.println("*************");
        System.out.println("Start");
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ":start()");
        }).start();
        System.out.println("End");
    }
}
