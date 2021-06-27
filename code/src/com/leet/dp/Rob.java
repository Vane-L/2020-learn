package com.leet.dp;

import com.leet.tree.TreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class Rob {
    public int rob1(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        if (nums.length == 1) {
            return nums[0];
        }
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        dp[1] = Math.max(nums[0], nums[1]);
        for (int i = 2; i < nums.length; i++) {
            dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i]);
        }
        return dp[nums.length - 1];
    }

    public int rob2(int[] nums) {
        int len = nums.length;
        if (len == 1) {
            return nums[0];
        } else if (len == 2) {
            return Math.max(nums[0], nums[1]);
        } else {
            return Math.max(robRange(nums, 0, len - 2), robRange(nums, 1, len - 1));
        }
    }

    public int robRange(int[] nums, int start, int end) {
        int dp_i = 0, dp_i_1 = 0, dp_i_2 = 0;
        for (int i = end; i >= start; i--) {
            dp_i = Math.max(dp_i_1, nums[i] + dp_i_2);
            dp_i_2 = dp_i_1;
            dp_i_1 = dp_i;
        }
        return dp_i;
    }

    public int rob3(TreeNode root) {
        int[] res = dp(root);
        return Math.max(res[0], res[1]);
    }

    // arr[0] 表示不抢 root 的话，得到的最大钱数
    // arr[1] 表示抢 root 的话，得到的最大钱数
    public int[] dp(TreeNode root) {
        if (root == null)
            return new int[]{0, 0};
        int[] left = dp(root.left);
        int[] right = dp(root.right);
        int rob = root.val + left[0] + right[0];
        int notRob = Math.max(left[0], left[1]) + Math.max(right[0], right[1]);
        return new int[]{notRob, rob};
    }
}
