package com.leet.tree;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

/**
 * @Author: wenhongliang
 */
public class MediumTree {

    List<Integer> res = new ArrayList<>();

    public List<Integer> preorderTraversal(TreeNode root) {
        if (root == null) {
            return res;
        }
        res.add(root.val);
        preorderTraversal(root.left);
        preorderTraversal(root.right);
        return res;
    }

    public List<Integer> preorderTraversal2(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        Stack<TreeNode> stack = new Stack<>();
        TreeNode node = root;
        while (!stack.isEmpty() || node != null) {
            while (node != null) {
                res.add(node.val);
                stack.push(node);
                node = node.left;
            }
            node = stack.pop();
            node = node.right;
        }
        return res;
    }

    public List<Integer> postorderTraversal(TreeNode root) {
        if (root != null) {
            postorderTraversal(root.left);
            postorderTraversal(root.right);
            res.add(root.val);
        }
        return res;
    }

    public List<Integer> postorderTraversal2(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        Stack<TreeNode> stack = new Stack<>();
        TreeNode pre = null;
        while (root != null || !stack.isEmpty()) {
            // add left
            while (root != null) {
                stack.push(root);
                root = root.left;
            }
            root = stack.pop();
            if (root.right == null || root.right == pre) {
                res.add(root.val);
                pre = root;
                root = null;
            } else {
                stack.push(root);
                root = root.right;
            }
        }
        return res;
    }

    /**
     * 叶节点 是二叉树中没有子节点的节点
     * 树的根节点的 深度 为 0，如果某一节点的深度为 d，那它的子节点的深度就是 d+1
     * 如果我们假定 A 是一组节点 S 的 最近公共祖先，S 中的每个节点都在以 A 为根节点的子树中，且 A 的深度达到此条件下可能的最大值。
     */


    int maxDepth;
    TreeNode ancestor;

    public TreeNode lcaDeepestLeaves1123(TreeNode root) {
        if (root == null) {
            return null;
        }
        dfs(root, 0);
        return ancestor;
    }

    private int dfs(TreeNode root, int depth) {
        if (root == null) {
            return depth;
        }
        depth++;
        int left = dfs(root.left, depth);
        int right = dfs(root.right, depth);
        depth = Math.max(left, right);
        if (left == right && depth > maxDepth) {
            maxDepth = depth;
            ancestor = root;
        }
        return depth;
    }

    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        boolean flag = false;
        Deque<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            List<Integer> list = new ArrayList<>();
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                if (!flag) {
                    TreeNode tmp = queue.pollFirst();
                    list.add(tmp.val);
                    if (tmp.right != null) {
                        queue.addLast(tmp.right);
                    }
                    if (tmp.left != null) {
                        queue.addLast(tmp.left);
                    }
                } else {
                    TreeNode tmp = queue.pollLast();
                    list.add(tmp.val);
                    if (tmp.left != null) {
                        queue.addFirst(tmp.left);
                    }
                    if (tmp.right != null) {
                        queue.addFirst(tmp.right);
                    }
                }
            }
            flag = !flag;
            res.add(list);
        }
        return res;
    }

    Map<Integer, Integer> map = new HashMap<>();

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        for (int i = 0; i < inorder.length; i++) {
            map.put(inorder[i], i);
        }
        return dfs(preorder, 0, preorder.length - 1, inorder, 0, inorder.length - 1);
    }

    public TreeNode dfs(int[] preorder, int ps, int pe, int[] inorder, int is, int ie) {
        if (ps > pe || is > ie) {
            return null;
        }
        TreeNode root = new TreeNode(preorder[ps]);
        int idx = map.get(preorder[ps]);
        root.left = dfs(preorder, ps + 1, ps + idx - is, inorder, is, idx - 1);
        root.right = dfs(preorder, ps + idx - is + 1, pe, inorder, idx + 1, ie);
        return root;
    }
}
