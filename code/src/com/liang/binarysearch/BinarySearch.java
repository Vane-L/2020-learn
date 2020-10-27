package com.liang.binarysearch;

/**
 * @Author: wenhongliang
 */
public class BinarySearch {
    public static void main(String[] args) {
        BinarySearch bs = new BinarySearch();
        System.out.println(bs.peakIndexInMountainArray(new int[]{0, 2, 1, 0}));
        System.out.println(bs.peakIndexInMountainArray2(new int[]{0, 1, 0}));
        System.out.println(bs.findPeakElement(new int[]{1, 2, 3, 1}));
        System.out.println(bs.findPeakElement2(new int[]{1, 2, 1, 3, 5, 6, 4}));
    }

    public int peakIndexInMountainArray(int[] arr) {
        int i = 0;
        while (arr[i] < arr[i + 1]) i++;
        return i;
    }

    public int peakIndexInMountainArray2(int[] arr) {
        int left = 0, right = arr.length - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] < arr[mid + 1]) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    public int findPeakElement(int[] nums) {
        for (int i = 0; i < nums.length - 1; i++) {
            if (nums[i] > nums[i + 1])
                return i;
        }
        return nums.length - 1;
    }

    public int findPeakElement2(int[] nums) {
        int left = 0, right = nums.length - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] > nums[mid + 1]) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        return left;
    }
}
