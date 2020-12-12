package com.leet.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
// 337 打家劫舍 III
public class Rob {
    Map<TreeNode, Integer> f = new HashMap<>();
    Map<TreeNode, Integer> g = new HashMap<>();

    public int rob(TreeNode root) {
        dfs(root);
        return Math.max(f.getOrDefault(root, 0), g.getOrDefault(root, 0));
    }

    public void dfs(TreeNode node) {
        if (node == null) {
            return;
        }
        dfs(node.left);
        dfs(node.right);
        f.put(node, node.val + g.getOrDefault(node.left, 0) + g.getOrDefault(node.right, 0));
        g.put(node, Math.max(f.getOrDefault(node.left, 0), g.getOrDefault(node.left, 0)) + Math.max(f.getOrDefault(node.right, 0), g.getOrDefault(node.right, 0)));
    }

    public int robDP(TreeNode root) {
        int[] rootStatus = dfsDP(root);
        return Math.max(rootStatus[0], rootStatus[1]);
    }

    public int[] dfsDP(TreeNode node) {
        if (node == null) {
            return new int[]{0, 0};
        }
        int[] l = dfsDP(node.left);
        int[] r = dfsDP(node.right);
        int selected = node.val + l[1] + r[1];
        int notSelected = Math.max(l[0], l[1]) + Math.max(r[0], r[1]);
        return new int[]{selected, notSelected};
    }
}
