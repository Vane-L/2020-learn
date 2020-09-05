package com.liang.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class BinaryTreePaths {

    public List<String> binaryTreePaths(TreeNode root) {
        List<String> res = new ArrayList<>();
        //dfs(res, root, "");
        dfs(root, "", res);
        return res;
    }

    // first
    private void dfs(List<String> res, TreeNode root, String s) {
        if (root == null) {
            return;
        }
        if (root.left == null && root.right == null) {
            res.add(s + root.val);
            return;
        }
        if (root.left != null) {
            dfs(res, root.left, s + root.val + "->");
        }
        if (root.right != null) {
            dfs(res, root.right, s + root.val + "->");
        }
    }

    // second
    public void dfs(TreeNode root, String path, List<String> res) {
        if (root != null) {
            path += root.val;
            if (root.left == null && root.right == null) {
                res.add(path);
            } else {
                path += "->";
                dfs(root.left, path, res);
                dfs(root.right, path, res);
            }
        }
    }
}
