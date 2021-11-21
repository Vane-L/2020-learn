package com.leet.week;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class Week14 {
    /**
     * Medium 5186. 区间内查询数字的频率 [二分查找]
     * 请你设计一个数据结构，它能求出给定子数组内一个给定值的 频率 。
     * 子数组中一个值的 频率 指的是这个子数组中这个值的出现次数。
     * 请你实现 RangeFreqQuery 类：
     * RangeFreqQuery(int[] arr) 用下标从 0 开始的整数数组 arr 构造一个类的实例。
     * int query(int left, int right, int value) 返回子数组 arr[left...right] 中 value 的 频率 。
     * 一个 子数组 指的是数组中一段连续的元素。arr[left...right] 指的是 nums 中包含下标 left 和 right 在内 的中间一段连续元素。
     */
    Map<Integer, List<Integer>> map = new HashMap<>();

    public void RangeFreqQuery(int[] arr) {
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            map.putIfAbsent(arr[i], new ArrayList<>());
            map.get(arr[i]).add(i);
        }
    }

    public int query(int left, int right, int value) {
        if (!map.containsKey(value)) return 0;
        List<Integer> list = map.get(value);
        int rIdx = getIdx(list, right);
        int lIdx = getIdx(list, left - 1);
        return rIdx - lIdx;
    }

    int getIdx(List<Integer> list, int t) {
        int l = 0, r = list.size() - 1;
        while (l <= r) {
            int mid = (l + r) >> 1;
            int v = list.get(mid);
            if (v == t) {
                return mid;
            } else if (v < t) {
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }
        return r;
    }

    /**
     * Hard 5933. k 镜像数字的和
     * 一个 k 镜像数字 指的是一个在十进制和 k 进制下从前往后读和从后往前读都一样的 没有前导 0 的 正 整数。
     * 比方说，9 是一个 2 镜像数字。9 在十进制下为 9 ，二进制下为 1001 ，两者从前往后读和从后往前读都一样。
     * 相反地，4 不是一个 2 镜像数字。4 在二进制下为 100 ，从前往后和从后往前读不相同。
     * 给你进制 k 和一个数字 n ，请你返回 k 镜像数字中 最小 的 n 个数 之和 。
     */
    private boolean isGood(char[] c) {
        for (int i = 0, j = c.length - 1; i < j; i++, j--) {
            if (c[i] != c[j]) {
                return false;
            }
        }
        return true;
    }

    public long kMirror(int k, int n) {
        long ans = 0;
        for (int len = 1; ; len++) {
            int x = (int) Math.pow(10, (len - 1) / 2);
            int y = (int) Math.pow(10, (len + 1) / 2);
            for (int i = x; i < y; i++) {
                long nn = i;
                for (int j = len % 2 == 0 ? i : i / 10; j > 0; j /= 10) {
                    nn = nn * 10 + j % 10;
                }
                String ss = Long.toString(nn, k);
                if (isGood(ss.toCharArray())) {
                    ans += nn;
                    if (--n == 0) {
                        return ans;
                    }
                }
            }
        }
    }

}
