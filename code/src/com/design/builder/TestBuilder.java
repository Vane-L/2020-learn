package com.design.builder;

/**
 * @Author: wenhongliang
 */
public class TestBuilder {
    public static void main(String[] args) {
        Builder builder = new ComputerBuilder();
        Director director = new Director(builder);
        Computer computer = director.createComputer("Intel 酷睿i9 7900X", "三星M9T 2TB （HN-M201RAD）", "技嘉AORUS Z270X-Gaming 7", "科赋Cras II 红灯 16GB DDR4 3000");
        System.out.println("这台电脑使用的是：\n" + computer.getMainBoard() + " 主板\n" + computer.getCpu() + " CPU\n" + computer.getHardDisk() + "硬盘\n" + computer.getMainBoard() + " 内存\n");
    }
}
