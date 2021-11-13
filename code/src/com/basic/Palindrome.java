package com.basic;

/**
 * @Author: wenhongliang
 */
public class Palindrome {
    public static void main(String[] args) {
        System.out.println("noon is " + isPalindrome("noon"));
        System.out.println("moon is " + isPalindrome("moon"));

        System.out.println("noon is " + isPalindromeD("noon"));
        System.out.println("moon is " + isPalindromeD("moon"));

        System.out.println("noon is " + isPalindromeD("noon", 0, 3));
        System.out.println("moon is " + isPalindromeD("moon", 0, 3));
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

    public static boolean isPalindromeD(String s) {
        if (s.length() <= 1) {
            return true;
        }
        if (s.charAt(0) != s.charAt(s.length() - 1)) {
            return false;
        }
        return isPalindrome(s.substring(1, s.length() - 1));
    }

    public static boolean isPalindromeD(String s, int low, int high) {
        if (high <= low) return true;
        if (s.charAt(low) != s.charAt(high)) return false;
        return isPalindromeD(s, low + 1, high - 1);
    }


}
