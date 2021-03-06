package com.leet.string;

/**
 * @Author: wenhongliang
 */
public class Easy {

    public boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;
        while (left <= right) {
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }
            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }
            if (left < right) {
                if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                    return false;
                }
                left++;
                right--;
            }
        }
        return true;
    }
}
