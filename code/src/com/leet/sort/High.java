package com.leet.sort;

import java.util.Arrays;

/**
 * @Author: wenhongliang
 */
public class High {
    // [1,5,1,1,6,4]
    // [1,6,1,5,1,4]
    public void wiggleSort(int[] nums) {
        Arrays.sort(nums);
        int[] res = new int[nums.length];
        int idx = nums.length - 1;
        for (int i = 1; i < nums.length; i += 2) {
            res[i] = nums[idx--];
        }
        for (int i = 0; i < nums.length; i += 2) {
            res[i] = nums[idx--];
        }
        for (int i = 0; i < nums.length; i++) {
            nums[i] = res[i];
        }
    }

    public int kthSmallest(int[][] matrix, int k) {
        int len = matrix.length;
        int left = matrix[0][0], right = matrix[len - 1][len - 1];
        while (left < right) {
            int mid = left + ((right - left) >> 1);
            int count = count(matrix, mid);
            if (count < k) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    private int count(int[][] matrix, int mid) {
        int row = 0;
        int column = matrix.length - 1;
        int cnt = 0;
        for (int i = 0; i < matrix.length; i++) {
            while (column >= 0 && matrix[i][column] > mid) {
                column--;
            }
            cnt += (column + 1);
            if (column == -1) break;
        }
        return cnt;
    }

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int m = nums1.length, n = nums2.length;
        int i = 0, j = 0;
        int index = 0, mid = (m + n) / 2;
        int pre = 0, cur = 0;
        while (index <= mid) {
            index++;
            pre = cur;
            if (i == m) {
                cur = nums2[j++];
                continue;
            }
            if (j == n) {
                cur = nums1[i++];
                continue;
            }
            if (nums1[i] <= nums2[j]) {
                cur = nums1[i++];
            } else {
                cur = nums2[j++];
            }
        }
        if ((m + n) % 2 == 0) {
            return (pre + cur) / 2.0;
        } else {
            return cur;
        }
    }
}
