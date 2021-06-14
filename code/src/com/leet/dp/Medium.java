package com.leet.dp;

import java.util.Arrays;

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

    public boolean canJump(int[] nums) {
        int tmp = 0;
        for (int i = 0; i <= tmp; i++) {
            tmp = Math.max(tmp, i + nums[i]);
            if (tmp >= nums.length - 1) {
                return true;
            }
        }
        return false;
    }

    public int uniquePaths(int m, int n) {
        int[][] dp = new int[m][n];
        for (int i = 0; i < n; i++) {
            dp[0][i] = 1;
        }
        for (int i = 0; i < m; i++) {
            dp[i][0] = 1;
        }
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
            }
        }
        return dp[m - 1][n - 1];
    }

    // [1,2,5] 11
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (i >= coin && dp[i - coin] != amount + 1) {
                    dp[i] = Math.min(dp[i], dp[i - coin] + 1);
                }
            }
        }
        return dp[amount] == amount + 1 ? -1 : dp[amount];
    }

    public int lengthOfLIS(int[] nums) {
        int max = Integer.MIN_VALUE;
        int[] dp = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            dp[i] = 1;
            for (int j = 0; j < i; j++) {
                if (nums[i] > nums[j]) {
                    dp[i] = Math.max(dp[i], dp[j] + 1);
                }
            }
            max = Math.max(max, dp[i]);
        }
        return max;
    }
}
