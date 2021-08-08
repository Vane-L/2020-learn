package com.leet.week;

import java.util.PriorityQueue;

/**
 * @Author: wenhongliang
 */
public class WeekTwo {
    /**
     * 输入：s = "iloveleetcode", words = ["i","love","leetcode","apples"]
     * 输出：true
     * 解释：
     * s 可以由 "i"、"love" 和 "leetcode" 相连得到。
     */
    public boolean isPrefixString(String s, String[] words) {
        for (int i = 0; i < words.length; i++) {
            if (s.contains(words[i])) {
                s = s.substring(s.indexOf(words[i]) + words[i].length());
            } else {
                return false;
            }
            if (s.length() == 0) {
                return true;
            }
        }
        return s.length() == 0;
    }

    /**
     * 输入：piles = [5,4,9], k = 2
     * 输出：12
     * 解释：可能的执行情景如下：
     * - 对第 2 堆石子执行移除操作，石子分布情况变成 [5,4,5] 。
     * - 对第 0 堆石子执行移除操作，石子分布情况变成 [3,4,5] 。
     * 剩下石子的总数为 12 。
     */
    public int minStoneSum0(int[] piles, int k) {
        PriorityQueue<Integer> queue = new PriorityQueue<>((o1, o2) -> (o2 - o1));
        for (int n : piles) {
            queue.add(n);
        }
        while (k > 0 && !queue.isEmpty()) {
            int pop = queue.poll();
            // 除以2等于右移一位，位运算更快
            int temp = pop - (pop >> 1);
            queue.add(temp);
            k--;
        }
        return queue.stream().reduce(Integer::sum).get();
    }

    int min = Integer.MAX_VALUE;

    public int minStoneSum1(int[] piles, int k) {
        int sum = 0;
        for (int x : piles) {
            sum += x;
        }
        back(piles, k, sum);
        return min;
    }

    private void back(int[] piles, int k, int sum) {
        if (k <= 0) {
            min = Math.min(min, sum);
            return;
        }
        for (int i = 0; i < piles.length; i++) {
            double tmp = Math.floor(piles[i] / 2.0);
            piles[i] -= tmp;
            back(piles, k - 1, (int) (sum - tmp));
            piles[i] += tmp;
        }
    }

    /**
     * 输入：s = "][]["
     * 输出：1
     * 解释：交换下标 0 和下标 3 对应的括号，可以使字符串变成平衡字符串。
     * 最终字符串变成 "[[]]" 。
     */
    public int minSwaps(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '[') {
                count++;
            } else if (count > 0) {
                count--;
            }
        }
        return count == 0 ? 0 : count / 2 + count % 2;
    }

    /**
     * 输入：obstacles = [1,2,3,2]
     * 输出：[1,2,3,3]
     * 解释：每个位置的最长有效障碍路线是：
     * - i = 0: [1], [1] 长度为 1
     * - i = 1: [1,2], [1,2] 长度为 2
     * - i = 2: [1,2,3], [1,2,3] 长度为 3
     * - i = 3: [1,2,3,2], [1,2,2] 长度为 3
     */
    public int[] longestObstacleCourseAtEachPosition(int[] obstacles) {
        int len = obstacles.length;
        int[] stack = new int[len];
        int top = -1;
        int[] res = new int[len];
        for (int i = 0; i < len; i++) {
            if (top == -1 || obstacles[i] >= stack[top]) {
                stack[++top] = obstacles[i];
                res[i] = top + 1;
            } else {
                //二分
                int l = 0, r = top, m;
                while (l < r) {
                    m = l + (r - l) / 2;
                    if (stack[m] <= obstacles[i]) {
                        l = m + 1;
                    } else {
                        r = m;
                    }
                }
                stack[r] = obstacles[i];
                res[i] = r + 1;
            }
        }
        return res;
    }
}
