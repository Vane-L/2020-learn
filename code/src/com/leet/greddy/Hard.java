package com.leet.greddy;

/**
 * @Author: wenhongliang
 */
public class Hard {
    public static void main(String[] args) {
        Hard hard = new Hard();
        System.out.println(hard.maxProfit(new int[]{3, 3, 5, 0, 0, 3, 1, 4}));
        System.out.println(hard.maxProfit2(new int[]{3, 3, 5, 0, 0, 3, 1, 4}));
    }

    //输入: [3,3,5,0,0,3,1,4]
    //输出: 6
    //解释: 在第 4 天（股票价格 = 0）的时候买入，在第 6 天（股票价格 = 3）的时候卖出，这笔交易所能获得利润 = 3-0 = 3 。
    //     随后，在第 7 天（股票价格 = 1）的时候买入，在第 8 天 （股票价格 = 4）的时候卖出，这笔交易所能获得利润 = 4-1 = 3 。
    public int maxProfit(int[] prices) {
        if (prices == null || prices.length == 0) {
            return 0;
        }
        int len = prices.length;
        int min = prices[0], max = prices[len - 1];
        int maxProfit1 = 0, maxProfit2 = 0;
        int[] profit1 = new int[len];
        int[] profit2 = new int[len];
        for (int i = 0; i < len; i++) {
            if (prices[i] <= min) {
                min = prices[i];
            } else {
                maxProfit1 = Math.max(maxProfit1, prices[i] - min);
            }
            profit1[i] = maxProfit1;
            if (prices[len - i - 1] >= max) {
                max = prices[len - i - 1];
            } else {
                maxProfit2 = Math.max(maxProfit2, max - prices[len - i - 1]);
            }
            profit2[len - i - 1] = maxProfit2;
        }
        int res = Integer.MIN_VALUE;
        for (int i = 0; i < len; i++) {
            res = Math.max(res, profit1[i] + profit2[i]);
        }
        return res;
    }

    public int maxProfit2(int[] prices) {
        int minPrice1 = Integer.MAX_VALUE;
        int maxProfit1 = 0;
        int maxRemaining = Integer.MIN_VALUE;
        int maxProfit2 = 0;
        for (int price : prices) {
            // 1.第一次最小购买价格
            minPrice1 = Math.min(minPrice1, price);
            // 2.第一次卖出的最大利润
            maxProfit1 = Math.max(maxProfit1, price - minPrice1);
            // 3.第二次购买后的剩余净利润
            maxRemaining = Math.max(maxRemaining, maxProfit1 - price);
            // 4.第二次卖出后，总共获得的最大利润（第3步的净利润 + 第4步卖出的股票钱）
            maxProfit2 = Math.max(maxProfit2, price + maxRemaining);
        }
        return maxProfit2;
    }
}
