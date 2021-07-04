package com.leet.backtrack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @Author: wenhongliang
 */
public class High {
    public List<List<String>> partition(String s) {
        List<List<String>> res = new ArrayList<>();
        backPartition(res, new ArrayList<>(), s, 0, s.length());
        return res;
    }

    private void backPartition(List<List<String>> res, ArrayList<String> list, String s, int start, int len) {
        if (start == len) {
            res.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < len; i++) {
            if (!isPar(s, start, i)) {
                continue;
            }
            list.add(s.substring(start, i + 1));
            backPartition(res, list, s, i + 1, len);
            list.remove(list.size() - 1);
        }
    }

    private boolean isPar(String s, int i, int j) {
        while (i < j) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }

    public List<String> findWords(char[][] board, String[] words) {
        List<String> res = new ArrayList<>();
        int m = board.length, n = board[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < words.length; k++) {
                    if (backFind(board, i, j, words[k], 0))
                        res.add(words[k]);
                }
            }
        }
        return res.stream().distinct().collect(Collectors.toList());
    }

    private boolean backFind(char[][] board, int i, int j, String word, int idx) {
        if (idx == word.length()) {
            return true;
        }
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || board[i][j] == '#') {
            return false;
        }
        char ch = board[i][j];
        if (board[i][j] == word.charAt(idx)) {
            board[i][j] = '.';
            boolean res = backFind(board, i + 1, j, word, idx + 1) ||
                    backFind(board, i - 1, j, word, idx + 1) ||
                    backFind(board, i, j - 1, word, idx + 1) ||
                    backFind(board, i, j + 1, word, idx + 1);
            board[i][j] = ch;
            return res;
        }
        return false;
    }


    private int len;
    private char[] charArray;
    private Set<String> validExpressions = new HashSet<>();

    public List<String> removeInvalidParentheses(String s) {
        this.len = s.length();
        this.charArray = s.toCharArray();

        // 第 1 步：遍历一次，计算多余的左右括号
        int leftRemove = 0;
        int rightRemove = 0;
        for (int i = 0; i < len; i++) {
            if (charArray[i] == '(') {
                leftRemove++;
            } else if (charArray[i] == ')') {
                // 遇到右括号的时候，须要根据已经存在的左括号数量决定
                if (leftRemove == 0) {
                    rightRemove++;
                }
                if (leftRemove > 0) {
                    // 关键：一个右括号出现可以抵销之前遇到的左括号
                    leftRemove--;
                }
            }
        }

        // 第 2 步：回溯算法，尝试每一种可能的删除操作
        backRem(0, 0, 0, leftRemove, rightRemove, new StringBuilder());
        return new ArrayList<>(this.validExpressions);
    }

    /**
     * @param index       当前遍历到的下标
     * @param leftCount   已经遍历到的左括号的个数
     * @param rightCount  已经遍历到的右括号的个数
     * @param leftRemove  最少应该删除的左括号的个数
     * @param rightRemove 最少应该删除的右括号的个数
     * @param path        一个可能的结果
     */
    private void backRem(int index, int leftCount, int rightCount, int leftRemove, int rightRemove, StringBuilder path) {
        if (index == len) {
            if (leftRemove == 0 && rightRemove == 0) {
                validExpressions.add(path.toString());
            }
            return;
        }

        char character = charArray[index];
        // 可能的操作 1：删除当前遍历到的字符
        if (character == '(' && leftRemove > 0) {
            // 由于 leftRemove > 0，并且当前遇到的是左括号，因此可以尝试删除当前遇到的左括号
            backRem(index + 1, leftCount, rightCount, leftRemove - 1, rightRemove, path);
        }
        if (character == ')' && rightRemove > 0) {
            // 由于 rightRemove > 0，并且当前遇到的是右括号，因此可以尝试删除当前遇到的右括号
            backRem(index + 1, leftCount, rightCount, leftRemove, rightRemove - 1, path);
        }

        // 可能的操作 2：保留当前遍历到的字符
        path.append(character);
        if (character != '(' && character != ')') {
            // 如果不是括号，继续深度优先遍历
            backRem(index + 1, leftCount, rightCount, leftRemove, rightRemove, path);
        } else if (character == '(') {
            // 考虑左括号
            backRem(index + 1, leftCount + 1, rightCount, leftRemove, rightRemove, path);
        } else if (rightCount < leftCount) {
            // 考虑右括号
            backRem(index + 1, leftCount, rightCount + 1, leftRemove, rightRemove, path);
        }
        path.deleteCharAt(path.length() - 1);
    }


    // 通配符匹配
    public boolean isMatch(String s, String p) {
        int m = s.length();
        int n = p.length();
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;
        for (int i = 1; i <= n; i++) {
            if (p.charAt(i - 1) == '*') {
                dp[0][i] = true;
            } else {
                break;
            }
        }
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (p.charAt(j - 1) == '*') {
                    dp[i][j] = dp[i][j - 1] || dp[i - 1][j];
                } else if (p.charAt(j - 1) == '?' || s.charAt(i - 1) == p.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                }
            }
        }
        return dp[m][n];
    }

    // 正则表达式匹配
    public boolean isMatch10(String s, String p) {
        int m = s.length();
        int n = p.length();
        boolean[][] dp = new boolean[m + 1][n + 1];
        dp[0][0] = true;
        for (int i = 2; i <= n; i++) {
            if (p.charAt(i - 1) == '*') {
                dp[0][i] = dp[0][i - 2];
            }
        }
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s.charAt(i - 1) == p.charAt(j - 1) || p.charAt(j - 1) == '.') {
                    dp[i][j] = dp[i - 1][j - 1];
                } else if (p.charAt(j - 1) == '*') {
                    if (p.charAt(j - 2) == s.charAt(i - 1) || p.charAt(j - 2) == '.') {
                        dp[i][j] = dp[i - 1][j] || dp[i][j - 2];
                    } else {
                        dp[i][j] = dp[i][j - 2];
                    }
                }
            }
        }
        return dp[m][n];
    }
}
