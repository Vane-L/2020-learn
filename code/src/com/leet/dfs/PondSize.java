package com.leet.dfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: wenhongliang
 * https://leetcode-cn.com/problems/pond-sizes-lcci/
 */
public class PondSize {
    /**
     * 输入：
     * [
     * [0,2,1,0],
     * [0,1,0,1],
     * [1,1,0,1],
     * [0,1,0,1]
     * ]
     * 输出： [1,2,4]
     */
    public int[] pondSizes(int[][] land) {
        if (land == null || land.length == 0) {
            return new int[0];
        }
        List<Integer> resList = new ArrayList<>();
        for (int i = 0; i < land.length; i++) {
            for (int j = 0; j < land[0].length; j++) {
                if (land[i][j] == 0) {
                    resList.add(dfs(land, i, j));
                }
            }
        }
        Collections.sort(resList);
        int[] res = new int[resList.size()];
        for (int i = 0; i < resList.size(); i++) {
            res[i] = resList.get(i);
        }
        return res;
    }

    private int dfs(int[][] land, int i, int j) {
        if (i < 0 || i >= land.length || j < 0 || j >= land[0].length || land[i][j] != 0) {
            return 0;
        }
        land[i][j] = -1;
        return dfs(land, i + 1, j) + dfs(land, i - 1, j) +
                dfs(land, i, j + 1) + dfs(land, i, j - 1) +
                dfs(land, i + 1, j - 1) + dfs(land, i - 1, j + 1) +
                dfs(land, i + 1, j + 1) + dfs(land, i - 1, j - 1) + 1;
    }
}
