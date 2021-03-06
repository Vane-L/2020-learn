package com.leet.backtrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class BackTrack {
    public static void main(String[] args) {
        new BackTrack().generateParenthesis(3);
        new BackTrack().permutation("qwe");
        new BackTrack().subsets(new int[]{1, 2, 3});
        new BackTrack().permute(new int[]{1, 2, 3});
        new BackTrack().combine(4, 3);
        new BackTrack().numTilePossibilities("AAB");
        new BackTrack().subsetsWithDup(new int[]{1, 2, 2});
        new BackTrack().permutation08("qqe");
        new BackTrack().combinationSum3(3, 9);
        new BackTrack().combinationSum(new int[]{2, 3, 5}, 8);
        System.out.println(new BackTrack().exist(new char[][]{{'a', 'b'}, {'c', 'd'}}, "abcd"));
        System.out.println(new BackTrack().exist(new char[][]{{'a', 'b', 'c', 'e'}, {'s', 'f', 'c', 's'}, {'a', 'd', 'e', 'e'}}, "abcced"));
        System.out.println(new BackTrack().exist(new char[][]{{'a', 'b', 'c', 'e'}, {'s', 'f', 'c', 's'}, {'a', 'd', 'e', 'e'}}, "abcb"));
        new BackTrack().partition("aab");
        new BackTrack().permuteUnique(new int[]{1, 1, 2});
    }

    public List<String> generateParenthesis(int n) {
        List<String> res = new ArrayList<>();
        dfs(res, "", 0, 0, n);
        return res;
    }

    public void dfs(List<String> list, String s, int left, int right, int n) {
        if (s.length() == 2 * n) {
            list.add(s);
            return;
        }
        /**
         * 如果左括号数量不大于 n，我们可以放一个左括号。
         * 如果右括号数量小于左括号的数量，我们可以放一个右括号。
         */
        if (left < n) {
            dfs(list, s + "(", left + 1, right, n);
        }
        if (right < left) {
            dfs(list, s + ")", left, right + 1, n);
        }
    }

    /*----------------------------------------------------------------------*/
    public String[] permutation(String str) {
        if (str.length() == 0)
            return new String[0];
        List<String> res = new ArrayList<>();
        boolean[] used = new boolean[str.length()];
        back(res, str, used, "");
        return res.toArray(new String[res.size()]);
    }

    private void back(List<String> res, String str, boolean[] used, String cur) {
        if (cur.length() == str.length()) {
            res.add(cur);
            return;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!used[i]) {
                used[i] = true;
                back(res, str, used, cur + str.charAt(i));
                used[i] = false;
            }
        }
    }

    /*----------------------------------------------------------------------*/
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        backtrack(res, nums, new ArrayList(), 0);
        return res;
    }

    private void backtrack(List<List<Integer>> res, int[] nums, ArrayList<Integer> tmp, int cur) {
        res.add(new ArrayList<>(tmp));
        for (int i = cur; i < nums.length; i++) {
            tmp.add(nums[i]);
            backtrack(res, nums, tmp, i + 1);
            tmp.remove(tmp.size() - 1);
        }
    }

    /*----------------------------------------------------------------------*/
    public List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        all(res, nums, used, new ArrayList());
        return res;
    }

    private void all(List<List<Integer>> res, int[] nums, boolean[] used, ArrayList<Integer> arrayList) {
        if (arrayList.size() == nums.length) {
            res.add(new ArrayList<>(arrayList));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (!used[i]) {
                used[i] = true;
                arrayList.add(nums[i]);
                all(res, nums, used, arrayList);
                used[i] = false;
                arrayList.remove(arrayList.size() - 1);
            }
        }
    }

    /*----------------------------------------------------------------------*/
    // 1234 -> 12 13 14 23 24 34
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> res = new ArrayList<>();
        back(res, n, k, 1, new LinkedList<Integer>());
        return res;
    }

    private void back(List<List<Integer>> res, int n, int k, int first, LinkedList<Integer> integers) {
        if (integers.size() == k) {
            res.add(new ArrayList<>(integers));
            return;
        }
        for (int i = first; i <= n; i++) {
            integers.add(i);
            back(res, n, k, i + 1, integers);
            integers.removeLast();
        }
    }

    private void backGood(List<List<Integer>> res, int n, int k, int first, LinkedList<Integer> integers) {
        if (integers.size() == k) {
            res.add(new ArrayList<>(integers));
            return;
        }
        // 搜索起点的上界 + 接下来要选择的元素个数 - 1 = n
        // 选择的元素个数 = k - path.size()
        for (int i = first; i <= n + 1 - (k - integers.size()); i++) {
            integers.add(i);
            backGood(res, n, k, i + 1, integers);
            integers.removeLast();
        }
    }


    /*----------------------------------------------------------------------*/
    // 输入："AAB"
    // 输出：8
    // 解释：可能的序列为 "A", "B", "AA", "AB", "BA", "AAB", "ABA", "BAA"。
    public int numTilePossibilities(String tiles) {
        int[] counter = new int[26];
        for (int i = 0; i < tiles.length(); i++)
            counter[tiles.charAt(i) - 'A']++;
        int res = dfs(counter);
        return res;
    }

    private int dfs(int[] counter) {
        int sum = 0;
        for (int i = 0; i < 26; i++) {
            if (counter[i] > 0) {
                sum++;
                counter[i]--;
                sum += dfs(counter);
                counter[i]++;
            }
        }
        return sum;
    }

    /*----------------------------------------------------------------------*/
    // 输入: [1,2,2]
    // 输出: [[2],[1],[1,2,2],[2,2],[1,2],[]]
    public List<List<Integer>> subsetsWithDup(int[] nums) {
        List<List<Integer>> lists = new ArrayList<>();
        Arrays.sort(nums);
        back(lists, nums, 0, new ArrayList<>());
        return lists;
    }

    private void back(List<List<Integer>> lists, int[] nums, int first, List<Integer> tmp) {
        lists.add(new ArrayList<>(tmp));
        for (int i = first; i < nums.length; i++) {
            if (i > first && nums[i] == nums[i - 1]) {
                continue;
            }
            tmp.add(nums[i]);
            back(lists, nums, i + 1, tmp);
            tmp.remove(tmp.size() - 1);
        }
    }

    /*----------------------------------------------------------------------*/
    public String[] permutation08(String S) {
        List<String> res = new ArrayList<>();
        boolean[] used = new boolean[S.length()];
        char[] chars = S.toCharArray();
        Arrays.sort(chars);
        back(res, used, chars, "");
        return res.toArray(new String[res.size()]);
    }

    private void back(List<String> res, boolean[] used, char[] chars, String tmp) {
        if (tmp.length() == chars.length) {
            res.add(tmp);
            return;
        }
        for (int i = 0; i < chars.length; i++) {
            if (!used[i]) {
                if (i > 0 && chars[i] == chars[i - 1] && !used[i - 1]) {
                    continue;
                }
                used[i] = true;
                back(res, used, chars, tmp + chars[i]);
                used[i] = false;
            }
        }
    }

    /*----------------------------------------------------------------------*/
    //输入: k = 3, n = 9
    //输出: [[1,2,6], [1,3,5], [2,3,4]]
    public List<List<Integer>> combinationSum3(int k, int n) {
        List<List<Integer>> res = new ArrayList<>();
        boolean[] used = new boolean[10];
        backSum3(res, k, n, used, new ArrayList<>(), 1);
        return res;
    }

    private void backSum3(List<List<Integer>> res, int k, int n, boolean[] used, ArrayList<Integer> tmp, int cur) {
        if (n == 0 && k == 0) {
            res.add(new ArrayList<>(tmp));
            return;
        }
        for (int i = cur; i <= 9; i++) {
            if (n - i < 0 || used[i]) {
                break;
            }
            tmp.add(i);
            used[i] = true;
            backSum3(res, k - 1, n - i, used, tmp, cur + 1);
            used[i] = false;
            tmp.remove(tmp.size() - 1);
        }
    }

    /*----------------------------------------------------------------------*/
    //输入：candidates = [2,3,5], target = 8,
    //所求解集为：
    //[ [2,2,2,2],[2,3,3],[3,5] ]
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        Arrays.sort(candidates);
        List<List<Integer>> res = new ArrayList<>();
        backSum(res, candidates, target, new ArrayList<Integer>(), 0, 0);
        return res;
    }

    private void backSum(List<List<Integer>> res, int[] candidates, int target, ArrayList<Integer> tmp, int cur, int start) {
        if (target == cur) {
            res.add(new ArrayList<>(tmp));
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            if (cur + i > target) {
                break;
            }
            cur += candidates[i];
            tmp.add(candidates[i]);
            backSum(res, candidates, target, tmp, cur, i);
            tmp.remove(tmp.size() - 1);
            cur -= candidates[i];
        }
    }

    /*----------------------------------------------------------------------*/
    public boolean exist(char[][] board, String word) {
        char[] chars = word.toCharArray();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (backWord(board, chars, i, j, 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean backWord(char[][] board, char[] word, int i, int j, int idx) {
        if (i < 0 || i >= board.length || j < 0 || j >= board[0].length || board[i][j] != word[idx]) {
            return false;
        }
        if (idx == word.length - 1) {
            return true;
        }
        char tmp = board[i][j];
        board[i][j] = '#';
        boolean flag = backWord(board, word, i + 1, j, idx + 1) || backWord(board, word, i, j + 1, idx + 1)
                || backWord(board, word, i - 1, j, idx + 1) || backWord(board, word, i, j - 1, idx + 1);
        board[i][j] = tmp;
        return flag;
    }

    /*----------------------------------------------------------------------*/
    public List<List<String>> partition(String s) {
        List<List<String>> res = new ArrayList<>();
        backPartition(res, new ArrayList<>(), s, 0, s.length());
        return res;
    }

    private void backPartition(List<List<String>> res, ArrayList<String> path, String s, int start, int len) {
        if (start == len) {
            res.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < len; i++) {
            if (!isPalindrome(s, start, i)) {
                continue;
            }
            path.add(s.substring(start, i + 1));
            backPartition(res, path, s, i + 1, len);
            path.remove(path.size() - 1);
        }
    }

    private boolean isPalindrome(String str, int left, int right) {
        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    /*----------------------------------------------------------------------*/
    // 输入: [1,1,2]
    //输出:  [ [1,1,2], [1,2,1], [2,1,1] ]
    public List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        Arrays.sort(nums);
        boolean[] used = new boolean[nums.length];
        backUnique(res, nums, used, new ArrayList<>());
        return new ArrayList<>(res);
    }

    private void backUnique(List<List<Integer>> res, int[] nums, boolean[] used, ArrayList<Integer> tmp) {
        if (tmp.size() == nums.length) {
            res.add(new ArrayList<>(tmp));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            // 当前值已用 OR 当前值==前一个值
            if (used[i] || (i > 0 && nums[i] == nums[i - 1] && !used[i - 1])) {
                continue;
            }
            tmp.add(nums[i]);
            used[i] = true;
            backUnique(res, nums, used, tmp);
            used[i] = false;
            tmp.remove(tmp.size() - 1);
        }
    }
}
