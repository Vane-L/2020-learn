package com.basic;

/**
 * @Author: wenhongliang
 */
public class PrimeNumber {
    public static void main(String[] args) {
        test1(30);
        System.out.println();
        test2(30);
        System.out.println();
        test3(30);
    }

    public static void test1(int n) {
        int number = 2;
        while (number <= n) {
            boolean isPrime = true;
            for (int divisor = 2; divisor <= number / 2 && isPrime; divisor++) {
                if (number % divisor == 0) {
                    isPrime = false;
                }
            }
            if (isPrime) {
                System.out.print(" " + number);
            }
            number++;
        }
    }

    public static void test2(int n) {
        int number = 2;
        while (number <= n) {
            boolean isPrime = true;
            int square = (int) Math.sqrt(number);
            for (int divisor = 2; divisor <= square && isPrime; divisor++) {
                if (number % divisor == 0) {
                    isPrime = false;
                }
            }
            if (isPrime) {
                System.out.print(" " + number);
            }
            number++;
        }
    }

    public static void test3(int n) {
        boolean[] primes = new boolean[n + 1];
        for (int i = 0; i <= n; i++) {
            primes[i] = true;
        }

        for (int k = 2; k <= n / k; k++) {
            if (primes[k]) {
                for (int i = k; i <= n / k; i++) {
                    primes[k * i] = false;
                }
            }
        }

        for (int i = 2; i < primes.length; i++) {
            if (primes[i]) {
                System.out.print(" " + i);
            }
        }
    }
}
