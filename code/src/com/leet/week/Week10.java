package com.leet.week;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

/**
 * @Author: wenhongliang
 */
public class Week10 {

    /**
     * Easy
     * 句子仅由小写字母（'a' 到 'z'）、数字（'0' 到 '9'）、连字符（'-'）、标点符号（'!'、'.' 和 ','）以及空格（' '）组成。每个句子可以根据空格分解成 一个或者多个 token ，这些 token 之间由一个或者多个空格 ' ' 分隔。
     * 如果一个 token 同时满足下述条件，则认为这个 token 是一个有效单词：
     * 仅由小写字母、连字符和/或标点（不含数字）。
     * 至多一个 连字符 '-' 。如果存在，连字符两侧应当都存在小写字母（"a-b" 是一个有效单词，但 "-ab" 和 "ab-" 不是有效单词）。
     * 至多一个 标点符号。如果存在，标点符号应当位于 token 的 末尾 。
     */
    public int countValidWords(String sentence) {
        int res = 0;
        int idx = 0, len = sentence.length();
        char[] ch = sentence.toCharArray();
        while (idx < len) {
            while (idx < len && ch[idx] == ' ') {
                idx++;
            }
            int start = idx, end = idx;
            while (end < len && ch[end] != ' ') {
                end++;
            }
            if (valid(ch, start, end)) {
                res++;
            }
            idx = end;
        }
        return res;
    }

    public boolean valid(char[] ch, int start, int end) {
        if (start >= end) return false;
        int a = 0, b = 0;
        for (int i = start; i < end; i++) {
            if (ch[i] >= '0' && ch[i] <= '9') {
                return false;
            }
            if (ch[i] == '-') {
                a++;
                if (a > 1) {
                    return false;
                }
                if (!(i > start && ch[i - 1] >= 'a' && ch[i - 1] <= 'z')
                        || !(i < end - 1 && ch[i + 1] >= 'a' && ch[i + 1] <= 'z')) {
                    return false;
                }
            }
            if (ch[i] == '!' || ch[i] == '.' || ch[i] == ',') {
                b++;
                if (b > 1 || i != end - 1) {
                    return false;
                }
            }
        }
        return true;
    }

    // 正则表达式
    public int countValidWords1(String sentence) {
        int count = 0;
        for (String s : sentence.split(" ")) {
            if (!s.isEmpty() && s.matches("[a-z]*([a-z]-[a-z])?[a-z]*[!.,]?")) {
                count++;
            }
        }
        return count;
    }


    /**
     * Medium 暴力
     * 如果整数  x 满足：对于每个数位 d ，这个数位 恰好 在 x 中出现 d 次。那么整数 x 就是一个 数值平衡数 。
     * 给你一个整数 n ，请你返回 严格大于 n 的 最小数值平衡数 。
     * 输入：n = 1
     * 输出：22
     * 解释：
     * 22 是一个数值平衡数，因为：
     * - 数字 2 出现 2 次
     * 这也是严格大于 1 的最小数值平衡数。
     */
    public int nextBeautifulNumber(int n) {
        // 1-1 2-22 3-333 4-4444 5-55555 6-666666 7-7777777
        // 122 212 221
        int res[] = {1, 22, 122, 212, 221, 333, 1333, 3133, 3313, 3331, 4444, 14444, 22333, 23233, 23323, 23332, 32233, 32323, 32332, 33223, 33232, 33322, 41444, 44144, 44414, 44441, 55555, 122333, 123233, 123323, 123332, 132233, 132323, 132332, 133223, 133232, 133322, 155555, 212333, 213233, 213323, 213332, 221333, 223133, 223313, 223331, 224444, 231233, 231323, 231332, 232133, 232313, 232331, 233123, 233132, 233213, 233231, 233312, 233321, 242444, 244244, 244424, 244442, 312233, 312323, 312332, 313223, 313232, 313322, 321233, 321323, 321332, 322133, 322313, 322331, 323123, 323132, 323213, 323231, 323312, 323321, 331223, 331232, 331322, 332123, 332132, 332213, 332231, 332312, 332321, 333122, 333212, 333221, 422444, 424244, 424424, 424442, 442244, 442424, 442442, 444224, 444242, 444422, 515555, 551555, 555155, 555515, 555551, 666666, 1224444};
        for (int x : res) {
            if (x > n) {
                return x;
            }
        }
        return -1;

    }

    public int nextBeautifulNumber1(int n) {
        int num = n + 1;
        while (!check(num)) {
            num++;
        }
        return num;
    }

    public boolean check(int num) {
        int[] nums = new int[10];
        while (num != 0) {
            nums[num % 10]++;
            num /= 10;
        }

        for (int x = 0; x <= 9; x++) {
            if (nums[x] != 0 && x != nums[x]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Medium dfs深度优先搜索
     * 给你一棵根节点为 0 的 二叉树 ，它总共有 n 个节点，节点编号为 0 到 n - 1 。
     * 同时给你一个下标从 0 开始的整数数组 parents 表示这棵树，其中 parents[i] 是节点 i 的父节点。由于节点 0 是根，所以 parents[0] == -1 。
     * 一个子树的 大小 为这个子树内节点的数目。每个节点都有一个与之关联的 分数 。
     * 求出某个节点分数的方法是，将这个节点和与它相连的边全部 删除 ，剩余部分是若干个 非空 子树，这个节点的 分数 为所有这些子树 大小的乘积 。
     * 请你返回有 最高得分 节点的 数目 。
     */
    public int countHighestScoreNodes(int[] parents) {
        int len = parents.length;
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            list.add(new ArrayList<>());
        }
        for (int i = 1; i < len; i++) {
            list.get(parents[i]).add(i);
        }
        TreeMap<Long, Integer> map = new TreeMap<>();
        dfs(0, new int[len], map, list);
        return map.lastEntry().getValue();
    }

    private void dfs(int index, int[] count, TreeMap<Long, Integer> map, List<List<Integer>> list) {
        long product = count[index] = 1;
        for (int i : list.get(index)) {
            dfs(i, count, map, list);
            count[index] += count[i];
            product *= count[i];
        }
        // 每个删除节点的分数=总节点数量减去删除节点数目
        product *= Math.max(1, count.length - count[index]);
        map.put(product, map.getOrDefault(product, 0) + 1);
    }

    /**
     * Hard bfs广度优先搜索
     * 给你一个整数 n ，表示有 n 节课，课程编号从 1 到 n 。同时给你一个二维整数数组 relations ，其中 relations[j] = [prevCoursej, nextCoursej] ，表示课程 prevCoursej 必须在课程 nextCoursej 之前 完成（先修课的关系）。同时给你一个下标从 0 开始的整数数组 time ，其中 time[i] 表示完成第 (i+1) 门课程需要花费的 月份 数。
     * 请你根据以下规则算出完成所有课程所需要的 最少 月份数：
     * 如果一门课的所有先修课都已经完成，你可以在 任意 时间开始这门课程。
     * 你可以 同时 上 任意门课程 。
     * 请你返回完成所有课程所需要的 最少 月份数。
     */
    public int minimumTime(int n, int[][] relations, int[] time) {
        List<List<Integer>> list = new ArrayList<>();
        int[] count = new int[n];
        int[] cost = new int[n];
        for (int i = 0; i < n; i++) {
            list.add(new ArrayList<>());
        }
        for (int[] course : relations) {
            // prevCourse 和 nextCourse
            list.get(course[0] - 1).add(course[1] - 1);
            // prevCourse完成了才能开始nextCourse
            count[course[1] - 1]++;
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            // 没有prevCourse，可以马上开始
            if (count[i] == 0) {
                queue.add(i);
            }
        }

        int res = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int cur = queue.poll();
                res = Math.max(res, cost[cur] += time[cur]);
                for (int x : list.get(cur)) {
                    // nextCourse的需要时间
                    cost[x] = Math.max(cost[x], cost[cur]);
                    if (--count[x] == 0) {
                        queue.add(x);
                    }
                }
            }
        }
        return res;
    }
}
