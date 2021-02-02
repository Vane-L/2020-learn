package com.design.strategy;

/**
 * @Author: wenhongliang
 */
public class MyContext {
    MyStrategyA strategyA;
    MyStrategyB strategyB;

    public MyContext(MyStrategyA strategyA, MyStrategyB strategyB) {
        this.strategyA = strategyA;
        this.strategyB = strategyB;
    }

    private long batchNum;
    private long fileSize;
    private long fileRow;

    public void init(long batchNum, long fileSize, long fileRow) {
        this.batchNum = batchNum;
        this.fileSize = fileSize;
        this.fileRow = fileRow;
    }

    public void work() {
        System.out.println("Default Check " + this.fileSize + " ...");
        System.out.println("Default Check " + this.fileRow + " ...");
        strategyA.contextCheck();
        System.out.println("Default Lock File...");
        System.out.println("Upload File to S3...");
        for (int i = 0; i < this.batchNum; i++) {
            strategyB.contextProcess();
            System.out.println("Process " + i + " Done...");
        }
        System.out.println("Notification by Email...");
    }
}
