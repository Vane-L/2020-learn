package com.leet.may;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: wenhongliang
 */
public class HappyNum {
    public static void main(String[] args) {
        System.out.println(new HappyNum().isHappy(19));
        System.out.println(new HappyNum().isHappy(2));
        System.out.println(new HappyNum().getSum(2, 3));
        new HappyNum().rotate(new int[]{1, 2, 3, 4, 5, 6, 7}, 2);
        System.out.println(new HappyNum().isPalindrome("A man, a plan, a canal: Panama"));
        System.out.println(new HappyNum().strStr("mississippi", "issip"));
    }

    public boolean isHappy(int n) {
        Set<Integer> set = new HashSet<>();
        while (n != 1 && !set.contains(n)) {
            set.add(n);
            n = getNext(n);
        }
        return n == 1;
    }

    public int getNext(int n) {
        int sum = 0;
        while (n > 0) {
            int d = n % 10;
            n /= 10;
            sum += d * d;
        }
        return sum;
    }

    public int getSum(int a, int b) {
        while (b != 0) {
            int tmp = a & b;
            a ^= b;
            b = tmp << 1;
        }
        return a;
    }

    public List<String> fizzBuzz(int n) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (i % 5 == 0 && i % 3 == 0) {
                list.add("FizzBuzz");
            } else if (i % 3 == 0) {
                list.add("Fizz");
            } else if (i % 5 == 0) {
                list.add("Buzz");
            } else {
                list.add(String.valueOf(i));
            }
        }
        return list;
    }

    // 加油站
    public int canCompleteCircuit(int[] gas, int[] cost) {
        int n = gas.length;
        for (int i = 0; i < n; i++) {
            int j = i;
            int remain = gas[i];
            while (remain >= cost[j]) {
                remain -= cost[j];
                j = (j + 1) % n;
                remain += gas[j];
                if (j == i) {
                    return i;
                }
            }
            if (j < i) {
                return -1;
            }
            i = j;
        }
        return -1;
    }

    public int canCompleteCircuit2(int[] gas, int[] cost) {
        int cur = 0;
        int total = 0;
        int start = 0;
        for (int i = 0; i < gas.length; i++) {
            cur += gas[i] - cost[i];
            total += gas[i] - cost[i];
            if (cur < 0) {
                cur = 0;
                start = i + 1;
            }
        }
        return total < 0 ? -1 : start;
    }

    // 输入：nums = [0,0,1,1,1,2,2,3,3,4]
    // 输出：5, nums = [0,1,2,3,4]
    public int removeDuplicates(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        int left = 0, right = nums.length - 1;
        int idx = 0;
        while (left <= right) {
            while (left < right && nums[left] == nums[left + 1]) {
                left++;
            }
            nums[idx++] = nums[left++];
        }
        return idx;
    }

    //输入: prices = [7,1,5,3,6,4]
    //输出: 7
    //解释: 在第 2 天（股票价格 = 1）的时候买入，在第 3 天（股票价格 = 5）的时候卖出, 这笔交易所能获得利润 = 5-1 = 4 。
    //     随后，在第 4 天（股票价格 = 3）的时候买入，在第 5 天（股票价格 = 6）的时候卖出, 这笔交易所能获得利润 = 6-3 = 3 。
    public int maxProfit(int[] prices) {
        if (prices == null || prices.length == 0) {
            return 0;
        }
        int res = 0;
        for (int i = 1; i < prices.length; i++) {
            res += Math.max(0, prices[i] - prices[i - 1]);
        }
        return res;
    }

    //输入: nums = [1,2,3,4,5,6,7], k = 3
    //输出: [5,6,7,1,2,3,4]
    //解释:
    //向右旋转 1 步: [7,1,2,3,4,5,6]
    //向右旋转 2 步: [6,7,1,2,3,4,5]
    //向右旋转 3 步: [5,6,7,1,2,3,4]
    public void rotate(int[] nums, int k) {
        int len = nums.length;
        k = k % len;
        merge(nums, 0, len - k - 1);
        merge(nums, len - k, len - 1);
        merge(nums, 0, len - 1);
        System.out.println(nums);
    }

    private void merge(int[] nums, int start, int end) {
        while (start <= end) {
            int tmp = nums[start];
            nums[start] = nums[end];
            nums[end] = tmp;
            start++;
            end--;
        }
    }

    // 加一
    public int[] plusOne(int[] digits) {
        for (int i = digits.length - 1; i >= 0; i--) {
            digits[i]++;
            digits[i] = digits[i] % 10;
            if (digits[i] != 0) {
                return digits;
            }
        }
        digits = new int[digits.length + 1];
        digits[0] = 1;
        return digits;
    }

    public void moveZeroes(int[] nums) {
        int count = 0;
        for (int x : nums) {
            if (x == 0) {
                count++;
            }
        }
        int idx = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                nums[idx++] = nums[i];
            }
        }
        for (int i = 0; i < count; i++) {
            nums[idx++] = 0;
        }
    }

    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if (map.containsKey(target - nums[i])) {
                return new int[]{map.get(target - nums[i]), i};
            }
            map.put(nums[i], i);
        }
        return new int[]{-1, -1};
    }

    // 旋转图像
    public void rotate(int[][] matrix) {
        for (int start = 0, end = matrix[0].length - 1; start < end; start++, end--) {
            for (int s = start, e = end; s < end; s++, e--) {
                int temp = matrix[start][s];
                matrix[start][s] = matrix[e][start];
                matrix[e][start] = matrix[end][e];
                matrix[end][e] = matrix[s][end];
                matrix[s][end] = temp;
            }
        }
    }

    // 整数翻转
    public int reverse(int x) {
        boolean negative = false;
        if (x < 0) {
            negative = true;
        }
        int res = 0;
        while (x != 0) {
            if (res > Integer.MAX_VALUE / 10 || (res == Integer.MAX_VALUE / 10 && res % 10 > 7)) {
                return 0;
            }
            if (res < Integer.MIN_VALUE / 10 || (res == Integer.MIN_VALUE / 10 && res % 10 < -8)) {
                return 0;
            }
            res = 10 * res + x % 10;
            x /= 10;
        }
        return negative ? -res : res;
    }

    // 字符串中第一个唯一字符
    public int firstUniqChar(String s) {
        int[] arr = new int[26];
        for (int i = 0; i < s.length(); i++) {
            arr[s.charAt(i) - 'a'] += 1;
        }
        for (int i = 0; i < s.length(); i++) {
            if (arr[s.charAt(i) - 'a'] == 1) { //当该位置数字为1
                return i;
            }
        }
        return -1;
    }

    // 有效的字母异位词
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }
        int[] arr = new int[26];
        for (char c : s.toCharArray()) {
            arr[c - 'a'] += 1;
        }
        for (char c : t.toCharArray()) {
            arr[c - 'a'] -= 1;
            if (arr[c - 'a'] < 0) {
                return false;
            }
        }
        return true;
    }

    // 验证回文串
    public boolean isPalindrome(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        int start = 0, end = s.length() - 1;
        char[] chars = s.toCharArray();
        while (start < end) {
            while (start < end && !Character.isLetterOrDigit(chars[start])) {
                start++;
            }
            while (start < end && !Character.isLetterOrDigit(chars[end])) {
                end--;
            }
            if (Character.toLowerCase(chars[start]) != Character.toLowerCase(chars[end])) {
                return false;
            }
            start++;
            end--;
        }
        return true;
    }

    public int myAtoi(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        int res = 0, len = s.length();
        char[] chars = s.toCharArray();
        boolean negative = false;
        int idx = 0;
        while (idx < len && chars[idx] == ' ') {
            idx++;
        }
        if (idx == len) {
            return 0;
        }
        if (chars[idx] == '-') {
            negative = true;
            idx++;
        } else if (chars[idx] == '+') {
            idx++;
        } else if (!Character.isDigit(chars[idx])) {
            return 0;
        }
        while (idx < len && Character.isDigit(chars[idx])) {
            int tmp = chars[idx] - '0';
            if (res > (Integer.MAX_VALUE - tmp) / 10) {
                return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            }
            res = res * 10 + tmp;
            idx++;
        }
        return negative ? -res : res;
    }

    // 给你两个字符串 haystack 和 needle ，请你在 haystack 字符串中找出 needle 字符串出现的第一个位置（下标从 0 开始）。
    public int strStr(String haystack, String needle) {
        int n = haystack.length();
        int m = needle.length();
        if (m == 0) {
            return 0;
        }
        if (n == 0) {
            return -1;
        }
        int start = 0;
        while (start < n - m + 1) {
            // 找到第一个相同的字符位置
            while (start < n - m + 1 && haystack.charAt(start) != needle.charAt(0)) ++start;
            int currLen = 0, end = 0;
            while (start < n && end < m && haystack.charAt(start) == needle.charAt(end)) {
                ++start;
                ++end;
                ++currLen;
            }
            if (currLen == m) return start - m;
            start = start - currLen + 1;
        }
        return -1;
    }

    public String countAndSay(int n) {
        if (n == 1) {
            return "1";
        }
        String str = "1";
        // 当n为2及以上时。因为下一个数列是对上面的解释。所以用三个变量
        // 一个代表数量count ,一个代表前一个数字pre，一个代表后一个数字back
        for (int i = 2; i <= n; i++) {
            StringBuilder builder = new StringBuilder();
            int count = 1;
            char pre = str.charAt(0); //大循环下面pre作为首数字，因为必须从第一个开始往后循环嘛，不然就遗漏了
            for (int j = 1; j < str.length(); j++) {
                char back = str.charAt(j); //后一个数字
                if (back == pre) { //相等count+1
                    count++;
                } else {
                    builder.append(count).append(pre); //不等则append几个pre
                    pre = back;
                    count = 1; //count重置
                }
            }
            //这一步是因为上层循环结束点在n-1的地方停了。并没有把最后的back加入到builder里面。并且观察数字，最后一位永远是1.所以可以直接把1个1加入到builder中。
            builder.append(count).append(pre);
            str = builder.toString();//因为方法体的数据类型是Stirng所以必须转换成String型的数据才能返回。
        }
        return str;
    }

    // 最长公共前缀
    public String longestCommonPrefix(String[] strs) {
        if (strs.length == 0) {
            return "";
        }
        if (strs.length == 1) {
            return strs[0];
        }
        String res = strs[0];
        for (int i = 1; i < strs.length; i++) {
            String str = strs[i];
            if (str.equals("") || res.equals("")) {
                return "";
            }
            int start = 0;
            while (start < res.length() && start < str.length() && str.charAt(start) == res.charAt(start)) {
                start++;
            }
            res = res.substring(0, start);
        }
        return res;
    }

}
