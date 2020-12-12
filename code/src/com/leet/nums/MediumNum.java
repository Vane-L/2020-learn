package com.leet.nums;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class MediumNum {


    /**
     * 给你一份工作时间表 hours，上面记录着某一位员工每天的工作小时数。
     * 我们认为当员工一天中的工作小时数大于 8 小时的时候，那么这一天就是「劳累的一天」。
     * 所谓「表现良好的时间段」，意味在这段时间内，「劳累的天数」是严格 大于「不劳累的天数」。
     * 请你返回「表现良好时间段」的最大长度。
     * 示例 1：
     * 输入：hours = [9,9,6,0,6,6,9]
     * [1,1,-1,-1,-1,-1,1]
     * 输出：3
     * 解释：最长的表现良好时间段是 [9,9,6]。
     * Make a new array A of +1/-1s corresponding to if hours[i] is > 8 or not. The goal is to find the longest subarray with positive sum.
     * Using prefix sums (PrefixSum[i+1] = A[0] + A[1] + ... + A[i]), you need to find for each j, the smallest i < j with PrefixSum[i] + 1 == PrefixSum[j].
     */
    public int longestWPI(int[] hours) {
        int res = 0;
        int n = hours.length;
        Map<Integer, Integer> sumToIndex = new HashMap<>();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            int temp = hours[i] > 8 ? 1 : -1;
            sum += temp;
            if (sum > 0) {
                res = Math.max(res, i + 1);
            } else {
                if (!sumToIndex.containsKey(sum))
                    sumToIndex.put(sum, i);
                if (sumToIndex.containsKey(sum - 1))
                    res = Math.max(res, i - sumToIndex.get(sum - 1));
            }
        }
        return res;
    }

    /**
     * 如果连续数字之间的差严格地在正数和负数之间交替，则数字序列称为摆动序列。
     * 第一个差（如果存在的话）可能是正数或负数。
     * >>> 少于两个元素的序列也是摆动序列。
     * 例如，[1,7,4,9,2,5] 是一个摆动序列，因为差值 (6,-3,5,-7,3) 是正负交替出现的。
     * 相反, [1,4,7,2,5] 和 [1,7,4,5,5] 不是摆动序列，第一个序列是因为它的前两个差值都是正数，第二个序列是因为它的最后一个差值为零。
     * 给定一个整数序列，返回作为摆动序列的最长子序列的长度。
     * 通过从原始序列中删除一些（也可以不删除）元素来获得子序列，剩下的元素保持其原始顺序。
     */
    public int wiggleMaxLength(int[] nums) {
        int len = nums.length;
        if (len < 2) {
            return len;
        }
        int up = 1, down = 1;
        for (int i = 1; i < len; i++) {
            if (nums[i] < nums[i - 1]) {
                down = up + 1;
            } else if (nums[i] > nums[i - 1]) {
                up = down + 1;
            }
        }
        return Math.max(up, down);
    }

    /**
     * 给定一个由正整数组成且不存在重复数字的数组，找出和为给定目标正整数的组合的个数。
     * nums = [1, 2, 3], target = 4
     * 所有可能的组合为：
     * (1, 1, 1, 1)
     * (1, 1, 2)
     * (1, 2, 1)
     * (1, 3)
     * (2, 1, 1)
     * (2, 2)
     * (3, 1)
     * 请注意，顺序不同的序列被视作不同的组合
     */
    public int combinationSum4(int[] nums, int target) {
        if (nums == null) {
            return 0;
        }
        int[] dp = new int[target + 1];
        dp[0] = 1;
        for (int i = 1; i <= target; i++) {
            for (int x : nums) {
                if (i >= x) {
                    dp[i] += dp[i - x];
                }
            }
        }
        return dp[target];
    }


    /**
     * 给定一个 n x n 矩阵，其中每行和每列元素均按升序排序，找到矩阵中第 k 小的元素。
     * 请注意，它是排序后的第 k 小元素，而不是第 k 个不同的元素。
     * matrix = [
     * [ 1,  5,  9],
     * [10, 11, 13],
     * [12, 13, 15]
     * ], k = 8, 返回 13。
     */
    public int kthSmallest(int[][] matrix, int k) {
        int n = matrix.length;
        int left = matrix[0][0], right = matrix[n - 1][n - 1];
        while (left < right) {
            int mid = left + ((right - left) >> 1);
            int count = getCount(matrix, mid, n);
            if (count >= k) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }

    private int getCount(int[][] matrix, int mid, int n) {
        int i = n - 1;
        int j = 0;
        int num = 0;
        while (i >= 0 && j < n) {
            if (matrix[i][j] <= mid) {
                num += i + 1;
                j++;
            } else {
                i--;
            }
        }
        return num;
    }


    public static void main(String[] args) {
        MediumNum medium = new MediumNum();
        System.out.println(medium.longestWPI(new int[]{9, 9, 6, 0, 6, 6, 9}));
        System.out.println(medium.combinationSum4(new int[]{1, 2, 3}, 4));
    }

}
