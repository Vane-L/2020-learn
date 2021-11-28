package com.leet.week;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @Author: wenhongliang
 * @Date: 2021/11/28
 * @Desciption: 又是一场水题，还得打好基础啊...
 */
public class Week15 {

    /**
     * Medium 5939. 半径为 k 的子数组平均值
     * 给你一个下标从 0 开始的数组 nums ，数组中有 n 个整数，另给你一个整数 k 。
     * 半径为 k 的子数组平均值 是指：nums 中一个以下标 i 为 中心 且 半径 为 k 的子数组中所有元素的平均值，即下标在 i - k 和 i + k 范围（含 i - k 和 i + k）内所有元素的平均值。如果在下标 i 前或后不足 k 个元素，那么 半径为 k 的子数组平均值 是 -1 。
     * 构建并返回一个长度为 n 的数组 avgs ，其中 avgs[i] 是以下标 i 为中心的子数组的 半径为 k 的子数组平均值 。
     * x 个元素的 平均值 是 x 个元素相加之和除以 x ，此时使用截断式 整数除法 ，即需要去掉结果的小数部分。
     * todo 复盘：卡在最后一个用例，需要用long在计算总和
     */
    public int[] getAverages(int[] nums, int k) {
        if (k == 0) return nums;
        int len = nums.length;
        int[] res = new int[len];
        for (int i = 0; i < len; i++) {
            res[i] = -1;
        }
        if (k >= len) return res;
        double[] pre = new double[len + 1];
        for (int i = 1; i <= len; i++) {
            pre[i] = pre[i - 1] + nums[i - 1];
        }
        for (int i = k; i < len - k; i++) {
            res[i] = (int) ((pre[i + k + 1] - pre[i - k]) / (2 * k + 1));
        }
        return res;
    }

    public int[] getAverages0(int[] nums, int k) {
        int[] result = new int[nums.length];
        long sum = 0;
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i] - (i < 2 * k + 1 ? 0 : nums[i - 2 * k - 1]);
            result[(i - k + nums.length) % nums.length] = i < 2 * k ? -1 : (int) (sum / (2 * k + 1));
        }
        return result;
    }

    /**
     * Medium 5940. 从数组中移除最大值和最小值
     * 给你一个下标从 0 开始的数组 nums ，数组由若干 互不相同 的整数组成。
     * nums 中有一个值最小的元素和一个值最大的元素。分别称为 最小值 和 最大值 。你的目标是从数组中移除这两个元素。
     * 一次 删除 操作定义为从数组的 前面 移除一个元素或从数组的 后面 移除一个元素。
     * 返回将数组中最小值和最大值 都 移除需要的最小删除次数。
     * todo 复盘：可以不需要min和max记录最大最小值，因为已经记录了最大最小值的下标
     */
    public int minimumDeletions(int[] nums) {
        if (nums == null) return 0;
        if (nums.length == 1) return 1;
        int len = nums.length;
        int minIdx = 0, maxIdx = 0;
        int min = nums[0], max = nums[0];
        for (int i = 0; i < len; i++) {
            if (min < nums[i]) {
                min = nums[i];
                minIdx = i;
            }
            if (max > nums[i]) {
                max = nums[i];
                maxIdx = i;
            }
        }
        int head = Math.max(minIdx + 1, maxIdx + 1);
        int tail = Math.max(len - minIdx, len - maxIdx);
        minIdx = Math.min(minIdx + 1, Math.abs(len - minIdx));
        maxIdx = Math.min(maxIdx + 1, Math.abs(len - maxIdx));
        int both = minIdx + maxIdx;
        return Math.min(both, Math.min(head, tail));
    }

    public int minimumDeletionsAfter(int[] nums) {
        if (nums == null) return 0;
        if (nums.length == 1) return 1;
        int len = nums.length;
        int minIdx = 0, maxIdx = 0;
        for (int i = 0; i < len; i++) {
            minIdx = nums[minIdx] < nums[i] ? i : minIdx;
            maxIdx = nums[maxIdx] > nums[i] ? i : maxIdx;
        }
        int head = Math.max(minIdx, maxIdx) + 1;
        int tail = Math.max(len - minIdx, len - maxIdx);
        minIdx = Math.min(minIdx + 1, Math.abs(len - minIdx));
        maxIdx = Math.min(maxIdx + 1, Math.abs(len - maxIdx));
        int both = minIdx + maxIdx;
        return Math.min(both, Math.min(head, tail));
    }

    public int minimumDeletions0(int[] nums) {
        int min = 0, max = 0;
        for (int i = 1; i < nums.length; i++) {
            min = nums[i] < nums[min] ? i : min;
            max = nums[i] > nums[max] ? i : max;
        }
        return Math.min(Math.max(min, max) + 1, nums.length - Math.max(Math.min(min, max), Math.abs(min - max) - 1));
    }

    /**
     * Hard 5941. 找出知晓秘密的所有专家
     * 给你一个整数 n ，表示有 n 个专家从 0 到 n - 1 编号。另外给你一个下标从 0 开始的二维整数数组 meetings ，
     * 其中 meetings[i] = [xi, yi, timei] 表示专家 xi 和专家 yi 在时间 timei 要开一场会。
     * 一个专家可以同时参加 多场会议 。最后，给你一个整数 firstPerson 。
     * 专家 0 有一个 秘密 ，最初，他在时间 0 将这个秘密分享给了专家 firstPerson 。接着，这个秘密会在每次有知晓这个秘密的专家参加会议时进行传播。
     * 更正式的表达是，每次会议，如果专家 xi 在时间 timei 时知晓这个秘密，那么他将会与专家 yi 分享这个秘密，反之亦然。
     * 秘密共享是 瞬时发生 的。也就是说，在同一时间，一个专家不光可以接收到秘密，还能在其他会议上与其他专家分享。
     * 在所有会议都结束之后，返回所有知晓这个秘密的专家列表。你可以按 任何顺序 返回答案。
     * todo 复盘：又是一个并查集没做出来...
     */
    public List<Integer> findAllPeople(int n, int[][] meetings, int firstPerson) {
        ArrayList<int[]>[] lists = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            lists[i] = new ArrayList<>();
        }
        for (int[] meeting : meetings) {
            lists[meeting[0]].add(new int[]{meeting[1], meeting[2]});
            lists[meeting[1]].add(new int[]{meeting[0], meeting[2]});
        }
        ArrayList<Integer> list = new ArrayList<>();
        PriorityQueue<int[]> queue = new PriorityQueue<>((o, p) -> o[1] - p[1]);
        queue.add(new int[]{0, 0});
        queue.add(new int[]{firstPerson, 0});
        while (!queue.isEmpty()) {
            int[] poll = queue.poll();
            if (lists[poll[0]] != null) {
                list.add(poll[0]);
                for (int[] meeting : lists[poll[0]]) {
                    if (meeting[1] >= poll[1]) {
                        queue.add(meeting);
                    }
                }
                lists[poll[0]] = null;
            }
        }
        return list;
    }
}
