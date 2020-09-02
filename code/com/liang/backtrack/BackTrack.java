package com.liang.backtrack;

import java.util.ArrayList;
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

    // 1234 -> 12 13 14 23 24 34
    public List<List<Integer>> combine(int n, int k) {
        List<List<Integer>> res = new ArrayList<>();
        back(res, n, k, 1, new LinkedList<Integer>());
        return res;
    }

    private void back(List<List<Integer>> res, int n, int k, int first, LinkedList<Integer> integers) {
        if (integers.size() == k) {
            res.add(new ArrayList<>(integers));
        }
        for (int i = first; i <= n; i++) {
            integers.add(i);
            back(res, n, k, i + 1, integers);
            integers.removeLast();
        }
    }
}
