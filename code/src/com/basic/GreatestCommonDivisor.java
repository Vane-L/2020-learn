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
    }
}
