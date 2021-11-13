package com.basic;

/**
 * @Author: wenhongliang
 */
public class ComputeChange {
    public static void main(String[] args) {
        double amount = 11.56;
        int remainingAmount = (int) (amount * 100);
        int numOfOneDollars = remainingAmount / 100;
        remainingAmount %= 100;
        int numOfQuarters = remainingAmount / 25;
        remainingAmount %= 25;
        int numOfDimes = remainingAmount / 10;
        remainingAmount %= 10;
        int numOfNickels = remainingAmount / 5;
        remainingAmount %= 5;
        int numOfPennies = remainingAmount;

        System.out.println("Your amount is " + amount);
        System.out.println(numOfOneDollars + " dollars");
        System.out.println(numOfQuarters + " quarters");
        System.out.println(numOfDimes + " dimes");
        System.out.println(numOfNickels + " nickels");
        System.out.println(numOfPennies + " pennies");
    }
}
