package com.singleton;

/**
 * https://juejin.cn/post/6844903925783461896#heading-1
 *
 * @Author: wenhongliang
 */
public class SingletonResourceEnum {
    private SingletonResourceEnum() {
    }

    public enum Singleton {
        INSTANCE;
        private final SingletonResourceEnum instance;

        Singleton() {
            instance = new SingletonResourceEnum();
        }

        public SingletonResourceEnum getInstance() {
            return instance;
        }
    }

    public static SingletonResourceEnum getInstance() {
        return Singleton.INSTANCE.getInstance();
    }
}
