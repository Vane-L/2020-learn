package com.design.builder;

/**
 * @Author: wenhongliang
 */
public interface Builder {
    // 安装主板
    void createMainBoard(String mainBoard);

    // 安装 cpu
    void createCpu(String cpu);

    // 安装硬盘
    void createHardDisk(String hardDisk);

    // 安装内存
    void createMemory(String memory);

    // 组成电脑
    Computer createComputer();
}
