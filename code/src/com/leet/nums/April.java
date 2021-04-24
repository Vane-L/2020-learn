package com.leet.nums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class April {

    public static void main(String[] args) {
        //0424
        April april = new April();
        april.maxProduct(new int[]{-2, 3, -4});
        april.rotate(new int[]{1, 2, 3, 4, 5, 6, 7}, 3);
        april.moveZeroes(new int[]{0, 1, 0, 3, 12});
        april.productExceptSelf(new int[]{1, 2, 3, 4});
    }

    public int maxProduct(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int max = nums[0], min = nums[0];
        int res = nums[0];
        int maxEnd, minEnd;
        for (int i = 1; i < nums.length; i++) {
            maxEnd = max * nums[i];
            minEnd = min * nums[i];
            max = Math.max(Math.max(maxEnd, minEnd), nums[i]);
            min = Math.min(Math.min(maxEnd, minEnd), nums[i]);
            res = Math.max(max, res);
        }
        return res;
    }

    /**
     * 输入: nums = [1,2,3,4,5,6,7], k = 3
     * 输出: [5,6,7,1,2,3,4]
     * 解释:
     * 向右旋转 1 步: [7,1,2,3,4,5,6]
     * 向右旋转 2 步: [6,7,1,2,3,4,5]
     * 向右旋转 3 步: [5,6,7,1,2,3,4]
     */
    public void rotate(int[] nums, int k) {
        k %= nums.length;
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, nums.length - 1);
    }

    public void reverse(int[] nums, int start, int end) {
        while (start < end) {
            int temp = nums[start];
            nums[start] = nums[end];
            nums[end] = temp;
            start++;
            end--;
        }
    }

    /**
     * 输入: [0,1,0,3,12]
     * 输出: [1,3,12,0,0]
     */
    public void moveZeroes(int[] nums) {
        int j = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                int tmp = nums[j];
                nums[j++] = nums[i];
                nums[i] = tmp;
            }
        }
    }

    public int[] intersect(int[] nums1, int[] nums2) {
        List<Integer> res = new ArrayList<>();
        Arrays.sort(nums1);
        Arrays.sort(nums2);
        int i = 0, j = 0;
        while (i < nums1.length && j < nums2.length) {
            if (nums1[i] == nums2[j]) {
                res.add(nums1[i]);
                i++;
                j++;
            } else if (nums1[i] < nums2[j]) {
                i++;
            } else {
                j++;
            }
        }
        int[] array = new int[res.size()];
        for (int k = 0; k < res.size(); k++) {
            array[k] = res.get(k);
        }
        return array;
    }

    public boolean increasingTriplet(int[] nums) {
        if (nums.length < 3) {
            return false;
        }
        int a = Integer.MAX_VALUE, b = Integer.MAX_VALUE;
        for (int i = 0; i < nums.length; i++) {
            if (a >= nums[i]) {
                a = nums[i];
            } else if (b >= nums[i]) {
                b = nums[i];
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean searchMatrix(int[][] matrix, int target) {
        if (matrix == null || matrix.length == 0) {
            return false;
        }
        if (target < matrix[0][0] || target > matrix[matrix.length - 1][matrix[0].length - 1]) {
            return false;
        }
        int m = 0, n = matrix[0].length - 1;
        while (m < matrix.length && n >= 0) {
            if (matrix[m][n] == target) {
                return true;
            } else if (matrix[m][n] < target) {
                m++;
            } else {
                n--;
            }
        }
        return false;
    }

    public int[] productExceptSelf(int[] nums) {
        if (nums == null || nums.length == 0) {
            return nums;
        }
        int[] res = new int[nums.length];
        res[0] = 1;
        for (int i = 1; i < nums.length; i++) {
            res[i] = res[i - 1] * nums[i - 1];
        }
        int right = 1;
        for (int i = nums.length - 1; i >= 0; i--) {
            res[i] *= right;
            right *= nums[i];
        }
        return res;
    }
}
