package com.leet.week;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class Week6 {

    /**
     * Easy AC
     * 给你一个 下标从 0 开始 的整数数组 nums ，返回满足下述条件的 不同 四元组 (a, b, c, d) 的 数目 ：
     * nums[a] + nums[b] + nums[c] == nums[d] ，且 a < b < c < d
     */
    public int countQuadruplets(int[] nums) {
        int res = 0;
        int len = nums.length;
        if (len == 4) return nums[0] + nums[1] + nums[2] == nums[3] ? 1 : 0;
        for (int i = 0; i < len - 3; i++) {
            for (int j = i + 1; j < len - 2; j++) {
                for (int k = j + 1; k < len - 1; k++) {
                    for (int l = k + 1; l < len; l++) {
                        if (nums[i] + nums[j] + nums[k] == nums[l]) {
                            res++;
                        }
                    }
                }
            }
        }
        return res;
    }

    /**
     * Medium 超时
     * 如果存在一个其他角色的攻击和防御等级 都严格高于 该角色的攻击和防御等级，则认为该角色为 弱角色 。
     * 更正式地，如果认为角色 i 弱于 存在的另一个角色 j ，那么 attackj > attacki 且 defensej > defensei 。
     * 返回 弱角色 的数量。
     */
    public int numberOfWeakCharacters(int[][] properties) {
        Arrays.sort(properties,
                ((o1, o2) -> {
                    if (o1[0] == o2[0]) {
                        return o1[1] - o2[1];
                    } else {
                        return o1[0] - o2[0];
                    }
                })
        );
        int res = 0;
        for (int i = 0; i < properties.length; i++) {
            for (int j = i + 1; j < properties.length; j++) {
                if (properties[j][0] > properties[i][0] && properties[j][1] > properties[i][1]) {
                    res++;
                    break;
                }
            }
        }
        return res;
    }


    public int numberOfWeakCharacters0(int[][] properties) {
        // 按照攻击从大到小排序，攻击相同的按照防御从小到大排序
        Arrays.sort(properties, (a, b) -> a[0] == b[0] ? a[1] - b[1] : b[0] - a[0]);
        int count = 0, max = 0;
        for (int[] property : properties) {
            count += max > property[1] ? 1 : 0;
            // 最大防御
            max = Math.max(max, property[1]);
        }
        return count;
    }


    /**
     * Medium
     * 最开始的第 0 天，你访问 0 号房间。给你一个长度为 n 且 下标从 0 开始 的数组 nextVisit 。在接下来的几天中，你访问房间的 次序 将根据下面的 规则 决定：
     * 假设某一天，你访问 i 号房间。
     * 如果算上本次访问，访问 i 号房间的次数为 奇数 ，那么 第二天 需要访问 nextVisit[i] 所指定的房间，其中 0 <= nextVisit[i] <= i 。
     * 如果算上本次访问，访问 i 号房间的次数为 偶数 ，那么 第二天 需要访问 (i + 1) mod n 号房间。
     * 输入：nextVisit = [0,0]
     * 输出：2
     * 解释：
     * - 第 0 天，你访问房间 0 。访问 0 号房间的总次数为 1 ，次数为奇数。
     * 下一天你需要访问房间的编号是 nextVisit[0] = 0
     * - 第 1 天，你访问房间 0 。访问 0 号房间的总次数为 2 ，次数为偶数。
     * 下一天你需要访问房间的编号是 (0 + 1) mod 2 = 1
     * - 第 2 天，你访问房间 1 。这是你第一次完成访问所有房间的那天。
     */

    public int firstDayBeenInAllRooms(int[] nextVisit) {
        int mod = 1000000000 + 7;
        int res = 1;
        // room - count
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, 1);
        int len = nextVisit.length;
        while (map.size() != len) {
            int next = nextVisit[0];
            int count = map.get(next);
            if (count % 2 == 1) {
                // nextVisit[i]
                next = nextVisit[next];
            } else {
                // (visited+1)%len
                next = (next + 1) % len;
            }
            map.put(next, map.getOrDefault(next, 0) + 1);
            res++;
        }
        return res;
    }

    public int firstDayBeenInAllRooms0(int[] nextVisit) {
        int mod = 1000000007;
        int n = nextVisit.length;
        int[] dp = new int[n];
        int[] sum = new int[n];
        for (int i = 1; i < n; i++) {
            dp[i] = (sum[i - 1] - sum[nextVisit[i - 1]] + mod + 2) % mod;
            sum[i] = (sum[i - 1] + dp[i]) % mod;
        }
        return sum[n - 1];
    }
}
