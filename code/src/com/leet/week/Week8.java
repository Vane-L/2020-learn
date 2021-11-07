package com.leet.week;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * @Author: wenhongliang
 */
public class Week8 {
    /**
     * Easy 暴力破解
     * 给你三个整数数组 nums1、nums2 和 nums3 ，请你构造并返回一个 不同 数组，且由 至少 在 两个 数组中出现的所有值组成。
     * 数组中的元素可以按 任意 顺序排列。
     * 提示:
     * 1 <= nums1.length, nums2.length, nums3.length <= 100
     * 1 <= nums1[i], nums2[j], nums3[k] <= 100
     */
    public List<Integer> twoOutOfThree(int[] nums1, int[] nums2, int[] nums3) {
        List<Integer> res = new ArrayList<>();
        int[] arr1 = new int[101];
        int[] arr2 = new int[101];
        int[] arr3 = new int[101];
        for (int n : nums1) arr1[n] = 1;
        for (int n : nums2) arr2[n] = 1;
        for (int n : nums3) arr3[n] = 1;
        for (int i = 1; i <= 100; i++) {
            if (arr1[i] + arr2[i] + arr3[i] > 1) {
                res.add(i);
            }
        }
        return res;
    }

    /**
     * Medium 贪心排序找中位数
     * 给你一个大小为 m x n 的二维整数网格 grid 和一个整数 x 。每一次操作，你可以对 grid 中的任一元素 加 x 或 减 x 。
     * 单值网格 是全部元素都相等的网格。
     * 返回使网格化为单值网格所需的 最小 操作数。如果不能，返回 -1 。
     */
    public int minOperations(int[][] grid, int x) {
        int m = grid.length, n = grid[0].length;
        int[] arr = new int[m * n];
        int idx = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                arr[idx++] = grid[i][j];
            }
        }
        Arrays.sort(arr);
        int mid = arr[(n * m) / 2];
        int sum = 0;
        for (int a : arr) {
            int l = Math.abs(mid - a);
            if (l % x != 0) {
                return -1;
            }
            sum += l / x;
        }
        return sum;
    }

    /**
     * Medium 两个有序字典
     * 给你一支股票价格的数据流。数据流中每一条记录包含一个 时间戳 和该时间点股票对应的 价格 。
     * 不巧的是，由于股票市场内在的波动性，股票价格记录可能不是按时间顺序到来的。
     * 某些情况下，有的记录可能是错的。如果两个有相同时间戳的记录出现在数据流中，前一条记录视为错误记录，后出现的记录 更正 前一条错误的记录。
     * 请你设计一个算法，实现：
     * 更新 股票在某一时间戳的股票价格，如果有之前同一时间戳的价格，这一操作将 更正 之前的错误价格。
     * 找到当前记录里 最新股票价格 。最新股票价格 定义为时间戳最晚的股票价格。
     * 找到当前记录里股票的 最高价格 。
     * 找到当前记录里股票的 最低价格 。
     */

    TreeMap<Integer, Integer> tp = new TreeMap<>();
    TreeMap<Integer, Integer> pt = new TreeMap<>();

    public void update(int timestamp, int price) {
        if (tp.containsKey(timestamp)) {
            int old = tp.get(timestamp);
            tp.put(timestamp, price);

            pt.put(old, pt.get(old) - 1);
            if (pt.get(old) == 0) {
                pt.remove(old);
            }
        } else {
            tp.put(timestamp, price);
        }
        pt.put(price, pt.getOrDefault(price, 0) + 1);
    }

    public int current() {
        return tp.lastEntry().getValue();
    }

    public int maximum() {
        return pt.lastEntry().getKey();

    }

    public int minimum() {
        return pt.firstEntry().getKey();
    }

}
