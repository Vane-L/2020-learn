package com.liang.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class PathSum {
    public static void main(String[] args) {
        PathSum sum = new PathSum();
    }

    public List<List<Integer>> pathSum(TreeNode root, int sum) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        dfs(res, root, sum, 0, new ArrayList<>());
        return res;
    }

    private void dfs(List<List<Integer>> res, TreeNode root, int sum, int cur, ArrayList<Integer> tmp) {
        cur += root.val;
        tmp.add(root.val);
        if (root.left == null && root.right == null) {
            if (cur == sum) {
                res.add(new ArrayList<>(tmp));
            }
            tmp.remove(tmp.size() - 1);
            return;
        }
        if (root.left != null) {
            dfs(res, root.left, sum, cur, tmp);
        }
        if (root.right != null) {
            dfs(res, root.right, sum, cur, tmp);
        }
        tmp.remove(tmp.size() - 1);
    }

}
