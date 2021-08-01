package com.leet.week;


/**
 * @Author: wenhongliang
 */
public class WeekOne {
    /**
     * 5830. 三除数
     * 给你一个整数 n 。如果 n 恰好有三个正除数 ，返回 true ；否则，返回 false 。
     * 如果存在整数 k ，满足 n = k * m ，那么整数 m 就是 n 的一个 除数
     */
    // 12 false 1 12 2 6 3 4
    // 14 true 1 14 2 7
    public boolean isThree(int n) {
        int count = 0;
        for (int i = 1; i <= n; i++) {
            if (n % i == 0) {
                count++;
                if (count > 3) {
                    break;
                }
            }
        }
        return count == 3;
    }

    /**
     * 输入：milestones = [1,2,3]
     * 输出：6
     * 解释：一种可能的情形是：
     * ​​​​- 第 1 周，你参与并完成项目 0 中的一个阶段任务。
     * - 第 2 周，你参与并完成项目 2 中的一个阶段任务。
     * - 第 3 周，你参与并完成项目 1 中的一个阶段任务。
     * - 第 4 周，你参与并完成项目 2 中的一个阶段任务。
     * - 第 5 周，你参与并完成项目 1 中的一个阶段任务。
     * - 第 6 周，你参与并完成项目 2 中的一个阶段任务。
     * 总周数是 6 。
     */
    public long numberOfWeeks0(int[] milestones) {
        int len = milestones.length;
        long max = milestones[0];
        long sum = milestones[0];
        for (int i = 1; i < len; i++) {
            max = Math.max(max, milestones[i]);
            sum += milestones[i];
        }
        // 选择最大与任务总和比较,max比sum-max大，必定有任务不能完成。否则，可以完成任务。
        return max > sum / 2 ? (sum - max) * 2 + 1 : sum;
    }

    /**
     * 输入：neededApples = 1
     * 输出：8
     * 解释：边长长度为 1 的正方形不包含任何苹果。
     * 但是边长为 2 的正方形包含 12 个苹果（如上图所示）。
     * 周长为 2 * 4 = 8 。
     */
    public long minimumPerimeter0(long neededApples) {
        long width = 2;
        long pre = 8;
        long all = 8;
        while (all < neededApples) {
            width += 2;
            pre += 4 * width + (2 * width - 1) * 4;
            all += pre;
        }
        return width * 4;
    }

    public long minimumPerimeter1(long neededApples) {
        long sum = 0;
        for (int i = 1; i <= neededApples; i++) {
            for (int j = 1; j < i; j++) {
                sum += 8 * (i + j);
            }
            sum += 4 * i + 4 * (i + i);
            if (sum >= neededApples) {
                return 2 * i * 4;
            }
        }
        return 0;
    }

    /**
     * 输入：nums = [0,1,2,2]
     * 输出：3
     * 解释：特殊子序列为 [0,1,2,2]，[0,1,2,2] 和 [0,1,2,2] 。
     */
    public int countSpecialSubsequences(int[] nums) {
        long[] dp = new long[3];
        int mod = 1000000007;
        for (int i = 0; i < nums.length; i++) {
            int n = nums[i];
            if (n == 0) {
                dp[0] = dp[0] * 2 + 1;
                dp[0] %= mod;
            } else if (n == 1) {
                dp[1] = dp[0] + dp[1] * 2;
                dp[1] %= mod;
            } else {
                dp[2] = dp[1] + dp[2] * 2;
                dp[2] %= mod;
            }
        }
        return (int) dp[2];
    }
}
