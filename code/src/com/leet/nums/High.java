package com.leet.nums;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @Author: wenhongliang
 */
public class High {
    public static void main(String[] args) {
        High high = new High();
        System.out.println(high.productExceptSelf(new int[]{1, 2, 3, 4}));
        System.out.println(high.productExceptSelf2(new int[]{1, 2, 3, 4}));
    }

    /**
     * 输入: [1,2,3,4]
     * 输出: [24,12,8,6]
     * [1,1,2,6]
     * [24,12,4,1]
     */
    public int[] productExceptSelf(int[] nums) {
        int[] left = new int[nums.length];
        int[] right = new int[nums.length];
        Arrays.fill(left, 1);
        Arrays.fill(right, 1);
        for (int i = 1; i < nums.length; i++) {
            left[i] = left[i - 1] * nums[i - 1];
        }
        for (int i = nums.length - 2; i >= 0; i--) {
            right[i] = right[i + 1] * nums[i + 1];
        }
        int[] res = new int[nums.length];
        for (int i = 0; i < nums.length; i++) {
            res[i] = left[i] * right[i];
        }
        return res;
    }

    public int[] productExceptSelf2(int[] nums) {
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

    public int findDuplicate(int[] nums) {
        int slow = nums[0];
        int fast = nums[nums[0]];
        //寻找相遇点
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[nums[fast]];
        }
        //slow 从起点出发, fast 从相遇点出发, 一次走一步
        slow = 0;
        while (slow != fast) {
            slow = nums[slow];
            fast = nums[fast];
        }
        return slow;
    }

    public int[] maxSlidingWindow239(int[] nums, int k) {
        int len = nums.length;
        Deque<Integer> deque = new LinkedList<>();
        for (int i = 0; i < k; ++i) {
            while (!deque.isEmpty() && nums[i] >= nums[deque.peekLast()]) {
                deque.pollLast();
            }
            deque.offerLast(i);
        }

        int[] res = new int[len - k + 1];
        res[0] = nums[deque.peekFirst()];

        for (int i = k; i < len; ++i) {
            while (!deque.isEmpty() && nums[i] >= nums[deque.peekLast()]) {
                deque.pollLast();
            }
            deque.offerLast(i);
            while (deque.peekFirst() <= i - k) {
                deque.pollFirst();
            }
            res[i - k + 1] = nums[deque.peekFirst()];
        }
        return res;
    }

    public int[] maxSlidingWindow(int[] nums, int k) {
        if (nums == null || nums.length < k) {
            return null;
        }
        LinkedList<Integer> list = new LinkedList<>();
        int[] res = new int[nums.length - k + 1];
        int idx = 0;
        for (int i = 0; i < nums.length; i++) {
            while (!list.isEmpty() && nums[list.peekLast()] <= nums[i]) {
                list.pollLast();
            }
            list.addLast(i);
            if (list.peekFirst() == i - k) {
                list.pollFirst();
            }
            if (i >= k - 1) {
                res[idx++] = nums[list.peekFirst()];
            }
        }
        return res;
    }

    // 76. 最小覆盖子串

    /**
     * int left = 0, right = 0;
     * while (right < s.size()) {
     * // 增大窗口
     * window.add(s[right]);
     * right++;
     * while (window needs shrink) {
     * // 缩小窗口
     * window.remove(s[left]);
     * left++;
     * }
     * }
     */
    public String minWindow(String s, String t) {
        HashMap<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) {
            // need记录需要出现的字符次数
            need.put(c, need.getOrDefault(c, 0) + 1);
        }

        HashMap<Character, Integer> window = new HashMap<>();
        int left = 0, right = 0;
        // 记录出现字符的个数
        int valid = 0;
        // 记录最小覆盖字串的起始索引及长度
        int start = 0, len = Integer.MAX_VALUE;
        while (right < s.length()) {
            char c = s.charAt(right);
            right++;
            // 判断取出的字符是否在字串中
            if (need.containsKey(c)) {
                // window记录出现字符的次数
                window.put(c, window.getOrDefault(c, 0) + 1);
                if (window.get(c).equals(need.get(c))) {
                    valid++;
                }
            }

            // 判断是否需要收缩
            while (valid == need.size()) {
                // 更新最小覆盖子串
                if (right - left < len) {
                    start = left;
                    len = right - left;
                }

                // c1是将移出窗口的字符
                char c1 = s.charAt(left);
                left++;
                if (need.containsKey(c1)) {
                    if (window.get(c1).equals(need.get(c1))) {
                        valid--;
                    }
                    window.put(c1, window.getOrDefault(c1, 0) - 1);
                }

            }
        }

        return len == Integer.MAX_VALUE ? "" : s.substring(start, start + len);
    }

}
