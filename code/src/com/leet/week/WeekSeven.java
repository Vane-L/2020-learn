package com.leet.week;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @Author: wenhongliang
 */
public class WeekSeven {
    /**
     * Easy 入门题
     * 给你一个下标从 0 开始的字符串 word 和一个字符 ch 。找出 ch 第一次出现的下标 i ，反转 word 中从下标 0 开始、直到下标 i 结束（含下标 i ）的那段字符。
     * 如果 word 中不存在字符 ch ，则无需进行任何操作。
     * 例如，如果 word = "abcdefd" 且 ch = "d" ，那么你应该 反转 从下标 0 开始、直到下标 3 结束（含下标 3 ）。结果字符串将会是 "dcbaefd" 。
     * 返回 结果字符串 。
     */
    public String reversePrefix(String word, char ch) {
        int idx = word.indexOf(ch);
        char[] chars = word.toCharArray();
        int left = 0, right = idx;
        while (left < right) {
            char tmp = chars[left];
            chars[left] = chars[right];
            chars[right] = tmp;
            left++;
            right--;
        }
        return new String(chars);
    }

    /**
     * Medium 哈希表暴力
     * 用一个下标从 0 开始的二维整数数组 rectangles 来表示 n 个矩形，其中 rectangles[i] = [widthi, heighti] 表示第 i 个矩形的宽度和高度。
     * 如果两个矩形 i 和 j（i < j）的宽高比相同，则认为这两个矩形 可互换 。更规范的说法是，两个矩形满足 widthi/heighti == widthj/heightj（使用实数除法而非整数除法），则认为这两个矩形 可互换 。
     * 计算并返回 rectangles 中有多少对 可互换 矩形。
     */
    class Solution {
        public long interchangeableRectangles(int[][] rectangles) {
            // 宽高比-数
            Map<Double, Long> map = new HashMap<>();
            for (int i = 0; i < rectangles.length; i++) {
                double ratio = (double) (rectangles[i][0] * 1.0 / rectangles[i][1]);
                map.put(ratio, map.getOrDefault(ratio, 0L) + 1);
            }
            long res = 0;
            for (Map.Entry<Double, Long> entry : map.entrySet()) {
                res += (entry.getValue() - 1) * entry.getValue() / 2;
            }
            return res;
        }
    }

    /**
     * Medium 失败的第三道题
     * 给你一个字符串 s ，请你找到 s 中两个 不相交回文子序列 ，使得它们长度的 乘积最大 。两个子序列在原字符串中如果没有任何相同下标的字符，则它们是 不相交 的。
     * 请你返回两个回文子序列长度可以达到的 最大乘积 。
     * 子序列 指的是从原字符串中删除若干个字符（可以一个也不删除）后，剩余字符不改变顺序而得到的结果。
     * 如果一个字符串从前往后读和从后往前读一模一样，那么这个字符串是一个 回文字符串 。
     */

    public int maxProduct(String s) {
        return maxProduct(0, "", "", s);
    }

    private int maxProduct(int index, String a, String b, String s) {
        return index == s.length() ? longestPalindromeSubseq(a) * longestPalindromeSubseq(b)
                : Math.max(maxProduct(index + 1, a + s.charAt(index), b, s),
                maxProduct(index + 1, a, b + s.charAt(index), s));
    }

    public int longestPalindromeSubseq(String s) {
        if (s == null || s.isEmpty())
            return 0;
        int len = s.length();
        int[][] dp = new int[len][len];
        for (int i = len - 1; i >= 0; i--) {
            dp[i][i] = 1;
            for (int j = i + 1; j < len; j++) {
                if (s.charAt(i) == s.charAt(j)) {
                    dp[i][j] = 2 + dp[i + 1][j - 1];
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[0][len - 1];
    }

    /**
     * Hard 失败的第四道题
     * 有一棵根节点为 0 的 家族树 ，总共包含 n 个节点，节点编号为 0 到 n - 1 。
     * 给你一个下标从 0 开始的整数数组 parents ，其中 parents[i] 是节点 i 的父节点。由于节点 0 是 根 ，所以 parents[0] == -1 。
     * 总共有 105 个基因值，每个基因值都用 闭区间 [1, 105] 中的一个整数表示。
     * 给你一个下标从 0 开始的整数数组 nums ，其中 nums[i] 是节点 i 的基因值，且基因值 互不相同 。
     * 请你返回一个数组 ans ，长度为 n ，其中 ans[i] 是以节点 i 为根的子树内 缺失 的 最小 基因值。
     * 节点 x 为根的 子树 包含节点 x 和它所有的 后代 节点。
     * 输入：parents = [-1,0,0,2], nums = [1,2,3,4]
     * 输出：[5,1,1,1]
     * 解释：每个子树答案计算结果如下：
     * - 0：子树包含节点 [0,1,2,3] ，基因值分别为 [1,2,3,4] 。5 是缺失的最小基因值。
     * - 1：子树只包含节点 1 ，基因值为 2 。1 是缺失的最小基因值。
     * - 2：子树包含节点 [2,3] ，基因值分别为 [3,4] 。1 是缺失的最小基因值。
     * - 3：子树只包含节点 3 ，基因值为 4 。1是缺失的最小基因值。
     */

    public int[] smallestMissingValueSubtree(int[] parents, int[] nums) {
        int len = nums.length;
        int[] result = new int[len];
        boolean[] visited = new boolean[len];
        TreeSet<Integer> set = new TreeSet<>();
        ArrayList<Integer>[] list = new ArrayList[len];
        for (int i = 0; i < len; i++) {
            // 默认最小的基因值为1
            result[i] = 1;
            set.add(1 + i);
            list[i] = new ArrayList<>();
        }
        set.add(1 + len);
        for (int i = 1; i < len; i++) {
            list[parents[i]].add(i);
        }
        for (int i = 0; i < len; i++) {
            if (nums[i] == 1) {
                for (int j = i; j >= 0; j = parents[j]) {
                    smallestMissingValueSubtree(j, set, visited, list, nums);
                    result[j] = set.first();
                }
                break;
            }
        }
        return result;
    }

    private void smallestMissingValueSubtree(int i, TreeSet<Integer> set, boolean[] visited,
                                             ArrayList<Integer>[] list, int[] nums) {
        if (!visited[i]) {
            visited[i] = true;
            set.remove(nums[i]);
            for (int j : list[i]) {
                smallestMissingValueSubtree(j, set, visited, list, nums);
            }
        }
    }

}
