package com.singleton;

/**
 * good: 实现了懒加载，节约了内存空间
 * bad: 在不加锁的情况下，线程不安全，可能出现多份实例;在加锁的情况下，会是程序串行化，使系统有严重的性能问题
 *
 * @Author: wenhongliang
 */
public class SingletonStatic {
    // 定义静态变量时，未初始化实例
    private static SingletonStatic instance;

    // 私有化构造函数
    private SingletonStatic() {
    }

    public static SingletonStatic getInstance() {
        // 使用时，先判断实例是否为空，如果实例为空，则实例化对象
        if (instance == null)
            instance = new SingletonStatic();
        return instance;
    }

}
