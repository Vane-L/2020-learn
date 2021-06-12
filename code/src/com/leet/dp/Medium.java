package com.leet.dp;

/**
 * @Author: wenhongliang
 */
public class Medium {
    /**
     * 926. 将字符串翻转到单调递增
     * 输入："010110"
     * 输出：2
     * 解释：我们翻转得到 011111，或者是 000111。
     */
    public int minFlipsMonoIncrDP(String S) {
        int len = S.length();
        int[] dp = new int[len];
        int numOfOne = 0;
        for (int i = 0; i < len; i++) {
            if (S.charAt(i) == '1') {
                dp[i] = (i == 0 ? 0 : Math.min(dp[i - 1], numOfOne + 1));
                numOfOne++;
            } else {
                dp[i] = (i == 0 ? 0 : Math.min(dp[i - 1] + 1, numOfOne));
            }
        }
        return dp[len - 1];
    }

    /**
     * dp[i]表示前i个字符所需最小翻转次数，则dp[i+1]有如下四种情况，取其最小值即可
     * 1、第i+1个字符为0，且不翻转，d[i+1]=前i个字符中1的个数
     * 2、第i+1个字符为0，且翻转，d[i+1]=d[i]+1
     * 3、第i+1个字符为1，且不翻转，d[i+1]=d[i]
     * 4、第i+1个字符为1，且翻转，d[i+1]=前i个字符中1的个数+1
     * 其中1的个数可以在遍历S的时候记录下来。
     */

    public int minFlipsMonoIncrNum(String S) {
        int len = S.length();
        int numOfOne = 0;
        int before = 0;
        for (int i = 0; i < len; i++) {
            if (S.charAt(i) == '1') {
                before = (i == 0 ? 0 : Math.min(before, numOfOne + 1));
                numOfOne++;
            } else {
                before = (i == 0 ? 0 : Math.min(before + 1, numOfOne));
            }
        }
        return before;
    }

    // 5.最长回文子串
    public String longestPalindrome(String s) {
        if (s.length() < 2) {
            return s;
        }
        String res = "";
        for (int i = 0; i < s.length(); i++) {
            String res1 = palindrome(s, i, i);
            String res2 = palindrome(s, i, i + 1);
            res = res.length() > res1.length() ? res : res1;
            res = res.length() > res2.length() ? res : res2;
        }
        return res;
    }

    // 寻找回文串
    public String palindrome(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        return s.substring(left + 1, right);
    }
}
