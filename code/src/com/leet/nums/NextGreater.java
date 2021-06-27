package com.leet.nums;

import com.leet.tree.TreeNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @Author: wenhongliang
 */
public class NextGreater {
    // 496 下一个更大元素 I
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums2.length; i++) {
            map.put(nums2[i], i);
        }
        for (int i = 0; i < nums1.length; i++) {
            int tmp = nums1[i];
            int idx = map.get(tmp) + 1;
            while (idx < nums2.length) {
                if (nums2[idx] > tmp) {
                    nums1[i] = nums2[idx];
                    break;
                }
                idx++;
            }
            if (idx >= nums2.length) {
                nums1[i] = -1;
            }
        }
        return nums1;
    }

    // 496 下一个更大元素 I
    public int[] nextGreaterElementStack(int[] nums1, int[] nums2) {
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums2.length; i++) {
            while (!stack.empty() && nums2[i] > stack.peek()) {
                map.put(stack.pop(), nums2[i]);
            }
            stack.push(nums2[i]);
        }
        int[] res = new int[nums1.length];
        for (int i = 0; i < nums1.length; i++) {
            res[i] = map.getOrDefault(nums1[i], -1);
        }
        return res;
    }

    // 503. 下一个更大元素 II
    public int[] nextGreaterElements(int[] nums) {
        int len = nums.length;
        int[] res = new int[len];
        Arrays.fill(res, -1);
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < 2 * len; i++) {
            while (!stack.empty() && nums[i % len] > nums[stack.peek()]) {
                res[stack.pop()] = nums[i % len];
            }
            stack.push(i % len);
        }
        return res;
    }

    // 556. 下一个更大元素 III
    public int nextGreaterElement(int n) {
        char[] chars = String.valueOf(n).toCharArray();
        int len = chars.length;
        Stack<Integer> stack = new Stack<>();
        int idx = -1;
        for (int i = len - 1; i >= 0; i--) {
            while (!stack.isEmpty() && chars[stack.peek()] > chars[i]) {
                idx = stack.pop();
            }
            if (idx != -1) {
                char c = chars[i];
                chars[i] = chars[idx];
                chars[idx] = c;
                int left = i + 1, right = len - 1;
                while (left < right) {
                    char c1 = chars[left];
                    chars[left] = chars[right];
                    chars[right] = c1;
                    left++;
                    right--;
                }
                long res = Long.parseLong(String.valueOf(chars));
                if (res > Integer.MAX_VALUE) return -1;
                else return (int) res;
            }
            stack.push(i);
        }
        return -1;
    }

    public int nextGreaterElement2(int n) {
        char[] arr = String.valueOf(n).toCharArray();

        int i = arr.length - 2;
        while (i >= 0 && arr[i] >= arr[i + 1]) {
            i--;
        }
        // 组成的数字都是非递增
        if (i < 0) return -1;

        int j = arr.length - 1;
        while (arr[j] <= arr[i]) {
            j--;
        }
        // 交换第一个比i大的数j
        swap(arr, i, j);
        // 翻转i+1到末尾的数
        reverse(arr, i + 1, arr.length - 1);

        long ret = Long.parseLong(new String(arr));
        return ret > Integer.MAX_VALUE ? -1 : (int) ret;
    }

    private void reverse(char[] arr, int s, int e) {
        while (s < e) {
            swap(arr, s++, e--);
        }
    }

    private void swap(char[] arr, int a, int b) {
        arr[a] ^= arr[b];
        arr[b] ^= arr[a];
        arr[a] ^= arr[b];
    }

    //单调栈
    public int[] nextGreaterElementTemplate(int[] nums) {
        int len = nums.length;
        int[] result = new int[len]; // 存放答案的数组
        Arrays.fill(result, -1);
        Stack<Integer> stack = new Stack<>();
        for (int i = len - 1; i >= 0; i--) { // 倒着往栈里放
            // 判定高矮
            while (!stack.isEmpty() && stack.peek() <= nums[i]) {
                // 矮个起开，反正也被挡着了。。。
                stack.pop();
            }
            // nums[i] 身后的第一个高的
            result[i] = !stack.isEmpty() ? stack.peek() : -1;
            // 进队，接受之后的⾝⾼判定吧
            stack.push(nums[i]);
        }
        return result;
    }

    // temperatures = [73, 74, 75, 71, 69, 72, 76, 73]，你的输出应该是 [1, 1, 4, 2, 1, 1, 0, 0]
    public int[] dailyTemperatures(int[] temperatures) {
        int len = temperatures.length;
        int[] result = new int[len];
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < len; i++) {
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
                int prevIndex = stack.pop();
                result[prevIndex] = i - prevIndex;
            }
            stack.push(i);
        }
        /*错误单调栈
        for (int i = len - 1; i >= 0; i--) {
            int count = 0;
            while (!stack.empty() && temperatures[stack.peek()] < temperatures[i]) {
                stack.pop();
                count++;
            }
            result[i] = count;
            stack.push(i);
        }*/
        return result;
    }
}
