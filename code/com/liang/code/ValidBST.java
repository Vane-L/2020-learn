package com.liang.code;

/**
 * @Author: wenhongliang
 */
// 98. 验证二叉搜索树
public class ValidBST {
    // 不仅仅是当前root节点和左右子节点比较，还要比较左右子树的孩子节点
    public boolean isValidBST(TreeNode root) {
        if (root == null) {
            return true;
        }
        TreeNode leftMax = root.left;
        TreeNode rightMin = root.right;
        while (leftMax != null && leftMax.right != null) {
            leftMax = leftMax.right;
        }
        while (rightMin != null && rightMin.left != null) {
            rightMin = rightMin.left;
        }
        // 当前层是否合法
        boolean ret = (leftMax == null || leftMax.val < root.val) &&
                (rightMin == null || root.val < rightMin.val);
        // 进入左子树和右子树并判断是否合法
        return ret && isValidBST(root.left) && isValidBST(root.right);
    }
}
