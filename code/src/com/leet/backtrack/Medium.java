package com.leet.backtrack;

import java.util.ArrayList;
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

}
