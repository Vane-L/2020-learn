package com.leet.week;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class Week18 {
    public static void main(String[] args) {
        System.out.println(new Week18().getDescentPeriods(new int[]{12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 4, 3, 10, 9, 8, 7}));
        System.out.println(new Week18().getDescentPeriodsDP(new int[]{12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 4, 3, 10, 9, 8, 7}));
        System.out.println(new Week18().kIncreasingError(new int[]{12, 6, 12, 6, 14, 2, 13, 17, 3, 8, 11, 7, 4, 11, 18, 8, 8, 3}, 1));
        System.out.println(new Week18().kIncreasing(new int[]{12, 6, 12, 6, 14, 2, 13, 17, 3, 8, 11, 7, 4, 11, 18, 8, 8, 3}, 1));
    }

    /**
     * Medium5958. 股票平滑下跌阶段的数目
     */
    public long getDescentPeriodsDP(int[] prices) {
        long[] dp = new long[prices.length];
        Arrays.fill(dp, 1);
        for (int i = 1; i < prices.length; i++) {
            if (prices[i] == prices[i - 1] - 1)
                dp[i] = dp[i - 1] + 1;
        }
        return Arrays.stream(dp).sum();
    }

    public long getDescentPeriods(int[] prices) {
        long res = 0;
        for (int i = 0; i < prices.length; ) {
            int j = i;
            while (j < prices.length && prices[j] - prices[i] == i - j) j++;
            res += (long) (j - i) * (j - i + 1) / 2;
            i = j;
        }
        return res;
    }

    /**
     * Hard 5959. 使数组 K 递增的最少操作次数
     * 对于一个数组，最少变换操作使该数组不递减 == 寻找该数组最长的不递减序列，对序列外的元素进行变换
     * 若求得最长不递减子序列的长度为 L，则最少的变换次数为 arr.length - L。
     */
    public int kIncreasingError(int[] arr, int k) {
        int res = 0;
        for (int i = k; i < arr.length; i++) {
            if (arr[i - k] > arr[i]) {
                res++;
            }
        }
        return res;
    }

    public int kIncreasing(int[] arr, int k) {
        List<Integer>[] lists = new ArrayList[k];
        // 初始化 k 个组
        for (int i = 0; i < k; i++) {
            lists[i] = new ArrayList<>();
        }
        for (int i = 0; i < arr.length; i++) {
            lists[i % k].add(arr[i]);
        }
        int sum = 0;
        // 分组求和
        for (List<Integer> list : lists) {
            sum += list.size() - lengthOfLIS(list);
        }
        return sum;
    }

    public int lengthOfLIS(List<Integer> nums) {
        int[] tails = new int[nums.size()];
        int size = 0;
        for (int x : nums) {
            int i = 0;
            int j = size;
            while (i != j) {
                int m = (i + j) / 2;
                if (tails[m] <= x)
                    i = m + 1;
                else
                    j = m;
            }
            tails[i] = x;
            if (i == size)
                size++;
        }
        return size;
    }

}
