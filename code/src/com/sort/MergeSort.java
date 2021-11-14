package com.sort;

/**
 * @Author: wenhongliang
 */
public class MergeSort {
    public static void main(String[] args) {
        int[] list = new int[]{2, 3, 2, 5, 6, 1, -2, 3, 14, 12};
        mergeSort(list);
        for (int x : list) {
            System.out.print(x + " ");
        }
    }

    public static void mergeSort(int[] list) {
        if (list.length > 1) {
            int[] firstHalf = new int[list.length / 2];
            System.arraycopy(list, 0, firstHalf, 0, list.length / 2);
            mergeSort(firstHalf);
            int[] secondHalf = new int[list.length - list.length / 2];
            System.arraycopy(list, list.length / 2, secondHalf, 0, list.length - list.length / 2);
            mergeSort(secondHalf);
            int[] tmp = merge(firstHalf, secondHalf);
            System.arraycopy(tmp, 0, list, 0, tmp.length);
        }
    }

    public static int[] merge(int[] list1, int[] list2) {
        int[] tmp = new int[list1.length + list2.length];
        int i = 0, j = 0, idx = 0;
        while (i < list1.length && j < list2.length) {
            if (list1[i] < list2[j]) {
                tmp[idx++] = list1[i++];
            } else {
                tmp[idx++] = list2[j++];
            }
        }
        while (i < list1.length) {
            tmp[idx++] = list1[i++];
        }
        while (j < list2.length) {
            tmp[idx++] = list2[j++];
        }
        return tmp;
    }
}
