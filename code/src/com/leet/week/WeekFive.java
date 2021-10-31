package com.leet.week;


import java.util.Arrays;

/**
 * @Author: wenhongliang
 */
public class WeekFive {
    /**
     * 学生分数的最小差值
     * 给你一个 下标从 0 开始 的整数数组 nums ，其中 nums[i] 表示第 i 名学生的分数。另给你一个整数 k 。
     * 从数组中选出任意 k 名学生的分数，使这 k 个分数间 最高分 和 最低分 的 差值 达到 最小化 。
     * 返回可能的 最小差值 。
     */
    public int minimumDifference(int[] nums, int k) {
        if (k == 1 || nums.length == 1) {
            return 0;
        }
        Arrays.sort(nums);
        int res = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int i = k - 1; i < nums.length; i++) {
            res = Math.min(res, nums[i] - nums[i - k + 1]);
        }
        return res;
    }

    /**
     * 找出数组中的第 K 大整数
     * 输入：nums = ["3","6","7","10"], k = 4
     * 输出："3"
     * 解释：nums 中的数字按非递减顺序排列为 ["3","6","7","10"]，其中第 4 大整数是 "3"
     */
    public String kthLargestNumber(String[] nums, int k) {
        Arrays.sort(nums, (o1, o2) -> o1.length() == o2.length() ? o1.compareTo(o2) : o1.length() - o2.length());
        return nums[nums.length - k];
    }


    /**
     * 完成任务的最少工作时间段
     * 输入：tasks = [1,2,3], sessionTime = 3
     * 输出：2
     * 解释：你可以在两个工作时间段内完成所有任务。
     * - 第一个工作时间段：完成第一和第二个任务，花费 1 + 2 = 3 小时。
     * - 第二个工作时间段：完成第三个任务，花费 3 小时。
     */
    public int minSessions(int[] tasks, int sessionTime) {
        int len = tasks.length;
        int[][] cost = new int[(1 << len)][sessionTime + 1];
        for (int i = 0; i < (1 << len); i++) {
            Arrays.fill(cost[i], Integer.MAX_VALUE);
        }
        return dfs(tasks, cost, 1, tasks[0], sessionTime);
    }

    private int dfs(int[] tasks, int[][] cost, int n, int curr, int sessionTime) {
        if (cost[n][curr] != Integer.MAX_VALUE) return cost[n][curr];
        if (n == (1 << tasks.length) - 1) {
            return 1;
        }
        int res = Integer.MAX_VALUE;
        for (int i = 0; i < tasks.length; i++) {
            if (((n >> i) & 1) == 0) {
                n ^= 1 << i;
                if (curr + tasks[i] <= sessionTime) {
                    res = Math.min(res, dfs(tasks, cost, n, curr + tasks[i], sessionTime));
                } else {
                    res = Math.min(res, dfs(tasks, cost, n, tasks[i], sessionTime) + 1);
                }
                n ^= 1 << i;
            }
        }
        cost[n][curr] = Math.min(cost[n][curr], res);
        return cost[n][curr];
    }

    /**
     * 不同的好子序列数目
     * 从后向前遍历字符串 binary:
     * 当 binary[i] == '0' 时，dp[i][0] 的求解可以分成 3 个部分
     * - 这个 '0' 可以添加到所有的子序列的前面
     * - 原有的dp[i+1][1] 个以0 开始的子序列，但是这部分不能增加到dp[i][0] 中
     * - 单独的 1 个0
     * dp[i][0] = 第一部分+ 第三部分 = dp[i+1][0] + dp[i+1][1] + 1; dp[i][1]=dp[i+1][1]
     * 当 binary[i] == '1' 时，dp[i][0] = dp[i+1][0]; dp[i][1]=dp[i+1][0] + dp[i+1][1] + 1
     * 最后答案，全部以 1 开头的子序列的个数 + 字符串0的个数
     */
    public int numberOfUniqueGoodSubsequences(String binary) {
        int mod = 1000000007;
        int dp0 = 0, dp1 = 0, has0 = 0;
        for (int i = binary.length() - 1; i >= 0; --i) {
            if (binary.charAt(i) == '0') {
                has0 = 1;
                dp0 = (dp0 + dp1 + 1) % mod;
            } else if (binary.charAt(i) == '1') {
                dp1 = (dp0 + dp1 + 1) % mod;
            }
        }
        return (dp1 + has0) % mod;

    }
}
