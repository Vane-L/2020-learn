package com.leet.tree;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    public boolean isSubtree(TreeNode s, TreeNode t) {
        String strS = serialize(s);
        String strT = serialize(t);

        return strS.contains(strT);
    }

    public String serialize(TreeNode s) {
        StringBuilder sb = new StringBuilder();
        Deque<TreeNode> stack = new LinkedList<>();
        stack.push(s);
        while (!stack.isEmpty()) {
            TreeNode tmp = stack.pop();
            sb.append(",").append(tmp == null ? "#" : tmp.val);
            if (tmp != null) {
                stack.push(tmp.left);
                stack.push(tmp.right);
            }
        }
        return sb.toString();
    }

    public boolean isSubtreeDFS(TreeNode s, TreeNode t) {
        if (s == null) return false;
        if (isSame(s, t)) return true;
        return isSubtreeDFS(s.left, t) || isSubtreeDFS(s.right, t);
    }

    private boolean isSame(TreeNode s, TreeNode t) {
        if (s == null && t == null) return true;
        if (s == null || t == null) return false;
        if (s.val != t.val) return false;
        return isSame(s.left, t.left) && isSame(s.right, t.right);
    }


    public int minDepth1(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = minDepth1(root.left);
        int right = minDepth1(root.right);
        if (root.left == null || root.right == null) {
            return left + right + 1;
        }
        return Math.min(minDepth1(root.left), minDepth1(root.right)) + 1;
    }

    public int minDepth2(TreeNode root) {
        if (root == null) {
            return 0;
        }
        if (root.left == null) {
            return minDepth1(root.right) + 1;
        }
        if (root.right == null) {
            return minDepth1(root.left) + 1;
        }
        return Math.min(minDepth1(root.left), minDepth1(root.right)) + 1;
    }

    public int minDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        getDepth(root, 1);
        return min;
    }

    int min = Integer.MAX_VALUE;

    private void getDepth(TreeNode root, int depth) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {
            min = Math.min(min, depth);
            return;
        }
        getDepth(root.left, depth + 1);
        getDepth(root.right, depth + 1);
    }


    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);
        root.left = new TreeNode(2);
        root.left.right = new TreeNode(6);
        //root.left.right.left = new TreeNode(7);
        //root.left.right.right.left = new TreeNode(8);
        root.right = new TreeNode(3);
        root.right.right = new TreeNode(4);
        root.right.right.right = new TreeNode(5);
        root.right.right.right.right = new TreeNode(9);
        System.out.println(new MediumTree().minDepth(root));
        System.out.println(new MediumTree().minDepth1(root));
        System.out.println(new MediumTree().minDepth2(root));
    }
}
