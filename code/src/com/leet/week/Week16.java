package com.leet.week;

import com.leet.list.ListNode;
import com.leet.tree.TreeNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * @Author: wenhongliang
 */
public class Week16 {
    /**
     * Easy 5942. 找出 3 位偶数
     * 给你一个整数数组 digits ，其中每个元素是一个数字（0 - 9）。数组中可能存在重复元素。
     * 你需要找出 所有 满足下述条件且 互不相同 的整数：
     * 该整数由 digits 中的三个元素按 任意 顺序 依次连接 组成。
     * 该整数不含 前导零
     * 该整数是一个 偶数
     * 例如，给定的 digits 是 [1, 2, 3] ，整数 132 和 312 满足上面列出的全部条件。
     * 将找出的所有互不相同的整数按 递增顺序 排列，并以数组形式返回。
     * 我想的是回溯，组成所有的三位数，然后找偶数哈哈哈，然后超出时间限制了...
     * 没想到别人的答案，是三层循环啊
     **/
    public int[] findEvenNumbers(int[] digits) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < digits.length; i++) {
            for (int j = 0; j < digits.length; j++) {
                for (int k = 0; k < digits.length; k++) {
                    if (i == j || j == k || i == k) continue;
                    if (digits[i] != 0 && digits[k] % 2 == 0) {
                        set.add(digits[i] * 100 + digits[j] * 10 + digits[k]);
                    }
                }
            }
        }
        int[] res = new int[set.size()];
        int idx = 0;
        for (int x : set) {
            res[idx++] = x;
        }
        Arrays.sort(res);
        return res;
    }

    public int[] findEvenNumbersMy(int[] digits) {
        int len = digits.length;
        boolean[] visited = new boolean[len];
        Set<Integer> set = new TreeSet<>((a, b) -> (a - b));
        backMy(digits, set, visited, 0);
        int[] res = new int[set.size()];
        int idx = 0;
        for (Integer x : set) {
            res[idx++] = x;
        }
        return res;
    }

    public void backMy(int[] digits, Set<Integer> set, boolean[] visited, int cur) {
        if (cur > 999) {
            return;
        }
        if (cur >= 100 && cur % 2 == 0) {
            set.add(cur);
            return;
        }
        for (int i = 0; i < digits.length; i++) {
            if (cur >= 10 && cur <= 99 && digits[i] % 2 == 1) continue;
            if (!visited[i]) {
                visited[i] = true;
                backMy(digits, set, visited, cur * 10 + digits[i]);
                visited[i] = false;
            }
        }
    }


    /**
     * Medium 5943. 删除链表的中间节点
     * 给你一个链表的头节点 head 。删除 链表的 中间节点 ，并返回修改后的链表的头节点 head 。
     * 长度为 n 链表的中间节点是从头数起第 ⌊n / 2⌋ 个节点（下标从 0 开始），其中 ⌊x⌋ 表示小于或等于 x 的最大整数。
     * 最开始想了个快慢指针，一直解答错误，因为需要知道总长度是奇数还是偶数！！！
     * 原来快慢指针是work的，只需要记录前一个节点即可。。。
     */
    public ListNode deleteMiddle(ListNode head) {
        if (head == null || head.next == null) return null;
        ListNode p = head;
        int len = 0;
        while (p != null) {
            len++;
            p = p.next;
        }
        int del = len / 2;
        p = head;
        while (--del > 0) {
            p = p.next;
        }
        if (p.next != null) {
            p.next = p.next.next;
        }
        return head;
    }

    public ListNode deleteMiddleSlowFast(ListNode head) {
        if (head == null || head.next == null) return null;
        ListNode slow = head, fast = head, pre = head;
        while (fast != null && fast.next != null) {
            // 记录前一个节点
            pre = slow;
            slow = slow.next;
            fast = fast.next.next;
        }
        pre.next = slow.next;
        return head;
    }


    /**
     * Medium 5944. 从二叉树一个节点到另一个节点每一步的方向
     */
    StringBuilder res = new StringBuilder();

    public String getDirections(TreeNode root, int startValue, int destValue) {
        StringBuilder sb = new StringBuilder();
        TreeNode newRoot = dfs(root, startValue, destValue);
        //添加start到root的路径
        int num = findSrc(newRoot, startValue);
        for (int i = 0; i < num; i++) {
            res.append('U');
        }
        //添加root到dest的路径
        findDest(newRoot, destValue, sb);
        return res.toString();
    }

    //寻找最近的公共祖先newRoot
    public TreeNode dfs(TreeNode root, int startValue, int destValue) {
        if (root == null || root.val == startValue || root.val == destValue) {
            return root;
        }
        TreeNode l = dfs(root.left, startValue, destValue);
        TreeNode r = dfs(root.right, startValue, destValue);
        if (l == null) return r;
        if (r == null) return l;
        return root;
    }

    //寻找newRoot到dest的路径
    public void findDest(TreeNode root, int destValue, StringBuilder sb) {
        if (root.val == destValue) {
            res.append(sb.toString());
            return;
        }
        if (root.right != null) {
            sb.append('R');
            findDest(root.right, destValue, sb);
            sb.deleteCharAt(sb.length() - 1);
        }
        if (root.left != null) {
            sb.append('L');
            findDest(root.left, destValue, sb);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    //寻找start到newRoot有几个U
    public int findSrc(TreeNode root, int srcValue) {
        if (root == null) {
            return 10000;
        }
        if (root.val == srcValue) {
            return 0;
        }
        return Math.min(findSrc(root.left, srcValue), findSrc(root.right, srcValue)) + 1;
    }

    /**
     * Hard 5932. 合法重新排列数对
     */

    Map<Integer, int[]> countMap = new HashMap<>();
    Map<Integer, Queue<Integer>> map = new HashMap<>();
    List<Integer> path = new ArrayList<>();

    public int[][] validArrangement(int[][] pairs) {
        int length = pairs.length;
        int[][] arrangement = new int[length][2];
        for (int[] pair : pairs) {
            int start = pair[0], end = pair[1];
            countMap.putIfAbsent(start, new int[2]);
            countMap.putIfAbsent(end, new int[2]);
            countMap.get(start)[0]++;
            countMap.get(end)[1]++;
            if (!map.containsKey(start))
                map.put(start, new ArrayDeque<>());
            map.get(start).offer(end);
        }
        int source = pairs[0][0];
        Set<Integer> set = countMap.keySet();
        for (int num : set) {
            int[] count = countMap.get(num);
            if (count[0] > count[1]) {
                source = num;
                break;
            }
        }
        depthFirstSearch(source);
        Collections.reverse(path);
        for (int i = 0; i < length; i++) {
            int start = path.get(i), end = path.get(i + 1);
            arrangement[i][0] = start;
            arrangement[i][1] = end;
        }
        return arrangement;
    }

    public void depthFirstSearch(int num) {
        while (map.containsKey(num) && !map.get(num).isEmpty()) {
            int tmp = map.get(num).poll();
            depthFirstSearch(tmp);
        }
        path.add(num);
    }
}
