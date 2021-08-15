package com.leet.week;

import java.util.Arrays;

/**
 * @Author: wenhongliang
 */
public class WeekThree {
    /**
     * 输入：patterns = ["a","abc","bc","d"], word = "abc"
     * 输出：3
     * 解释：
     * - "a" 是 "abc" 的子字符串。
     * - "abc" 是 "abc" 的子字符串。
     * - "bc" 是 "abc" 的子字符串。
     * - "d" 不是 "abc" 的子字符串。
     * patterns 中有 3 个字符串作为子字符串出现在 word 中。
     */
    public int numOfStrings(String[] patterns, String word) {
        int res = 0;
        for (String pattern : patterns) {
            if (word.contains(pattern)) {
                res++;
            }
        }
        return res;
    }

    /**
     * 给你一个 下标从 0 开始 的数组 nums ，数组由若干 互不相同的 整数组成。你打算重新排列数组中的元素以满足：
     * 重排后，数组中的每个元素都 不等于 其两侧相邻元素的 平均值 。
     * 更公式化的说法是，重新排列的数组应当满足这一属性：
     * 对于范围 1 <= i < nums.length - 1 中的每个 i ，(nums[i-1] + nums[i+1]) / 2 不等于 nums[i] 均成立 。
     * 输入：nums = [1,2,3,4,5]
     * 输出：[1,2,4,5,3]
     */
    public int[] rearrangeArray(int[] nums) {
        Arrays.sort(nums);
        for (int i = 1; i < nums.length - 1; i += 2) {
            int temp = nums[i];
            nums[i] = nums[i + 1];
            nums[i + 1] = temp;
        }
        return nums;
    }

    /**
     * 给你一个正整数 p 。你有一个下标从 1 开始的数组 nums ，这个数组包含范围 [1, 2p - 1] 内所有整数的二进制形式（两端都 包含）。
     * 你可以进行以下操作 任意 次：
     * - 从 nums 中选择两个元素 x 和 y  。
     * - 选择 x 中的一位与 y 对应位置的位交换。对应位置指的是两个整数 相同位置 的二进制位。
     * 输入：p = 3
     * 输出：1512
     * 解释：nums = [001, 010, 011, 100, 101, 110, 111]
     * - 第一次操作中，我们交换第二个和第五个元素最左边的数位。结果数组为 [001, 110, 011, 100, 001, 110, 111] 。
     * - 第二次操作中，我们交换第三个和第四个元素中间的数位。结果数组为 [001, 110, 001, 110, 001, 110, 111] 。
     * 数组乘积 1 * 6 * 1 * 6 * 1 * 6 * 7 = 1512 是最小乘积。
     * !!!思路是快速幂:
     */
    private final int MOD = (int) 1e9 + 7;

    public int minNonZeroProduct(int p) {
        long a = (long) Math.pow(2, p) - 1;
        long b = a - 1;
        long c = (long) Math.pow(2, p - 1) - 1;
        // (a * b) % c <-> a % c * (b % c) % c
        long ans = a % MOD * quickPow(b % MOD, c) % MOD;
        return (int) ans;
    }

    private long quickPow(long x, long n) {
        long res = 1;
        while (n > 0) {
            if ((n & 1) == 1) {
                res = res * x % MOD; // 1
            }
            x = x * x % MOD; // 此处与1处可以这样写的前提是传进来的参数x已经取余
            n >>= 1;
        }
        return res;
    }

}
