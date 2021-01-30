package com.leet.tree;

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

    // 给定二叉搜索树（BST）的根节点和要插入树中的值，将值插入二叉搜索树。
    // 返回插入后二叉搜索树的根节点。 输入数据保证，新值和原始二叉搜索树中的任意节点值都不同。
    public TreeNode insertIntoBST(TreeNode root, int val) {
        if (root == null) {
            return new TreeNode(val);
        }
        TreeNode pos = root;
        while (pos != null) {
            if (val < pos.val) {
                if (pos.left == null) {
                    pos.left = new TreeNode(val);
                    break;
                } else {
                    pos = pos.left;
                }
            } else {
                if (pos.right == null) {
                    pos.right = new TreeNode(val);
                    break;
                } else {
                    pos = pos.right;
                }
            }
        }
        return root;
    }

    int maxDepth;
    TreeNode ancestor;

    // 获取所有深度最大节点的子树
    public TreeNode subtreeWithAllDeepest(TreeNode root) {
        if (root == null) {
            return null;
        }
        subtree(root, 0);
        return ancestor;
    }

    private int subtree(TreeNode root, int depth) {
        if (root == null) {
            return depth;
        }
        depth++;
        int left = subtree(root.left, depth);
        int right = subtree(root.right, depth);
        depth = Math.max(left, right);
        if (left == right && depth >= maxDepth) {
            maxDepth = depth;
            ancestor = root;
        }
        return depth;
    }
}
