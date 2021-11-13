package com.basic;

/**
 * @Author: wenhongliang
 */
public class Palindrome {
    public static void main(String[] args) {
        System.out.println("noon is " + isPalindrome("noon"));
        System.out.println("moon is " + isPalindrome("moon"));
    }

    public static boolean isPalindrome(String s) {
        int low = 0, high = s.length() - 1;
        while (low < high) {
            if (s.charAt(low) != s.charAt(high)) {
                return false;
            }
            low++;
            high--;
        }
        return true;
    }
}
