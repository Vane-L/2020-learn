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
        ap.rotate(new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}
        });
        System.out.println("----------");
        ap.printZigZag(new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
        });
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
            m[tR][tC + i] = m[dR - i][tC];
            m[dR - i][tC] = m[dR][dC - i];
            m[dR][dC - i] = m[tR + i][dC];
            m[tR + i][dC] = tmp;
        }
    }

    public void printZigZag(int[][] matrix) {
        int tR = 0, tC = 0, dR = 0, dC = 0;
        int r = matrix.length - 1, c = matrix[0].length - 1;
        boolean fromUp = false;
        while (tR != r + 1) {
            printLevel(matrix, tR, tC, dR, dC, fromUp);
            tR = tC == c ? tR + 1 : tR;
            tC = tC == c ? tC : tC + 1;
            dC = dR == r ? dC + 1 : dC;
            dR = dR == r ? dR : dR + 1;
            fromUp = !fromUp;
        }
        System.out.println();
    }

    private void printLevel(int[][] matrix, int tR, int tC, int dR, int dC, boolean fromUp) {
        if (fromUp) {
            while (tR != dR + 1) {
                System.out.print(matrix[tR++][tC--] + " ");
            }
        } else {
            while (dR != tR - 1) {
                System.out.print(matrix[dR--][dC++] + " ");
            }
        }
    }
}
