package com.leet.week;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: wenhongliang
 */
public class Week4 {

    /**
     * 最大公约数
     * 给你一个整数数组 nums ，返回数组中最大数和最小数的 最大公约数 。
     * 两个数的 最大公约数 是能够被两个数整除的最大正整数。
     */
    public int findGCD(int[] nums) {
        Arrays.sort(nums);
        int min = nums[0], max = nums[nums.length - 1];
        for (int i = min; i >= 1; i--) {
            if (min % i == 0 && max % i == 0) {
                return i;
            }
        }
        return 1;
    }

    /**
     * 输入：nums = ["01","10"]
     * 输出："11"
     * 解释："11" 没有出现在 nums 中。"00" 也是正确答案。
     */
    String res = "";
    Set<String> set = new HashSet<>();

    public String findDifferentBinaryString(String[] nums) {
        int len = nums.length;
        for (String s : nums) {
            set.add(s);
        }
        dfs("", set, len);
        return res;
    }

    public void dfs(String cur, Set<String> set, int len) {
        if (cur.length() >= len) {
            if (!set.contains(cur)) {
                res = cur;
                return;
            }
        }
        for (int i = cur.length(); i < len; i++) {
            if (res.length() != len) {
                dfs(cur + '0', set, len);
                dfs(cur + '1', set, len);
            }
        }
    }

    /**
     * 给你一个大小为 m x n 的整数矩阵 mat 和一个整数 target 。
     * 从矩阵的 每一行 中选择一个整数，你的目标是 最小化 所有选中元素之 和 与目标值 target 的 绝对差 。
     * 返回 最小的绝对差 。
     */
    public int minimizeTheDifference0(int[][] mat, int target) {
        int n = mat.length;
        int m = mat[0].length;
        // dp[i][j]表示前i行选完，能否凑出和为j的方案
        boolean[][] dp = new boolean[n][4900 + 1];
        for (int j = 0; j < m; ++j) dp[0][mat[0][j]] = true;
        for (int i = 1; i < n; ++i) {
            for (int j = 0; j <= 4900; ++j) {
                for (int k = 0; k < m; ++k) {
                    if (j - mat[i][k] >= 0 && dp[i - 1][j - mat[i][k]]) {
                        dp[i][j] = true;
                        break;
                    }
                }

            }
        }

        int ans = 4900;
        for (int i = 1; i <= 4900; ++i)
            if (dp[n - 1][i])
                ans = Math.min(ans, Math.abs(target - i));
        return ans;
    }

    public int minimizeTheDifference(int[][] mat, int target) {
        int min = target;
        for (int j = 0; j < mat[0].length; j++) {
            min = Math.min(min, Math.abs(back(mat, 0, j, 0) - target));
        }
        return min;
    }

    public int back(int[][] mat, int row, int col, int sum) {
        if (row >= mat.length) return sum;
        for (int j = col; j < mat[0].length; j++) {
            return back(mat, row + 1, 0, sum + mat[row][j]);
        }
        return back(mat, row + 1, 0, sum);
    }


}
