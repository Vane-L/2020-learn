package com.sort;

/**
 * @Author: wenhongliang
 */
public class QuickSort {
    public static void main(String[] args) {
        int[] list = new int[]{2, 3, 2, 5, 6, 1, -2, 3, 14, 12};
        quickSort(list, 0, list.length - 1);
        for (int x : list) {
            System.out.print(x + " ");
        }
    }

    public static void quickSort(int[] list, int first, int last) {
        if (first < last) {
            int pivotIdx = partition(list, first, last);
            quickSort(list, first, pivotIdx);
            quickSort(list, pivotIdx + 1, last);
        }
    }

    public static int partition(int[] list, int first, int last) {
        int pivot = list[first];
        int low = first + 1, high = last;
        while (low < high) {
            while (low <= high && list[low] <= pivot) {
                low++;
            }
            while (low <= high && list[high] > pivot) {
                high--;
            }
            if (low < high) {
                int tmp = list[low];
                list[low] = list[high];
                list[high] = tmp;
            }
        }

        while (first < high && pivot <= list[high]) {
            high--;
        }

        if (pivot > list[high]) {
            list[first] = list[high];
            list[high] = pivot;
            return high;
        } else {
            return first;
        }
    }
}
