package com.leet.week;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class Week19 {
    public static void main(String[] args) {
        System.out.println(new Week19().recoverArray(new int[]{11, 6, 3, 4, 8, 7, 8, 7, 9, 8, 9, 10, 10, 2, 1, 9}));
    }

    public long[] getDistancesTimeOut(int[] arr) {
        int len = arr.length;
        long[] res = new long[len];
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < len; i++) {
            List<Integer> idxList = map.getOrDefault(arr[i], new ArrayList<>());
            idxList.add(i);
            map.put(arr[i], idxList);
        }

        for (int i = 0; i < len; i++) {
            List<Integer> idxList = map.get(arr[i]);
            long sum = 0;
            for (int x : idxList) {
                sum += Math.abs(x - i);
            }
            res[i] = sum;
        }
        return res;
    }

    /**
     * Medium 5965. 相同元素的间隔之和 >>> 画图
     * 输入：arr = [2,1,3,1,2,3,3]
     * 输出：[4,2,7,2,4,4,5]
     * 解释：
     * - 下标 0 ：另一个 2 在下标 4 ，|0 - 4| = 4
     * - 下标 1 ：另一个 1 在下标 3 ，|1 - 3| = 2
     * - 下标 2 ：另两个 3 在下标 5 和 6 ，|2 - 5| + |2 - 6| = 7
     * - 下标 3 ：另一个 1 在下标 1 ，|3 - 1| = 2
     * - 下标 4 ：另一个 2 在下标 0 ，|4 - 0| = 4
     * - 下标 5 ：另两个 3 在下标 2 和 6 ，|5 - 2| + |5 - 6| = 4
     * - 下标 6 ：另两个 3 在下标 2 和 5 ，|6 - 2| + |6 - 5| = 5
     */
    public long[] getDistances(int[] arr) {
        int len = arr.length;
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < len; i++) {
            List<Integer> idxList = map.getOrDefault(arr[i], new ArrayList<>());
            idxList.add(i);
            map.put(arr[i], idxList);
        }

        long[] res = new long[len];
        for (List<Integer> list : map.values()) {
            for (int i : list) {
                // |2 - 2|+|2 - 5|+|2 - 6| = 7
                res[list.get(0)] += i - list.get(0);
            }
            for (int i = 1; i < list.size(); i++) {
                // i=1, {|2 - 2|+|2 - 5|+|2 - 6|} + {(2*1-3)*(5-2)} = 7 + (-3) = |5 - 2| + |5 - 6| = 4
                res[list.get(i)] = res[list.get(i - 1)] + (2 * i - list.size()) * (list.get(i) - list.get(i - 1));
            }
        }
        return res;
    }

    /**
     * Hard 5966. 还原原数组
     * 数组中最小的值必定在lower 中, 枚举最小数在higher 中对应的数字，检查k 是否正确 >>> (nums[i] - nums[0])%2==0
     * 若nums[i] 在lower 中，那么数组中肯定存在对应higher 中的值nums[i]+2k >>> nums[i] = nums[lower]+k = nums[higher]-k >>>  nums[lower]+2k = nums[higher]
     * 排序后每次将需要寻找的nums[i]+2k 放入队列，检查是否存在
     */
    public int[] recoverArray(int[] nums) {
        int len = nums.length;
        int[] arr = new int[len / 2];
        Arrays.sort(nums);
        for (int i = 1; i < len; i++) {
            if (nums[i] == nums[0] || (nums[i] - nums[0]) % 2 != 0) continue;
            int k = (nums[i] - nums[0]) / 2;
            int idx = 0;
            ArrayDeque<Integer> deque = new ArrayDeque<>();
            for (int num : nums) {
                if (!deque.isEmpty() && deque.peek() == num) {
                    deque.poll();
                    continue;
                }
                if (idx == arr.length) {
                    break;
                }
                deque.offer(num + 2 * k);
                arr[idx++] = num + k;
            }
            if (deque.isEmpty()) {
                break;
            }
        }
        return arr;
    }
}
