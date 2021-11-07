package com.leet.week;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author: wenhongliang
 */
public class Week11 {
    /**
     * 转化数字的最小运算数 Medium
     * 给你一个下标从 0 开始的整数数组 nums ，该数组由 互不相同 的数字组成。另给你两个整数 start 和 goal 。
     * 整数 x 的值最开始设为 start ，你打算执行一些运算使 x 转化为 goal 。你可以对数字 x 重复执行下述运算：
     * 如果 0 <= x <= 1000 ，那么，对于数组中的任一下标 i（0 <= i < nums.length），可以将 x 设为下述任一值：
     * x + nums[i]
     * x - nums[i]
     * x ^ nums[i]（按位异或 XOR）
     * 注意，你可以按任意顺序使用每个 nums[i] 任意次。使 x 越过 0 <= x <= 1000 范围的运算同样可以生效，但该该运算执行后将不能执行其他运算。
     * 返回将 x = start 转化为 goal 的最小操作数；如果无法完成转化，则返回 -1 。
     * 输入：nums = [1,3], start = 6, goal = 4
     * 输出：2
     * 解释：
     * 可以按 6 → 7 → 4 的转化路径进行，只需执行下述 2 次运算：
     * - 6 ^ 1 = 7
     * - 7 ^ 3 = 4
     */

    public int minimumOperations(int[] nums, int start, int goal) {
        boolean[] visited = new boolean[1001];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        int time = 1;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int poll = queue.poll();
                for (int num : nums) {
                    int a = poll + num;
                    int b = poll - num;
                    int c = (poll ^ num);
                    if (a == goal || b == goal || c == goal) {
                        return time;
                    }
                    if (a >= 0 && a <= 1000 && !visited[a]) {
                        visited[a] = true;
                        queue.add(a);
                    }
                    if (b >= 0 && b <= 1000 && !visited[b]) {
                        visited[b] = true;
                        queue.add(b);
                    }
                    if (c >= 0 && c <= 1000 && !visited[c]) {
                        visited[c] = true;
                        queue.add(c);
                    }
                }
            }
            time++;
        }
        return -1;
    }

    /**
     * 同源字符串检测 Hard
     * 原字符串由小写字母组成，可以按下述步骤编码：
     * 任意将其 分割 为由若干 非空 子字符串组成的一个 序列 。
     * 任意选择序列中的一些元素（也可能不选择），然后将这些元素替换为元素各自的长度（作为一个数字型的字符串）。
     * 重新 顺次连接 序列，得到编码后的字符串。
     * 例如，编码 "abcdefghijklmnop" 的一种方法可以描述为：
     * 将原字符串分割得到一个序列：["ab", "cdefghijklmn", "o", "p"] 。
     * 选出其中第二个和第三个元素并分别替换为它们自身的长度。序列变为 ["ab", "12", "1", "p"] 。
     * 重新顺次连接序列中的元素，得到编码后的字符串："ab121p" 。
     * 给你两个编码后的字符串 s1 和 s2 ，由小写英文字母和数字 1-9 组成。如果存在能够同时编码得到 s1 和 s2 原字符串，返回 true ；否则，返回 false。
     * 注意：生成的测试用例满足 s1 和 s2 中连续数字数不超过 3 。
     * 输入：s1 = "112s", s2 = "g841"
     * 输出：true
     * 解释："gaaaaaaaaaaaas" 可以作为原字符串
     * - "gaaaaaaaaaaaas"
     * -> 分割：       ["g", "aaaaaaaaaaaa", "s"]
     * -> 替换：       ["1", "12",           "s"]
     * -> 连接：       "112s"，得到 s1
     * - "gaaaaaaaaaaaas"
     * -> 分割：       ["g", "aaaaaaaa", "aaaa", "s"]
     * -> 替换：       ["g", "8",        "4",    "1"]
     * -> 连接         "g841"，得到 s2
     */

    public boolean possiblyEquals(String s1, String s2) {
        return possiblyEquals(0, 0, 0, 0, s1, s2, new HashMap<>());
    }

    private boolean possiblyEquals(int i, int j, int n, int m, String s1, String s2, HashMap<String, Boolean> map) {
        if (i == s1.length() && n == 0) {
            return j == s2.length() && m == 0;
        } else if (j == s2.length() && m == 0) {
            return false;
        } else if (n > 0 && m > 0) {
            return possiblyEquals(i, j, n - Math.min(n, m), m - Math.min(n, m), s1, s2, map);
        } else if (n > 0) {
            if (s2.charAt(j) >= 'a' && s2.charAt(j) <= 'z') {
                return possiblyEquals(i, j + 1, n - 1, m, s1, s2, map);
            } else if (!map.containsKey(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2)) {
                map.put(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2, false);
                for (int k = j; k < s2.length() && s2.charAt(k) >= '0' && s2.charAt(k) <= '9'; k++) {
                    if (possiblyEquals(i, k + 1, n, Integer.parseInt(s2.substring(j, k + 1)), s1, s2, map)) {
                        map.put(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2, true);
                        break;
                    }
                }
            }
            return map.get(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2);
        } else if (m > 0) {
            return possiblyEquals(j, i, m, n, s2, s1, map);
        } else if (s1.charAt(i) >= '0' && s1.charAt(i) <= '9') {
            if (!map.containsKey(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2)) {
                map.put(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2, false);
                for (int k = i; k < s1.length() && s1.charAt(k) >= '0' && s1.charAt(k) <= '9'; k++) {
                    if (possiblyEquals(k + 1, j, Integer.parseInt(s1.substring(i, k + 1)), m, s1, s2, map)) {
                        map.put(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2, true);
                        break;
                    }
                }
            }
            return map.get(i + " " + j + " " + n + " " + m + " " + s1 + " " + s2);
        } else if (s2.charAt(j) >= '0' && s2.charAt(j) <= '9') {
            return possiblyEquals(j, i, m, n, s2, s1, map);
        } else {
            return s1.charAt(i) == s2.charAt(j) && possiblyEquals(i + 1, j + 1, 0, 0, s1, s2, map);
        }
    }
}
