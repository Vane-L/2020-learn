package com.book;

/**
 * @Author: wenhongliang
 */
public class ArrayProblem {
    public static void main(String[] args) {
        ArrayProblem ap = new ArrayProblem();
        int[][] matrix = new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        };
        ap.orderPrint(matrix);
        ap.rotate(matrix);
    }

    public void orderPrint(int[][] matrix) {
        int tR = 0, tC = 0;
        int dR = matrix.length - 1, dC = matrix[0].length - 1;
        while (tR <= dR && tC <= dC) {
            printEdge(matrix, tR++, tC++, dR--, dC--);
        }
    }

    private void printEdge(int[][] m, int tR, int tC, int dR, int dC) {
        if (tR == dR) {
            for (int i = tC; i <= dC; i++) {
                System.out.print(m[tR][i]);
            }
        } else if (tC == dC) {
            for (int i = tR; i <= dR; i++) {
                System.out.print(m[i][tC]);
            }
        } else {
            int curR = tR;
            int curC = tC;
            while (curC != dC) {
                System.out.print(m[curR][curC++] + " ");
            }
            while (curR != dR) {
                System.out.print(m[curR++][curC] + " ");
            }
            while (curC != tC) {
                System.out.print(m[curR][curC--] + " ");
            }
            while (curR != tR) {
                System.out.print(m[curR--][curC] + " ");
            }
        }
    }

    public void rotate(int[][] matrix) {
        int tR = 0, tC = 0;
        int dR = matrix.length - 1, dC = matrix[0].length - 1;
        while (tR < dR) {
            printRotate(matrix, tR++, tC++, dR--, dC--);
        }
    }

    private void printRotate(int[][] m, int tR, int tC, int dR, int dC) {
        int times = dC - tC;
        for (int i = 0; i != times; i++) {
            int tmp = m[tR][tC + i];
            m[tR][tC + i] = m[dR-i][tC];
            m[dR-i][tC] = m[dR][dC - i];
            m[dR][dC - i] = m[tR + i][dC];
            m[tR + i][dC] = tmp;
        }
    }
}
