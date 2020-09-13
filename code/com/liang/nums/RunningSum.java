package com.liang.nums;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class RunningSum {
    public static void main(String[] args) {
        RunningSum rs = new RunningSum();
        rs.runningSum(new int[]{3, 1, 2, 10, 1});
        rs.numIdenticalPairs(new int[]{1, 1, 1, 1});
        rs.numIdenticalPairs2(new int[]{1, 2, 3, 1, 1, 3});

        System.out.println(rs.xorOperation(1, 7));
        System.out.println(rs.xorOperation(5, 0));
        System.out.println(rs.xorOperation(4, 3));

        System.out.println(rs.diagonalSum(new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}));

        System.out.println(rs.numTeams(new int[]{2, 5, 3, 4, 1}));
    }

    public int[] runningSum(int[] nums) {
        int tmp = 0;
        for (int i = 0; i < nums.length; i++) {
            tmp += nums[i];
            nums[i] = tmp;
        }
        return nums;
    }

    /**
     * 如果一组数字 (i,j) 满足 nums[i] == nums[j] 且 i < j ，就可以认为这是一组 好数对 。
     */
    public int numIdenticalPairs(int[] nums) {
        int res = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] == nums[j]) {
                    res++;
                }
            }
        }
        return res;
    }

    public int numIdenticalPairs2(int[] nums) {
        int res = 0;
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : nums) {
            int valve = map.getOrDefault(num, 0);
            res += valve;
            map.put(num, valve + 1);
        }
        return res;
    }

    /**
     * 数组 nums 定义为：nums[i] = start + 2*i（下标从 0 开始）且 n == nums.length 。
     */
    public int xorOperation(int n, int start) {
        int res = start;
        for (int i = 1; i < n; i++) {
            res ^= (start + 2 * i);
        }
        return res;
    }

    /**
     * 请你返回在矩阵主对角线上的元素和副对角线上且不在主对角线上元素的和。
     */
    public int diagonalSum(int[][] mat) {
        int res = 0;
        if (mat == null || mat.length == 0) {
            return res;
        }
        int m = mat.length, n = mat[0].length;
        int i = 0, j = 0, r = m - 1, c = n - 1;
        while (i < r && j < c) {
            res += mat[i][j];
            res += mat[i][c];
            res += mat[r][j];
            res += mat[r][c];
            i++;
            j++;
            r--;
            c--;
        }
        if (i == r && j == c) {
            res += mat[i][j];
        }
        return res;
    }

    /**
     * 作战单位需满足： rating[i] < rating[j] < rating[k] 或者 rating[i] > rating[j] > rating[k] ，其中  0 <= i < j < k < n
     */
    public int numTeams(int[] rating) {
        int len = rating.length;
        if (len <= 2) {
            return 0;
        }
        int[] minToMax = new int[len];
        int[] maxToMin = new int[len];
        int result = 0;
        for (int i = 0; i < len; i++) {
            for (int j = i - 1; j >= 0; j--) {
                if (rating[i] > rating[j]) {
                    minToMax[i]++;
                    result += minToMax[j];
                }
                if (rating[i] < rating[j]) {
                    maxToMin[i]++;
                    result += maxToMin[j];
                }
            }
        }
        return result;
    }
}
