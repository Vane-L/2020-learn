package com.basic;

/**
 * @Author: wenhongliang
 */
public class PrimeNumber {
    public static void main(String[] args) {
        int count = 0, number = 2;
        while (count < 20) {
            boolean isPrime = true;
            for (int divisor = 2; divisor <= number / 2 && isPrime; divisor++) {
                if (number % divisor == 0) {
                    isPrime = false;
                }
            }
            if (isPrime) {
                count++;
                System.out.print(" " + number);
            }
            number++;
        }
    }
}
