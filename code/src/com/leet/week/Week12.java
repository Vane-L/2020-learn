package com.leet.week;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
public class Week12 {

    public int countVowelSubstrings(String word) {
        int res = 0;
        for (int i = 0; i < word.length(); i++) {
            boolean aa = false;
            boolean ee = false;
            boolean ii = false;
            boolean oo = false;
            boolean uu = false;
            for (int j = i; j < word.length(); j++) {
                if (word.charAt(j) != 'a' && word.charAt(j) != 'e' && word.charAt(j) != 'i' && word.charAt(j) != 'o' && word.charAt(j) != 'u') {
                    break;
                }
                if (word.charAt(j) == 'a') aa = true;
                if (word.charAt(j) == 'e') ee = true;
                if (word.charAt(j) == 'i') ii = true;
                if (word.charAt(j) == 'o') oo = true;
                if (word.charAt(j) == 'u') uu = true;
                if (aa && ee && ii && oo && uu) res++;
            }
        }
        return res;
    }

    public int countVowelSubstrings0(String word) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            HashSet<Character> set = new HashSet<>();
            for (int j = i; j < word.length(); j++) {
                if ("aeiou".indexOf(word.charAt(j)) < 0) {
                    break;
                }
                set.add(word.charAt(j));
                if (set.size() == 5) {
                    count++;
                }
            }
        }
        return count;
    }

    public long countVowels(String word) {
        long count = 0;
        for (int i = 0; i < word.length(); i++) {
            if ("aeiou".indexOf(word.charAt(i)) >= 0) {
                // 元音会出现在多少个子串中
                count += (i + 1L) * (word.length() - i);
            }
        }
        return count;
    }

    public int minimizedMaximum0(int n, int[] quantities) {
        int left = 1, right = 100000;
        while (left < right) {
            int mid = (left + right) / 2;
            int count = 0;
            for (int quantity : quantities) {
                count += (quantity + mid - 1) / mid;
            }
            if (count > n) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }


    public int maximalPathQuality(int[] values, int[][] edges, int maxTime) {
        List<int[]>[] list = new ArrayList[values.length];
        for (int i = 0; i < values.length; i++) {
            list[i] = new ArrayList<>();
        }
        for (int[] edge : edges) {
            list[edge[0]].add(new int[]{edge[1], edge[2]});
            list[edge[1]].add(new int[]{edge[0], edge[2]});
        }
        return dfs(0, maxTime, new int[values.length], values, list);
    }

    private int dfs(int node, int time, int[] map, int[] values, List<int[]>[] list) {
        if (time < 0) {
            return Integer.MIN_VALUE;
        }
        int max = node > 0 ? Integer.MIN_VALUE : map[0] > 0 ? 0 : values[node];
        map[node]++;
        for (int[] next : list[node]) {
            max = Math.max(max, (map[node] > 1 ? 0 : values[node])
                    + dfs(next[0], time - next[1], map, values, list));
        }
        map[node]--;
        return max;
    }

    int res = 0;

    public int maximalPathQuality0(int[] values, int[][] edges, int maxTime) {
        int n = values.length;
        Map<Integer, List<int[]>> map = new HashMap<>();
        int[] visited = new int[n];
        visited[0] = 1;
        for (int i = 0; i < n; i++) {
            map.put(i, new ArrayList<>());
        }
        for (int[] edge : edges) {
            map.get(edge[0]).add(new int[]{edge[1], edge[2]});
            map.get(edge[1]).add(new int[]{edge[0], edge[2]});
        }
        back(0, maxTime, values[0], map, visited, values);
        return res;
    }

    void back(int cur, int maxTime, int sum, Map<Integer, List<int[]>> map, int[] visited, int[] values) {
        if (cur == 0) {
            res = Math.max(res, sum);
        }
        for (int[] nextAndTime : map.get(cur)) {
            int next = nextAndTime[0], time = nextAndTime[1];
            if (time > maxTime) continue;
            if (++visited[next] == 1) {
                sum += values[nextAndTime[0]];
            }
            back(next, maxTime - time, sum, map, visited, values);
            if (--visited[next] == 0) {
                sum -= values[next];
            }
        }
    }
}

