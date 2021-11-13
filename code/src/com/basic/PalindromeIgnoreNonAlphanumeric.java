package com.basic;


/**
 * @Author: wenhongliang
 */
public class PalindromeIgnoreNonAlphanumeric {
    public static void main(String[] args) {
        System.out.println(isPalindrome("aaa"));
        System.out.println(isPalindrome("aba"));
        System.out.println(isPalindrome("abc"));
    }

    public static boolean isPalindrome(String s) {
        String s1 = filter(s);
        String s2 = reverse(s1);
        return s2.equals(s1);
    }


    public static String filter(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }
}
