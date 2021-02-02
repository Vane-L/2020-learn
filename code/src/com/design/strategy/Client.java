package com.design.strategy;

/**
 * @Author: wenhongliang
 */
public class Client {
    public static void main(String[] args) {
        Context context;
        context = new Context(new ConcreteStrategyA());
        context.contextInterface();

        context = new Context(new ConcreteStrategyB());
        context.contextInterface();


        MyContext myContext;
        myContext = new MyContext(new MyConcreteStrategyA(), new MyConcreteStrategyB());
        myContext.init(10, 100, 100);
        myContext.work();

        myContext = new MyContext(new MyConcreteStrategyC(), new MyConcreteStrategyD());
        myContext.init(5, 50, 50);
        myContext.work();

    }
}
