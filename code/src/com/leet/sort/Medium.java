package com.leet.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @Author: wenhongliang
 */
public class Medium {
    public void sortColors(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return;
        }
        int count0 = 0, count1 = 0, count2 = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) {
                count0++;
            } else if (nums[i] == 1) {
                count1++;
            } else {
                count2++;
            }
        }
        int idx = 0;
        while (count0-- > 0) {
            nums[idx++] = 0;
        }
        while (count1-- > 0) {
            nums[idx++] = 1;
        }
        while (count2-- > 0) {
            nums[idx++] = 2;
        }
    }

    public int[] topKFrequent(int[] nums, int k) {
        Queue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>((a, b) -> (b.getValue() - a.getValue()));
        Map<Integer, Integer> map = new HashMap<>();
        for (int x : nums) {
            map.put(x, map.getOrDefault(x, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            queue.offer(entry);
            if (queue.size() > k) {
                queue.poll();
            }
        }
        int[] res = new int[k];
        int idx = 0;
        while (!queue.isEmpty()) {
            res[idx++] = queue.poll().getKey();
        }
        return res;
    }

    public int findKthLargest(int[] nums, int k) {
        Queue<Integer> queue = new PriorityQueue<>((a, b) -> (a - b));
        for (int x : nums) {
            queue.offer(x);
            if (queue.size() > k) {
                queue.poll();
            }
        }
        return queue.poll();
    }

    public int findPeakElement(int[] nums) {
        if (nums.length < 2) {
            return 0;
        }
        int left = nums[0];
        for (int i = 1; i < nums.length - 1; i++) {
            if (left < nums[i] && nums[i] > nums[i + 1]) {
                return i;
            }
        }
        return nums[nums.length - 2] < nums[nums.length - 1] ? nums.length - 1 : 0;
    }

    public int[] searchRange(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            return new int[]{-1, -1};
        }
        if (target < nums[0] || target > nums[nums.length - 1]) {
            return new int[]{-1, -1};
        }
        int left = 0, right = nums.length - 1;
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] < target) {
                left = mid + 1;
            } else if (nums[mid] > target) {
                right = mid - 1;
            } else {
                int start = mid;
                while (start > 0 && nums[start] == target) {
                    start--;
                }
                int end = mid;
                while (end < nums.length && nums[end] == target) {
                    end++;
                }
                return new int[]{start, end};
            }
        }
        return new int[]{-1, -1};
    }

    public int[][] merge(int[][] intervals) {
        if (intervals.length == 0) {
            return new int[0][2];
        }
        Arrays.sort(intervals, (v1, v2) -> (v1[0] - v2[0]));
        List<int[]> merged = new ArrayList<>();
        for (int i = 0; i < intervals.length; ++i) {
            int left = intervals[i][0], right = intervals[i][1];
            if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < left) {
                merged.add(new int[]{left, right});
            } else {
                merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], right);
            }
        }
        return merged.toArray(new int[merged.size()][]);
    }

    public int search(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            return -1;
        }
        int left = 0, right = nums.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (nums[mid] == target) {
                return mid;
            } else if (nums[left] <= nums[mid]) {
                if (target >= nums[left] && target < nums[mid]) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            } else {
                if (nums[right] >= target && target > nums[mid]) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }
        return -1;
    }

    public boolean searchMatrix(int[][] matrix, int target) {
        if (matrix == null || matrix.length == 0) {
            return false;
        }
        int i = 0, j = matrix[0].length - 1;
        while (i < matrix.length && j >= 0) {
            if (matrix[i][j] == target) {
                return true;
            }
            if (matrix[i][j] < target) {
                i++;
            } else {
                j--;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Medium medium = new Medium();
        medium.sortColors(new int[]{2, 0, 2, 1, 1, 0});
        System.out.println(medium.findKthLargest(new int[]{3, 2, 1, 5, 6, 4}, 2));
        System.out.println(medium.search(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
        System.out.println(medium.search(new int[]{4, 5, 6, 7, 0, 1, 2}, 5));
    }
}
