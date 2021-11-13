package com.basic;

/**
 * @Author: wenhongliang
 */
public class GreatestCommonDivisor {
    public static void main(String[] args) {
        int n1 = 125;
        int n2 = 2525;
        int gcd1 = 1;
        int k = 2;
        while (k <= n1 && k <= n2) {
            if (n1 % k == 0 && n2 % k == 0) {
                gcd1 = k;
            }
            k++;
        }
        int gcd2 = 1;
        for (int i = 2; i <= n1 && i <= n2; i++) {
            if (n1 % i == 0 && n2 % i == 0) {
                gcd2 = i;
            }
        }

        System.out.println("gcd for " + n1 + " and " + n2 + " is " + gcd1);
        System.out.println("gcd for " + n1 + " and " + n2 + " is " + gcd2);

        System.out.println(gcd(125, 2525));

        System.out.println(sum(5));
        System.out.println(sum2(2));
    }

    public static int gcd(int m, int n) {
        if (m % n == 0) return n;
        return gcd(n, m % n);
    }

    public static double sum(double n) {
        if (n <= 1) return 1;
        return (1.0 / n) + sum(n - 1);
    }

    public static double sum2(double n) {
        if (n < 1) return 0;
        return (n / ( n + 1)) + sum2(n - 1);
    }
}
