package com.leet.week;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class Week9 {

    /**
     * Easy 暴力
     * 句子是由若干 token 组成的一个列表，token 间用 单个 空格分隔，句子没有前导或尾随空格。每个 token 要么是一个由数字 0-9 组成的不含前导零的 正整数 ，要么是一个由小写英文字母组成的 单词 。
     * 示例，"a puppy has 2 eyes 4 legs" 是一个由 7 个 token 组成的句子："2" 和 "4" 是数字，其他像 "puppy" 这样的 tokens 属于单词。
     * 给你一个表示句子的字符串 s ，你需要检查 s 中的 全部 数字是否从左到右严格递增（即，除了最后一个数字，s 中的 每个 数字都严格小于它 右侧 的数字）。
     * 如果满足题目要求，返回 true ，否则，返回 false 。
     */
    public boolean areNumbersAscending(String s) {
        char[] ch = s.toCharArray();
        int pre = 0;
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] >= '0' && ch[i] <= '9') {
                int cur = 0;
                while (i < ch.length && ch[i] >= '0' && ch[i] <= '9') {
                    cur = cur * 10 + ch[i] - '0';
                    i++;
                }
                if (cur <= pre) {
                    return false;
                } else {
                    pre = cur;
                }
            }
        }
        return true;
    }

    /**
     * Medium 模拟
     * 你的任务是为一个很受欢迎的银行设计一款程序，以自动化执行所有传入的交易（转账，存款和取款）。银行共有 n 个账户，编号从 1 到 n 。每个账号的初始余额存储在一个下标从 0 开始的整数数组 balance 中，其中第 (i + 1) 个账户的初始余额是 balance[i] 。
     * 请你执行所有 有效的 交易。如果满足下面全部条件，则交易 有效 ：
     * 指定的账户数量在 1 和 n 之间，且
     * 取款或者转账需要的钱的总数 小于或者等于 账户余额。
     * 实现 Bank 类：
     * Bank(long[] balance) 使用下标从 0 开始的整数数组 balance 初始化该对象。
     * boolean transfer(int account1, int account2, long money) 从编号为 account1 的账户向编号为 account2 的账户转帐 money 美元。如果交易成功，返回 true ，否则，返回 false 。
     * boolean deposit(int account, long money) 向编号为 account 的账户存款 money 美元。如果交易成功，返回 true ；否则，返回 false 。
     * boolean withdraw(int account, long money) 从编号为 account 的账户取款 money 美元。如果交易成功，返回 true ；否则，返回 false 。
     */

    class Bank {
        private long[] num;
        private int count;

        public Bank(long[] balance) {
            this.count = balance.length;
            this.num = new long[balance.length];
            for (int i = 0; i < balance.length; i++) {
                this.num[i] = balance[i];
            }
        }

        public boolean transfer(int account1, int account2, long money) {
            if (account1 - 1 > count || account2 - 1 > count) {
                return false;
            }
            if (num[account1 - 1] < money) {
                return false;
            }
            num[account1 - 1] -= money;
            num[account2 - 1] += money;
            return true;
        }

        public boolean deposit(int account, long money) {
            if (account - 1 > count) {
                return false;
            }
            num[account - 1] += money;
            return true;
        }

        public boolean withdraw(int account, long money) {
            if (account - 1 > count || num[account - 1] < money) {
                return false;
            }
            num[account - 1] -= money;
            return true;
        }
    }


    /**
     * Medium 回溯
     * 统计按位或能得到最大值的子集数目
     * 给你一个整数数组 nums ，请你找出 nums 子集 按位或 可能得到的 最大值 ，并返回按位或能得到最大值的 不同非空子集的数目 。
     * 如果数组 a 可以由数组 b 删除一些元素（或不删除）得到，则认为数组 a 是数组 b 的一个 子集 。如果选中的元素下标位置不一样，则认为两个子集 不同 。
     * 对数组 a 执行 按位或 ，结果等于 a[0] OR a[1] OR ... OR a[a.length - 1]（下标从 0 开始）。
     */
    int res = 0;

    public int countMaxOrSubsets(int[] nums) {
        int max = 0;
        for (int i = 0; i < nums.length; i++) {
            max |= nums[i];
        }
        List<Integer> list = new ArrayList<>();
        back(nums, 0, max, list);
        return res;
    }

    public void back(int[] nums, int idx, int max, List<Integer> list) {
        int sum = 0;
        for (int x : list) {
            sum |= x;
        }
        if (sum == max) res++;
        for (int i = idx; i < nums.length; i++) {
            list.add(nums[i]);
            back(nums, i + 1, max, list);
            list.remove(list.size() - 1);
        }
    }
}
