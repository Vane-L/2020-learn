package com.leet.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * @Author: wenhongliang
 */
public class Hard {

    int maxSum = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        getMaxSum(root);
        return maxSum;
    }

    public int getMaxSum(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = Math.max(0, getMaxSum(root.left));
        int right = Math.max(0, getMaxSum(root.right));
        maxSum = Math.max(maxSum, root.val + left + right);
        return root.val + Math.max(left, right);
    }

    // 单词接龙
    public int ladderLengthBFS1Q(String beginWord, String endWord, List<String> wordList) {
        if (!wordList.contains(endWord)) {
            return 0;
        }
        boolean[] visited = new boolean[wordList.size()];
        if (wordList.contains(beginWord)) {
            visited[wordList.indexOf(beginWord)] = true;
        }
        Queue<String> queue = new LinkedList<>();
        queue.offer(beginWord);
        int count = 1;
        while (!queue.isEmpty()) {
            count++;
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String tmp = queue.poll();
                for (int j = 0; j < wordList.size(); j++) {
                    if (visited[j] || !canCovert(tmp, wordList.get(j))) {
                        continue;
                    }
                    if (wordList.get(j).equals(endWord)) {
                        return count;
                    }
                    visited[j] = true;
                    queue.offer(wordList.get(j));
                }
            }
        }
        return 0;
    }

    public int ladderLengthBFS2Q(String beginWord, String endWord, List<String> wordList) {
        if (!wordList.contains(endWord)) {
            return 0;
        }
        Queue<String> queue1 = new LinkedList<>();
        Queue<String> queue2 = new LinkedList<>();
        queue1.offer(beginWord);
        queue2.offer(endWord);
        Set<String> visited1 = new HashSet<>();
        Set<String> visited2 = new HashSet<>();
        visited1.add(beginWord);
        visited2.add(endWord);
        int count1 = 0;
        int count2 = 0;
        while (!queue1.isEmpty() && !queue2.isEmpty()) {
            count1++;
            int size1 = queue1.size();
            for (int i = 0; i < size1; i++) {
                String tmp = queue1.poll();
                for (int j = 0; j < wordList.size(); j++) {
                    String word = wordList.get(j);
                    if (visited1.contains(word) || !canCovert(tmp, word)) {
                        continue;
                    }
                    if (visited2.contains(word)) {
                        return count1 + count2 + 1;
                    }
                    visited1.add(word);
                    queue1.offer(word);
                }
            }
            count2++;
            int size2 = queue2.size();
            for (int i = 0; i < size2; i++) {
                String tmp = queue2.poll();
                for (int j = 0; j < wordList.size(); j++) {
                    String word = wordList.get(j);
                    if (visited2.contains(word) || !canCovert(tmp, word)) {
                        continue;
                    }
                    if (visited1.contains(word)) {
                        return count1 + count2 + 1;
                    }
                    visited2.add(word);
                    queue2.offer(word);
                }
            }
        }
        return 0;
    }

    private boolean canCovert(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        int count = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                ++count;
                if (count > 1) {
                    return false;
                }
            }
        }
        return count == 1;
    }

    public boolean canFinish(int numCourses, int[][] prerequisites) {
        int[] degree = new int[numCourses];
        List<List<Integer>> list = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            list.add(new ArrayList<>());
        }
        for (int[] cp : prerequisites) {
            degree[cp[0]]++;
            list.get(cp[1]).add(cp[0]);
        }
        for (int i = 0; i < numCourses; i++) {
            if (degree[i] == 0) {
                queue.offer(i);
            }
        }
        while (!queue.isEmpty()) {
            int pre = queue.poll();
            numCourses--;
            for (int cur : list.get(pre)) {
                if (--degree[cur] == 0) {
                    queue.offer(cur);
                }
            }
        }
        return numCourses == 0;
    }

    public int[] findOrder(int numCourses, int[][] prerequisites) {
        int idx = 0;
        int[] res = new int[numCourses];
        int[] degree = new int[numCourses];
        List<List<Integer>> list = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            list.add(new ArrayList<>());
        }
        for (int[] cp : prerequisites) {
            degree[cp[0]]++;
            list.get(cp[1]).add(cp[0]);
        }
        for (int i = 0; i < numCourses; i++) {
            if (degree[i] == 0) {
                queue.offer(i);
                res[idx++] = i;
            }
        }
        while (!queue.isEmpty()) {
            int pre = queue.poll();
            numCourses--;
            res[idx++] = pre;
            for (int cur : list.get(pre)) {
                if (--degree[cur] == 0) {
                    queue.offer(cur);
                }
            }
        }
        return res;
    }

    public int[] findOrder2(int numCourses, int[][] prerequisites) {
        int idx = 0;
        int[] res = new int[numCourses];
        int[] degree = new int[numCourses];
        List<List<Integer>> list = new ArrayList<>();
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < numCourses; i++) {
            list.add(new ArrayList<>());
        }
        for (int[] cp : prerequisites) {
            degree[cp[0]]++;
            list.get(cp[1]).add(cp[0]);
        }
        for (int i = 0; i < numCourses; i++) {
            if (degree[i] == 0) {
                queue.offer(i);
            }
        }
        while (!queue.isEmpty()) {
            int pre = queue.poll();
            numCourses--;
            res[idx++] = pre;
            for (int cur : list.get(pre)) {
                if (--degree[cur] == 0) {
                    queue.offer(cur);
                }
            }
        }
        if (numCourses != 0) return new int[0];
        return res;
    }
}
