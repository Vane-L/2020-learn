package com.leet.dp;

/**
 * @Author: wenhongliang
 */
public class HardString {

    public static void main(String[] args) {
        System.out.println(numDistinct("babgbag", "bag"));
    }

    /**
     * Problem 115
     * https://leetcode-cn.com/problems/distinct-subsequences/
     **/
    public static int numDistinct(String s, String t) {
        int m = s.length();
        int n = t.length();
        if (m < n) {
            return 0;
        }
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) {
            dp[i][n] = 1;
        }
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                dp[i][j] = dp[i + 1][j] + (s.charAt(i) == t.charAt(j) ? dp[i + 1][j + 1] : 0);
            }
        }
        return dp[0][0];
    }
}
