package com;

/**
 * @Author: wenhongliang
 */
public class KellyUtil {
    public static double kelly(double pwin, double odds) {
        return (odds * pwin + pwin - 1) / odds;
    }

    public static double kelly(double pwin, double odds, double lossRate) {
        return (odds * pwin + pwin - 1) / (odds * lossRate);
    }


    public static double buffet(double pwin) {
        return 2 * pwin - 1;
    }

    public static void main(String[] args) {
        double odds = kelly(0.5, 3.0);
        System.out.println("仓位 " + 100 * odds + "%");
    }
}
