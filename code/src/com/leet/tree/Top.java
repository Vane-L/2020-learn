package com.leet.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @Author: wenhongliang
 */
public class Top {
    int ans = 0, count = 0;

    List<Integer> list = new ArrayList<>();

    public int kthSmallest(TreeNode root, int k) {
        dfs(root, k);
        return ans;
    }

    private void dfs(TreeNode root, int k) {
        if (root == null) {
            return;
        }
        dfs(root.left, k);
        if (++count == k) {
            ans = root.val;
            return;
        }
        dfs(root.right, k);
    }

    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) {
            return root;
        }
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return root;
    }


    public class Codec {

        // Encodes a tree to a single string.
        public String serializeDFS(TreeNode root) {
            if (root == null) {
                return "null";
            }
            return root.val + "," + serializeDFS(root.left) + "," + serializeDFS(root.right);
        }

        // Decodes your encoded data to tree.
        public TreeNode deserializeDFS(String data) {
            Queue<String> queue = new LinkedList<>(Arrays.asList(data.split(",")));
            return dfs(queue);
        }

        private TreeNode dfs(Queue<String> queue) {
            String val = queue.poll();
            if ("null".equals(val)) {
                return null;
            }
            TreeNode root = new TreeNode(Integer.parseInt(val));
            root.left = dfs(queue);
            root.right = dfs(queue);
            return root;
        }

        // Encodes a tree to a single string.
        public String serializeBFS(TreeNode root) {
            StringBuilder sb = new StringBuilder();
            Queue<TreeNode> queue = new LinkedList<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                TreeNode cur = queue.poll();
                if (cur == null) {
                    sb.append("null,");
                } else {
                    sb.append(cur.val + ",");
                    queue.add(cur.left);
                    queue.add(cur.right);
                }
            }
            sb.substring(0, sb.length() - 1);
            return sb.toString();
        }

        // Decodes your encoded data to tree.
        public TreeNode deserializeBFS(String data) {
            if (data.equals("")) {
                return null;
            }
            String[] nodes = data.split(",");
            TreeNode root = new TreeNode(Integer.parseInt(nodes[0]));
            Queue<TreeNode> queue = new LinkedList<>();
            TreeNode parent = root;
            int idx = 1;
            queue.offer(root);
            while (!queue.isEmpty()) {
                TreeNode cur = queue.poll();
                if (!"null".equals(nodes[idx])) {
                    cur.left = new TreeNode(Integer.parseInt(nodes[idx]));
                    queue.offer(cur.left);
                }
                idx++;
                if (!"null".equals(nodes[idx])) {
                    cur.right = new TreeNode(Integer.parseInt(nodes[idx]));
                    queue.offer(cur.right);
                }
                idx++;
            }
            return root;
        }
    }

    public List<List<Integer>> getSkyline(int[][] buildings) {
        List<List<Integer>> points = new ArrayList<>();
        List<List<Integer>> results = new ArrayList<>();
        for (int[] b : buildings) {
            List<Integer> p1 = new ArrayList<>();
            p1.add(b[0]);
            p1.add(-b[2]);
            points.add(p1);

            List<Integer> p2 = new ArrayList<>();
            p2.add(b[1]);
            p2.add(b[2]);
            points.add(p2);
        }
        points.sort((p1, p2) -> {
            int x1 = p1.get(0);
            int y1 = p1.get(1);
            int x2 = p2.get(0);
            int y2 = p2.get(1);
            if (x1 != x2) {
                return x1 - x2;
            } else {
                return y1 - y2;
            }
        });

        Queue<Integer> queue = new PriorityQueue<>((a, b) -> (b - a));
        queue.offer(0);
        int preMax = 0;
        for (List<Integer> p : points) {
            int x = p.get(0);
            int y = p.get(1);
            if (y < 0) {
                //左上角坐标
                queue.offer(-y);
            } else {
                //右上角坐标
                queue.remove(y);
            }
            int curMax = queue.peek();
            //最大值更新了, 将当前结果加入
            if (curMax != preMax) {
                List<Integer> temp = new ArrayList<>();
                temp.add(x);
                temp.add(curMax);
                results.add(temp);
                preMax = curMax;
            }
        }
        return results;
    }

    public List<List<Integer>> getSkyline2(int[][] buildings) {
        if (buildings.length == 0) {
            return new ArrayList<>();
        }
        return merge2(buildings, 0, buildings.length - 1);
    }

    private List<List<Integer>> merge2(int[][] buildings, int start, int end) {
        List<List<Integer>> res = new ArrayList<>();
        //只有一个建筑, 将 [x, h], [y, 0] 加入结果
        if (start == end) {
            res.add(Arrays.asList(buildings[start][0], buildings[start][2]));
            res.add(Arrays.asList(buildings[start][1], 0));
            return res;
        }
        int mid = (start + end) >> 1;
        //第一组解
        List<List<Integer>> skyline1 = merge2(buildings, start, mid);
        //第二组解
        List<List<Integer>> skyline2 = merge2(buildings, mid + 1, end);
        //下边将两组解合并
        int h1 = 0;
        int h2 = 0;
        int i = 0;
        int j = 0;
        while (i < skyline1.size() || j < skyline2.size()) {
            long x1 = i < skyline1.size() ? skyline1.get(i).get(0) : Long.MAX_VALUE;
            long x2 = j < skyline2.size() ? skyline2.get(j).get(0) : Long.MAX_VALUE;
            long x = 0;
            //比较两个坐标
            if (x1 < x2) {
                h1 = skyline1.get(i).get(1);
                x = x1;
                i++;
            } else if (x1 > x2) {
                h2 = skyline2.get(j).get(1);
                x = x2;
                j++;
            } else {
                h1 = skyline1.get(i).get(1);
                h2 = skyline2.get(j).get(1);
                x = x1;
                i++;
                j++;
            }
            //更新 height
            int height = Math.max(h1, h2);
            //重复的解不要加入
            if (res.isEmpty() || height != res.get(res.size() - 1).get(1)) {
                res.add(Arrays.asList((int) x, height));
            }
        }
        return res;
    }

    public List<TreeNode> generateTrees(int n) {
        if (n == 0) {
            return new ArrayList<>();
        }
        return build(1, n);
    }

    private List<TreeNode> build(int start, int end) {
        List<TreeNode> list = new ArrayList<>();
        if (start > end) {
            list.add(null);
            return list;
        }
        for (int i = start; i <= end; i++) {
            List<TreeNode> left = build(start, i - 1);
            List<TreeNode> right = build(i + 1, end);
            for (TreeNode x : left) {
                for (TreeNode y : right) {
                    TreeNode node = new TreeNode(i);
                    node.left = x;
                    node.right = y;
                    list.add(node);
                }
            }
        }
        return list;
    }

    public int numTrees(int n) {
        int[] dp = new int[n + 1];
        dp[0] = 1;
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= i; j++)
                dp[i] += dp[j - 1] * dp[i - j];
        return dp[n];
    }

}
