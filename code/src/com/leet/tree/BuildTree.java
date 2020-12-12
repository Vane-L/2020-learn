package com.leet.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class BuildTree {
    public static void main(String[] args) {
        BuildTree bt = new BuildTree();
        bt.buildTree(new int[]{9, 3, 15, 20, 7}, new int[]{9, 15, 7, 20, 3});
        bt.sumOddLengthSubarrays(new int[]{1, 4, 2, 5, 3});
    }

    // 中序遍历 inorder   = [9,3,15,20,7]
    // 后序遍历 postorder = [9,15,7,20,3]
    public TreeNode buildTree(int[] inorder, int[] postorder) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            map.put(inorder[i], i);
        }
        return buildTree(map, postorder, 0, inorder.length - 1, 0, postorder.length - 1);
    }

    private TreeNode buildTree(Map<Integer, Integer> map, int[] postorder, int is, int ie, int ps, int pe) {
        if (is > ie || ps > pe) {
            return null;
        }
        TreeNode root = new TreeNode(postorder[pe]);
        int index = map.get(postorder[pe]);
        root.left = buildTree(map, postorder, is, index - 1, ps, ps + index - 1 - is);
        root.right = buildTree(map, postorder, index + 1, ie, ps + index - is, pe - 1);
        return root;
    }

    public int sumOddLengthSubarrays(int[] arr) {
        int len = arr.length, res = 0;
        for (int i = 0; i < len; i++) {
            int LeftOdd = (i + 1) / 2, LeftEven = i / 2 + 1;
            int RightOdd = (len - i) / 2, RightEven = (len - 1 - i) / 2 + 1;
            res += arr[i] * (LeftOdd * RightOdd + LeftEven * RightEven);
        }
        return res;
    }

}
