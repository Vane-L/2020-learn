package com.liang.nums;


import java.util.HashMap;
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
}
