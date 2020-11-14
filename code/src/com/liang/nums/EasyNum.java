package com.liang.nums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class EasyNum {

    /**
     * 输入: cost = [1, 100, 1, 1, 1, 100, 1, 1, 100, 1]
     * 输出: 6
     * 解释: 最低花费方式是从cost[0]开始，逐个经过那些1，跳过cost[3]，一共花费6。
     */
    public int minCostClimbingStairs(int[] cost) {
        if (cost == null || cost.length == 0) {
            return 0;
        }
        if (cost.length == 1) {
            return cost[0];
        }
        if (cost.length == 2) {
            return Math.min(cost[0], cost[1]);
        }
        int len = cost.length;
        int[] dp = new int[len];
        dp[0] = cost[0];
        dp[1] = cost[1];
        for (int i = 2; i < len; i++) {
            dp[i] = Math.min(dp[i - 1], dp[i - 2]) + cost[i];
        }
        return Math.min(dp[len - 1], dp[len - 2]);
    }

    /**
     * 输入： 3
     * 输出： 3
     * 解释： 有三种方法可以爬到楼顶。
     * 1.  1 阶 + 1 阶 + 1 阶
     * 2.  1 阶 + 2 阶
     * 3.  2 阶 + 1 阶
     */
    public int climbStairs(int n) {
        if (n <= 2) {
            return n;
        }
        int a = 1, b = 1;
        for (int i = 2; i <= n; i++) {
            b = a + b;
            a = b - a;
        }
        return b;
    }

    /**
     * 输入：[1,8,6,2,5,4,8,3,7]
     * 输出：49
     */
    public int maxArea(int[] height) {
        int result = 0;
        int left = 0, right = height.length - 1;
        while (left < right) {
            result = Math.max(result, (right - left) * Math.min(height[left], height[right]));
            if (height[left] < height[right]) {
                result = Math.max(result, (right - left) * height[left++]);
            } else {
                result = Math.max(result, (right - left) * height[right--]);
            }
        }
        return result;
    }

    /**
     * 给定数组 nums = [-1, 0, 1, 2, -1, -4]，
     * 满足要求的三元组集合为：
     * [
     * [-1, 0, 1],
     * [-1, -1, 2]
     * ]
     */
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 0) {
                return res;
            }
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            int left = i + 1, right = nums.length - 1;
            while (left < right) {
                int sum = nums[left] + nums[right] + nums[i];
                if (sum == 0) {
                    res.add(Arrays.asList(nums[i], nums[left], nums[right]));
                    while (left < right && nums[left] == nums[left + 1]) {
                        left++;
                    }
                    while (left < right && nums[right] == nums[right - 1]) {
                        right--;
                    }
                    left++;
                    right--;
                } else if (sum < 0) {
                    left++;
                } else {
                    right--;
                }
            }
        }
        return res;
    }

    /**
     * 给定数组 nums = [1, 0, -1, 0, -2, 2]，和 target = 0。
     * 满足要求的四元组集合为：
     * [
     * [-1,  0, 0, 1],
     * [-2, -1, 1, 2],
     * [-2,  0, 0, 2]
     * ]
     */
    public List<List<Integer>> fourSum(int[] nums, int target) {
        List<List<Integer>> res = new ArrayList<>();
        if (nums == null || nums.length < 4) {
            return res;
        }
        Arrays.sort(nums);
        int len = nums.length;
        for (int i = 0; i < len - 3; i++) {
            if (nums[i] > target / 4) {
                return res;
            }
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }
            for (int j = i + 1; j < len - 2; j++) {
                if (j > i + 1 && nums[j] == nums[j - 1]) {
                    continue;
                }
                int left = j + 1, right = len - 1;
                while (left < right) {
                    int sum = nums[left] + nums[right] + nums[i] + nums[j];
                    if (sum == target) {
                        res.add(Arrays.asList(nums[i], nums[j], nums[left], nums[right]));
                        while (left < right && nums[left] == nums[left + 1]) {
                            left++;
                        }
                        while (left < right && nums[right] == nums[right - 1]) {
                            right--;
                        }
                        left++;
                        right--;
                    } else if (sum < target) {
                        left++;
                    } else {
                        right--;
                    }
                }
            }
        }
        return res;
    }

    public List<List<Integer>> fourSum2(int[] nums, int target) {
        List<List<Integer>> res = new ArrayList<>();
        if (nums == null || nums.length < 4) {
            return res;
        }
        Arrays.sort(nums);
        int len = nums.length;
        for (int index1 = 0; index1 < len - 3; index1++) {
            if (index1 > 0 && nums[index1] == nums[index1 - 1]) {
                continue;
            }
            int curMin = nums[index1] + nums[index1 + 1] + nums[index1 + 2] + nums[index1 + 3];
            if (curMin > target) {
                break;
            }
            int curMax = nums[index1] + nums[len - 1] + nums[len - 2] + nums[len - 3];
            if (curMax < target) {
                continue;
            }
            for (int index2 = index1 + 1; index2 < len - 2; index2++) {
                if (index2 > index1 + 1 && nums[index2] == nums[index2 - 1]) {
                    continue;
                }
                int index3 = index2 + 1;
                int maxIndex = len - 1;
                curMin = nums[index1] + nums[index2] + nums[index3] + nums[index3 + 1];
                if (curMin > target) {
                    break;
                }
                curMax = nums[index1] + nums[index2] + nums[maxIndex] + nums[maxIndex - 1];
                if (curMax < target) {
                    continue;
                }

                while (index3 < maxIndex) {
                    int sum = nums[index1] + nums[index2] + nums[index3] + nums[maxIndex];
                    if (sum == target) {
                        res.add(Arrays.asList(nums[index1], nums[index2], nums[index3], nums[maxIndex]));
                        while (index3 < maxIndex && nums[index3] == nums[index3 + 1]) {
                            index3++;
                        }
                        while (index3 < maxIndex && nums[maxIndex] == nums[maxIndex - 1]) {
                            maxIndex--;
                        }
                        index3++;
                        maxIndex--;
                    } else if (sum < target) {
                        index3++;
                    } else {
                        maxIndex--;
                    }
                }
            }
        }
        return res;
    }

    /**
     * 输入：arr1 = [2,3,1,3,2,4,6,7,9,2,19], arr2 = [2,1,4,3,9,6]
     * 输出：      [2,2,2,1,4,3,3,9,6,7,19]
     */
    public int[] relativeSortArray(int[] arr1, int[] arr2) {
        int[] bucket = new int[1001];
        for (int num : arr1) {
            bucket[num]++;
        }
        int i = 0;
        for (int num : arr2) {
            while (bucket[num]-- > 0) {
                arr1[i++] = num;
            }
        }
        for (int j = 0; j < 1001; ++j) {
            while (bucket[j]-- > 0) {
                arr1[i++] = j;
            }
        }
        return arr1;
    }

    public static void main(String[] args) {
        EasyNum easy = new EasyNum();
        System.out.println(easy.climbStairs(5));
        System.out.println(easy.maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}));
        System.out.println(easy.threeSum(new int[]{-1, 0, 1, 2, -1, -4}));
        System.out.println(easy.threeSum(new int[]{0, 0, 0}));
        System.out.println(easy.fourSum(new int[]{1, 0, -1, 0, -2, 2}, 0));
    }

}