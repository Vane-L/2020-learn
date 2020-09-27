package com.liang.tree;

/**
 * @Author: wenhongliang
 */
public class MidTree {
    public static void main(String[] args) {
        MidTree midTree = new MidTree();

    }

    // 「好节点」X 定义为：从根到该节点 X 所经过的节点中，没有任何节点的值大于 X 的值。
    int res = 0;

    public int goodNodes(TreeNode root) {
        dfs(root, Integer.MIN_VALUE);
        return res;
    }

    private void dfs(TreeNode root, int max) {
        if (root == null) {
            return;
        }
        if (root.val >= max) {
            res++;
            max = root.val;
        }
        dfs(root.left, max);
        dfs(root.right, max);
    }
}
