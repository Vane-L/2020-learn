package com.leet.dp;

import java.util.List;

/**
 * @Author: wenhongliang
 */
public class Triangle {
    /**
     * 给定一个三角形，找出自顶向下的最小路径和。
     * 每一步只能移动到下一行的相邻节点。
     * 相邻节点是指下标与上一层节点下标相同或者上一层节点下标+1的两个节点。
     * 如果当前行的下标为i，那么下一行的下标为i或i+1。
     */
    public int minimumTotal(List<List<Integer>> triangle) {
        int n = triangle.size();
        int[] dp = new int[n + 1];
        for (int i = n - 1; i >= 0; i--) {
            List<Integer> list = triangle.get(i);
            for (int j = 0; j < list.size(); j++) {
                dp[j] = Math.min(dp[j], dp[j + 1]) + list.get(j);
            }
        }
        return dp[0];
    }
}
