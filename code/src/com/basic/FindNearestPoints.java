package com.basic;

/**
 * @Author: wenhongliang
 */
public class FindNearestPoints {
    public static void main(String[] args) {
        double[][] points = new double[][]{
                {-1, 3}, {-1, -1}, {1, 1}, {2, 0.5},
                {2, -1}, {3, 3}, {4, 2}, {4, -0.5}
        };

        int p1 = 0, p2 = 1;
        double min = shortDistance(points[p1][0], points[p1][1], points[p2][0], points[p2][1]);
        for (int i = 0; i < points.length; i++) {
            for (int j = i + 1; j < points.length; j++) {
                double distance = shortDistance(points[i][0], points[i][1], points[j][0], points[j][1]);
                if (distance < min) {
                    p1 = i;
                    p2 = j;
                    min = distance;
                }
            }
        }

        System.out.println("The closest two point are "
                + "(" + points[p1][0] + "," + points[p1][1] + ") "
                + "(" + points[p2][0] + "," + points[p2][1] + ")");
    }

    public static double shortDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
