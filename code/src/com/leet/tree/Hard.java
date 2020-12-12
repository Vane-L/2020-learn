package com.leet.tree;

/**
 * @Author: wenhongliang
 */
public class Hard {

    int maxSum = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        getMaxSum(root);
        return maxSum;
    }

    public int getMaxSum(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = Math.max(0, getMaxSum(root.left));
        int right = Math.max(0, getMaxSum(root.right));
        maxSum = Math.max(maxSum, root.val + left + right);
        return root.val + Math.max(left, right);
    }
}
