package com.singleton;

/**
 * @Author: wenhongliang
 */
public class SingletonDoubleCheck {
    private static SingletonDoubleCheck instance;

    private SingletonDoubleCheck() {

    }

    /**
     * 在多线程的情况下，可能会出现空指针问题
     * 出现问题的原因是JVM在实例化对象的时候会进行优化和指令重排序操作。
     */
    public static SingletonDoubleCheck getInstance() {
        // 第一次判断，如果这里为空，不进入抢锁阶段，直接返回实例
        if (instance == null)
            synchronized (SingletonDoubleCheck.class) {
                // 抢到锁之后再次判断是否为空
                if (instance == null) {
                    instance = new SingletonDoubleCheck();
                }
            }
        return instance;
    }
}
