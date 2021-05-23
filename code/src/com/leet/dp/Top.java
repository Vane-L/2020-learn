package com.leet.dp;

import com.leet.tree.TreeNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: wenhongliang
 */
public class Top {
    // 395. 至少有 K 个重复字符的最长子串
    public int longestSubstring(String s, int k) {
        int len = s.length();
        if (len < k) {
            return 0;
        }
        return dfs(s, 0, len, k);
    }

    private int dfs(String s, int idx, int len, int k) {
        if (len - idx < k) {
            return 0;
        }
        int[] times = new int[26];
        for (int i = idx; i < len; i++) {
            times[s.charAt(i) - 'a']++;
        }
        while (len - idx >= k && times[s.charAt(idx) - 'a'] < k) {
            idx++;
        }
        while (len - idx >= k && times[s.charAt(len - 1) - 'a'] < k) {
            len--;
        }
        if (len - idx < k) {
            return 0;
        }
        for (int i = idx; i < len; i++) {
            if (times[s.charAt(i) - 'a'] < k) {
                return Math.max(dfs(s, idx, i, k), dfs(s, i + 1, len, k));
            }
        }
        return len - idx;
    }

    // 124. 二叉树中的最大路径和
    int maxSum = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        sum(root);
        return maxSum;
    }

    private int sum(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int maxLeft = Math.max(0, sum(root.left));
        int maxRight = Math.max(0, sum(root.right));
        maxSum = Math.max(maxSum, root.val + maxLeft + maxRight);
        return root.val + Math.max(maxLeft, maxRight);
    }

    // 128. 最长连续序列
    public int longestConsecutive(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int max = 0;
        Set<Integer> set = new HashSet<>();
        for (int x : nums) {
            set.add(x);
        }
        for (int x : nums) {
            if (set.contains(x - 1)) {
                continue;
            } else {
                int len = 0;
                while (set.contains(x++)) {
                    len++;
                }
                max = Math.max(max, len);
            }
        }
        return max;
    }

    // 打家劫舍
    public int rob(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        if (nums.length == 2) {
            return Math.max(nums[0], nums[1]);
        }
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < nums.length; i++) {
            dp[i] = Math.max(dp[i - 2] + nums[i], dp[i - 1]);
        }
        return dp[nums.length - 1];
    }


    // 完全平方数
    public int numSquares(int n) {
        int[] dp = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            dp[i] = i;
            for (int j = 2; j * j <= i; j++) {
                dp[i] = Math.min(dp[i - j * j] + 1, dp[i]);
            }
        }
        return dp[n];
    }

    // 最长上升子序列
    public int lengthOfLIS(int[] nums) {
        int[] dp = new int[nums.length];
        int max = Integer.MIN_VALUE;
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

    // 最长上升子序列
    public int lengthOfLIS2(int[] nums) {
        int[] dp = new int[nums.length];
        int res = 0;
        for (int num : nums) {
            int i = 0, j = res;
            while (i < j) {
                int m = (i + j) / 2;
                if (dp[m] < num) i = m + 1;
                else j = m;
            }
            dp[i] = num;
            if (res == j) res++;
        }
        return res;
    }

    // 零钱兑换
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

    public static void main(String[] args) {
        new Top().longestIncreasingPath(new int[][]{{9, 9, 4}, {6, 6, 8}, {2, 1, 1}});
    }

    // 矩阵中的最长递增路径
    public int longestIncreasingPath(int[][] matrix) {
        int m = matrix.length, n = matrix[0].length;
        int res = 0;
        int[][] used = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (used[i][j] == 0) {
                    //这里先做一次比较找出max，可以避免最后再去遍历一个visited数组
                    res = Math.max(res, board(matrix, i, j, used));
                }
                res = Math.max(res, used[i][j]);
            }
        }
        return res;
    }

    private int board(int[][] matrix, int i, int j, int[][] used) {
        if (i < 0 || i >= matrix.length || j < 0 || j >= matrix[0].length) {
            return 0;
        }
        if (used[i][j] > 0) {
            return used[i][j];
        }
        int num = 0;
        //这里分别去判断上下左右是否比当前数小，然后去递归遍历
        if (i - 1 >= 0 && matrix[i - 1][j] < matrix[i][j]) {
            num = Math.max(num, board(matrix, i - 1, j, used));
        }
        if (i + 1 < matrix.length && matrix[i + 1][j] < matrix[i][j]) {
            num = Math.max(num, board(matrix, i + 1, j, used));
        }
        if (j - 1 >= 0 && matrix[i][j - 1] < matrix[i][j]) {
            num = Math.max(num, board(matrix, i, j - 1, used));
        }
        if (j + 1 < matrix[0].length && matrix[i][j + 1] < matrix[i][j]) {
            num = Math.max(num, board(matrix, i, j + 1, used));
        }
        used[i][j] = num + 1;
        return num + 1;
    }
}
