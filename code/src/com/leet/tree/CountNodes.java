package com.leet.tree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author: wenhongliang
 */
// 222. 完全二叉树的节点个数
public class CountNodes {
    public int countNodesMethod1(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int count = 0;
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            TreeNode tmp = queue.poll();
            if (tmp.left != null) {
                queue.offer(tmp.left);
            }
            if (tmp.right != null) {
                queue.offer(tmp.right);
            }
            count++;
        }
        return count;
    }

    public int countNodesMethod2(TreeNode root) {
        if (root == null) {
            return 0;
        }
        return countNodesMethod2(root.left) + countNodesMethod2(root.right) + 1;
    }

    public int countNodesMethod3(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = contLevel(root.left);
        int right = contLevel(root.right);
        if (left == right) { // 相等则左子树则是满二叉树
            return countNodesMethod3(root.right) + (1 << left);
        } else { // 否则右子树则是满二叉树
            return countNodesMethod3(root.left) + (1 << left);
        }
    }

    private int contLevel(TreeNode node) {
        int level = 0;
        while (node != null) {
            level++;
            node = node.left;
        }
        return level;
    }
}
