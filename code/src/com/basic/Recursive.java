package com.basic;

/**
 * @Author: wenhongliang
 */
public class Recursive {

    public static void sort(double[] list) {
        recursiveSort(list, 0, list.length - 1);
    }

    public static void recursiveSort(double[] list, int low, int high) {
        if (low < high) {
            int idxMin = low;
            double min = list[low];
            for (int i = low + 1; i <= high; i++) {
                if (list[i] < min) {
                    idxMin = i;
                    min = list[i];
                }
            }

            list[idxMin] = list[low];
            list[low] = min;

            recursiveSort(list, low + 1, high);
        }
    }

    public static int recurseSearch(int[] list, int key) {
        int low = 0, high = list.length - 1;
        return recurseBinarySearch(list, low, high, key);
    }

    public static int recurseBinarySearch(int[] list, int low, int high, int key) {
        if (low > high) {
            return -1;
        }
        int mid = (low + high) / 2;
        if (key < list[mid]) {
            return recurseBinarySearch(list, low, mid - 1, key);
        }
        if (key > list[mid]) {
            return recurseBinarySearch(list, mid + 1, high, key);
        }
        return mid;
    }
}
