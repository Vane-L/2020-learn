package com.design.builder;

/**
 * @Author: wenhongliang
 */
public class Director {

    private Builder builder;

    public Director(Builder builder) {
        this.builder = builder;
    }

    public Computer createComputer(String cpu, String hardDisk, String mainBoard, String memory) {
        this.builder.createMainBoard(mainBoard);
        this.builder.createCpu(cpu);
        this.builder.createMemory(memory);
        this.builder.createHardDisk(hardDisk);
        return this.builder.createComputer();
    }

}
