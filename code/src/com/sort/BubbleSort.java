package com.sort;

/**
 * @Author: wenhongliang
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] list = new int[]{2, 3, 2, 5, 6, 1, -2, 3, 14, 12};
        bubbleSort(list);
        for (int x : list) {
            System.out.print(x + " ");
        }
    }

    public static void bubbleSort(int[] list) {
        boolean needNextPass = true;
        for (int i = 1; i < list.length && needNextPass; i++) {
            needNextPass = false;
            for (int j = 0; j < list.length - i; j++) {
                if (list[j] > list[j + 1]) {
                    int tmp = list[j];
                    list[j] = list[j + 1];
                    list[j + 1] = tmp;
                    needNextPass = true;
                }
            }
        }
    }
}
