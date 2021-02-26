package com.singleton;

/**
 * @Author: wenhongliang
 */
public class SingletonStaticWithSynchronized {
    private static SingletonStaticWithSynchronized instance;

    private SingletonStaticWithSynchronized() {
    }

    /**
     * 添加class类锁，影响了性能，加锁之后将代码进行了串行化
     */
    public synchronized static SingletonStaticWithSynchronized getInstance() {
        if (instance == null)
            instance = new SingletonStaticWithSynchronized();
        return instance;
    }

}
