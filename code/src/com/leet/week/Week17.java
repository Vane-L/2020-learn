package com.leet.week;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: wenhongliang
 */
public class Week17 {

    public static void main(String[] args) {
        System.out.println(new Week17().countPoints("B0B6G0R6R0R6G9"));
        System.out.println(new Week17().subArrayRanges(new int[]{1, 2, 3}));
        System.out.println(new Week17().minimumRefill(
                new int[]{726, 739, 934, 116, 643, 648, 473, 984, 482, 85, 850, 806, 146, 764, 156, 66, 186, 339, 985, 237, 662, 552, 800, 78, 617, 933, 481, 652, 796, 594, 151, 82, 183, 241, 525, 221, 951, 732, 799, 483, 368, 354, 776, 175, 974, 187, 913, 842},
                1439, 1207));
        System.out.println(new Week17().maxTotalFruits(new int[][]{{2, 8}, {6, 3}, {8, 6}}, 5, 4));
        System.out.println(new Week17().maxTotalFruits(new int[][]{{0, 9}, {4, 1}, {5, 7}, {6, 2}, {7, 4}, {10, 9}}, 5, 4));
    }

    /**
     * Easy 5952. 环和杆
     * https://leetcode-cn.com/problems/rings-and-rods/
     */
    public int countPoints(String rings) {
        if (rings.length() / 2 < 3) return 0;
        char[] ch = rings.toCharArray();
        Map<Character, Set<Character>> map = new HashMap<>();
        for (int i = 0; i < ch.length; i += 2) {
            Set<Character> set = map.getOrDefault(ch[i + 1], new HashSet<>());
            set.add(ch[i]);
            map.put(ch[i + 1], set);
        }
        int res = 0;
        for (Map.Entry<Character, Set<Character>> entry : map.entrySet()) {
            if (entry.getValue().size() == 3) res++;
        }
        return res;
    }

    /**
     * Medium 5953. 子数组范围和
     * https://leetcode-cn.com/problems/sum-of-subarray-ranges/
     */
    public long subArrayRanges(int[] nums) {
        long res = 0;
        for (int i = 0; i < nums.length; i++) {
            int min = nums[i], max = nums[i];
            for (int j = i + 1; j < nums.length; j++) {
                min = Math.min(min, nums[j]);
                max = Math.max(max, nums[j]);
                res += max - min;
            }
        }
        return res;
    }

    /**
     * Medium 5954. 给植物浇水 II
     * https://leetcode-cn.com/problems/watering-plants-ii/
     */
    public int minimumRefill(int[] plants, int capacityA, int capacityB) {
        int a = capacityA, b = capacityB;
        int left = 0, right = plants.length - 1;
        int res = 0;
        while (left < right) {
            if (capacityA < plants[left]) {
                res++;
                capacityA = a;
            }
            capacityA -= plants[left++];
            if (capacityB < plants[right]) {
                res++;
                capacityB = b;
            }
            capacityB -= plants[right--];
        }
        if (left == right) {
            int max = Math.max(capacityA, capacityB);
            if (max < plants[left]) res++;
        }
        return res;
    }


    /**
     * Hard 5955. 摘水果
     * https://leetcode-cn.com/problems/maximum-fruits-harvested-after-at-most-k-steps/
     */
    public int maxTotalFruits(int[][] fruits, int startPos, int k) {
        int res = 0;
        int min = Math.max(0, startPos - k);
        int max = startPos + k;
        // 我的想法：排序+贪心
        Arrays.sort(fruits, (a, b) -> (b[1] - a[1]));
        for (int[] fruit : fruits) {
            int position = fruit[0];
            int amount = fruit[1];
            if (position < min || position > max || startPos + k < position) continue;
            res += amount;
            k -= position - startPos;
        }
        return res;
    }

    public int maxTotalFruits0(int[][] fruits, int startPos, int k) {
        int min = startPos - k;
        int max = startPos + k;
        int fin = 0;
        // 只用在可达的范围[min,max]内寻找草莓
        int[] dp = new int[2 * k + 1];
        for (int[] fruit : fruits) {
            if (fruit[0] >= min && fruit[0] <= max) {
                dp[fruit[0] - startPos + k] = fruit[1];
            }
        }
        int start = dp[k]; // 预先保留起点草莓数
        dp[k] = 0;
        for (int i = 1; i <= k; i++) {
            //  a[i]为从左(右)走i步时可获取的草莓总数
            //  步数k满足k=2*s+i,s为先走消耗的步数,i为后走消耗的步数
            //  分别判断先走左和右的最值，再比较当前最大值
            dp[k + i] += dp[k + i - 1];
            dp[k - i] += dp[k - i + 1];
            int s = (k - i) / 2;
            fin = Math.max(Math.max(dp[k - s] + dp[k + i], dp[k + s] + dp[k - i]), fin);
        }
        return start + fin;
    }
}
