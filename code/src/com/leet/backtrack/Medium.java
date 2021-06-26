package com.leet.backtrack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class Medium {
    private String[] keyboard = {"abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};

    public List<String> letterCombinations(String digits) {
        List<String> resList = new ArrayList<>();
        if (digits == null || digits.length() == 0) {
            return resList;
        }
        dfs(digits, 0, "", resList);
        return resList;
    }

    private void dfs(String digits, int idx, String cur, List<String> resList) {
        if (idx == digits.length()) {
            resList.add(cur);
            return;
        }
        String str = keyboard[digits.charAt(idx) - '2'];
        for (int i = 0; i < str.length(); i++) {
            dfs(digits, idx + 1, cur + str.charAt(i), resList);
        }
    }

    public List<String> generateParenthesis(int n) {
        List<String> res = new ArrayList<>();
        back(res, "", 0, 0, n);
        return res;
    }

    private void back(List<String> res, String s, int left, int right, int n) {
        if (s.length() == n * 2) {
            res.add(s);
            return;
        }
        if (left < n) {
            back(res, s + "(", left + 1, right, n);
        }
        if (right < left) {
            back(res, s + ")", left, right + 1, n);
        }
    }

    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        backNums(res, new ArrayList<>(), nums, used);
        return res;
    }

    private void backNums(List<List<Integer>> res, ArrayList<Integer> integers, int[] nums, boolean[] used) {
        if (integers.size() == nums.length) {
            res.add(new ArrayList<>(integers));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (!used[i]) {
                used[i] = true;
                integers.add(nums[i]);
                backNums(res, integers, nums, used);
                integers.remove(integers.size() - 1);
                used[i] = false;
            }
        }
    }


    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrack(res, nums, new ArrayList<>(), 0);
        return res;
    }

    private void backtrack(List<List<Integer>> res, int[] nums, ArrayList<Integer> tmp, int idx) {
        res.add(new ArrayList<>(tmp));
        for (int i = idx; i < nums.length; i++) {
            tmp.add(nums[i]);
            backtrack(res, nums, tmp, i + 1);
            tmp.remove(tmp.size() - 1);
        }
    }

    public boolean exist(char[][] board, String word) {
        if (board == null || board.length == 0) {
            return false;
        }
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (backWord(board, i, j, word, 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean backWord(char[][] board, int i, int j, String word, int idx) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || board[i][j] != word.charAt(idx)) {
            return false;
        }
        if (idx == word.length() - 1) {
            return true;
        }
        char tmp = board[i][j];
        board[i][j] = '#';
        boolean flag = backWord(board, i - 1, j, word, idx + 1) ||
                backWord(board, i + 1, j, word, idx + 1) ||
                backWord(board, i, j - 1, word, idx + 1) ||
                backWord(board, i, j + 1, word, idx + 1);
        board[i][j] = tmp;
        return flag;
    }

    public void solve(char[][] board) {
        if (board == null || board.length == 0) {
            return;
        }
        int m = board.length, n = board[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                boolean isEdge = i == 0 || j == 0 || i == m - 1 || j == n - 1;
                if (isEdge && board[i][j] == 'O') {
                    backX(board, i, j);
                }
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == 'O') {
                    board[i][j] = 'X';
                }
                if (board[i][j] == '#') {
                    board[i][j] = 'O';
                }
            }
        }

    }

    private void backX(char[][] board, int i, int j) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || board[i][j] == 'X' || board[i][j] == '#') {
            return;
        }
        board[i][j] = '#';
        backX(board, i - 1, j);
        backX(board, i + 1, j);
        backX(board, i, j - 1);
        backX(board, i, j + 1);
    }


    public int findCircleNum(int[][] isConnected) {
        int provinces = isConnected.length;
        // 记录城市是否访问过
        boolean[] visited = new boolean[provinces];
        int circles = 0;
        for (int i = 0; i < provinces; i++) {
            // 从未被访问过的城市开始DFS
            if (!visited[i]) {
                dfs(isConnected, visited, provinces, i);
                circles++;
            }
        }
        return circles;
    }

    public void dfs(int[][] isConnected, boolean[] visited, int provinces, int i) {
        for (int j = 0; j < provinces; j++) {
            if (isConnected[i][j] == 1 && !visited[j]) {
                visited[j] = true;
                dfs(isConnected, visited, provinces, j);
            }
        }
    }

    public int longestIncreasingPath(int[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return 0;
        }
        int m = matrix.length, n = matrix[0].length;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                max = Math.max(max, backIncr(matrix, i, j, -1, 0));
            }
        }
        return max;
    }

    private int backIncr(int[][] matrix, int i, int j, int pre, int cur) {
        if (i < 0 || i >= matrix.length || j < 0 || j >= matrix[0].length || matrix[i][j] < 0 || matrix[i][j] <= pre) {
            return cur;
        }
        cur++;
        int tmp = matrix[i][j];
        matrix[i][j] = -1;
        int left = backIncr(matrix, i, j - 1, tmp, cur);
        int right = backIncr(matrix, i, j + 1, tmp, cur);
        int down = backIncr(matrix, i + 1, j, tmp, cur);
        int up = backIncr(matrix, i - 1, j, tmp, cur);
        matrix[i][j] = tmp;
        return Math.max(left, Math.max(right, Math.max(down, up)));
    }

    public int longestIncreasingPath2(int[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return 0;
        }
        int m = matrix.length, n = matrix[0].length;
        int[][] used = new int[m][n];
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                max = Math.max(max, backIncr2(matrix, i, j, used));
            }
        }
        return max;
    }

    private int backIncr2(int[][] matrix, int i, int j, int[][] used) {
        if (i < 0 || i >= matrix.length || j < 0 || j >= matrix[0].length) {
            return 0;
        }
        if (used[i][j] > 0) {
            return used[i][j];
        }
        int num = 0;
        if (i - 1 >= 0 && matrix[i - 1][j] < matrix[i][j]) {
            num = Math.max(num, backIncr2(matrix, i - 1, j, used));
        }
        if (i + 1 < matrix.length && matrix[i + 1][j] < matrix[i][j]) {
            num = Math.max(num, backIncr2(matrix, i + 1, j, used));
        }
        if (j - 1 >= 0 && matrix[i][j - 1] < matrix[i][j]) {
            num = Math.max(num, backIncr2(matrix, i, j - 1, used));
        }
        if (j + 1 < matrix[0].length && matrix[i][j + 1] < matrix[i][j]) {
            num = Math.max(num, backIncr2(matrix, i, j + 1, used));
        }
        used[i][j] = num + 1;
        return num + 1;
    }

    public List<Integer> countSmaller(int[] nums) {
        List<Integer> list = new ArrayList<>();
        if (nums == null || nums.length == 0) {
            return list;
        }
        for (int i = 0; i < nums.length; i++) {
            int count = 0;
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[j] < nums[i]) {
                    count++;
                }
            }
            list.add(count);
        }
        return list;
    }

    public List<Integer> countSmaller2(int[] nums) {
        if (nums == null || nums.length == 0) return new LinkedList<>();
        LinkedList<Integer> res = new LinkedList<>();
        int len = nums.length;
        //反向插入排序
        for (int i = len - 2; i >= 0; i--) {
            int j = i + 1, tmp = nums[i];
            while (j < len && nums[j] >= tmp) {
                nums[j - 1] = nums[j];
                j++;
            }
            nums[j - 1] = tmp;
            //len - j就表示计数个数
            res.addFirst(len - j);
        }
        //添加最后一个数
        res.add(0);
        return res;
    }

}
