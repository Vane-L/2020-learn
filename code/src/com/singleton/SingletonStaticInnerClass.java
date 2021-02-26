package com.singleton;

/**
 * @Author: wenhongliang
 */
public class SingletonStaticInnerClass {
    private SingletonStaticInnerClass() {

    }

    /**
     * 静态内部类单例模式也称单例持有者模式，实例由内部类创建
     * 由于 JVM 在加载外部类的过程中, 是不会加载静态内部类的, 只有内部类的属性/方法被调用时才会被加载, 并初始化其静态属性。
     * 静态属性由static修饰，保证只被实例化一次，并且严格保证实例化顺序。
     */
    private static class InstanceHolder {
        private final static SingletonStaticInnerClass instance = new SingletonStaticInnerClass();
    }

    public static SingletonStaticInnerClass getInstance() {
        // 调用内部类属性
        return InstanceHolder.instance;
    }

}
