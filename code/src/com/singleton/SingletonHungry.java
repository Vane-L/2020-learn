package com.singleton;

/**
 * good: 使用了static关键字，保证了JVM层面的线程安全
 * bad: 不能实现懒加载，造成空间浪费
 *
 * @Author: wenhongliang
 */
public class SingletonHungry {
    //利用静态变量来存储唯一实例
    private static final SingletonHungry instance = new SingletonHungry();

    // 私有化构造函数
    private SingletonHungry() {
    }

    // 提供公开获取实例接口
    public static SingletonHungry getInstance() {
        return instance;
    }
}
