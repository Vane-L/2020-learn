package com.singleton;

/**
 * @Author: wenhongliang
 */
public class SingletonDoubleCheckWithVolatile {
    /**
     * 要解决双重检查锁模式带来空指针异常的问题，只需要使用volatile关键字
     * volatile关键字严格遵循happens-before原则，即在读操作前，写操作必须全部完成
     */
    private static volatile SingletonDoubleCheckWithVolatile instance;

    private SingletonDoubleCheckWithVolatile() {

    }


    public static SingletonDoubleCheckWithVolatile getInstance() {
        if (instance == null)
            synchronized (SingletonDoubleCheckWithVolatile.class) {
                if (instance == null) {
                    instance = new SingletonDoubleCheckWithVolatile();
                }
            }
        return instance;
    }

}
