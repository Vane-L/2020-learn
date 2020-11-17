package com.book;

/**
 * @Author: wenhongliang
 */
public class ArrayProblem {
    public static void main(String[] args) {
        ArrayProblem ap = new ArrayProblem();
        ap.orderPrint(new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
    }

    public void orderPrint(int[][] matrix) {
        int tR = 0, tC = 0;
        int dR = matrix.length - 1, dC = matrix[0].length - 1;
        while (tR <= dR && tC <= dC) {
            printEdge(matrix, tR++, tC++, dR--, dC--);
        }
    }

    private void printEdge(int[][] matrix, int tR, int tC, int dR, int dC) {
        if (tR == dR) {
            for (int i = tC; i <= dC; i++) {
                System.out.print(matrix[tR][i]);
            }
        } else if (tC == dC) {
            for (int i = tR; i <= dR; i++) {
                System.out.print(matrix[i][tC]);
            }
        } else {
            int curR = tR;
            int curC = tC;
            while (curC != dC) {
                System.out.print(matrix[curR][curC++] + " ");
            }
            while (curR != dR) {
                System.out.print(matrix[curR++][curC] + " ");
            }
            while (curC != tC) {
                System.out.print(matrix[curR][curC--] + " ");
            }
            while (curR != tR) {
                System.out.print(matrix[curR--][curC] + " ");
            }
        }
    }
}
