package com.leet.stack;

import java.util.Stack;

/**
 * @Author: wenhongliang
 */
public class StackProblem {
    public static void main(String[] args) {
        StackProblem stackProblem = new StackProblem();
        System.out.println(stackProblem.removeDuplicates2("abcd", 2));
        System.out.println(stackProblem.removeDuplicates2("deeedbbcccbdaa", 3));
        System.out.println(stackProblem.removeDuplicates("yfttttfbbbbnnnnffbgffffgbbbbgssssgthyyyy", 4));
        System.out.println(stackProblem.removeOuterParentheses("(()())(())"));
        System.out.println(stackProblem.minAddToMakeValid("()))(("));
        System.out.println(stackProblem.minAddToMakeValid("((()))"));
    }


    public String removeDuplicates(String s, int k) {
        Stack<Integer> stack = new Stack<>();
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            if (i == 0 || sb.charAt(i) != sb.charAt(i - 1)) {
                stack.push(1);
            } else {
                int tmp = stack.pop() + 1;
                if (tmp == k) {
                    sb.delete(i - k + 1, i + 1);
                    i = i - k;
                } else {
                    stack.push(tmp);
                }
            }
        }
        return sb.toString();
    }

    public String removeDuplicates2(String s, int k) {
        Stack<int[]> stack = new Stack<>();
        for (int i = 0; i < s.length(); i++) {
            char character = s.charAt(i);
            if (stack.isEmpty() || stack.peek()[0] != character) {
                stack.push(new int[]{character, 1});
            } else {
                stack.peek()[1]++;
                if (stack.peek()[1] == k) {
                    stack.pop();
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            int[] tmp = stack.pop();
            for (int i = 0; i < tmp[1]; i++) {
                sb.append((char) (tmp[0]));
            }
        }
        return sb.reverse().toString();
    }

    //输入："(()())(())"
    //输出："()()()"
    public String removeOuterParentheses(String s) {
        StringBuilder sb = new StringBuilder();
        int level = 0;
        for (char c : s.toCharArray()) {
            if (c == ')') --level;
            if (level >= 1) sb.append(c);
            if (c == '(') ++level;
        }
        return sb.toString();
    }

    // 921. 使括号有效的最少添加
    // 输入："()))(("
    // 输出：4
    public int minAddToMakeValid(String S) {
        Stack<Character> stack = new Stack<>();
        for (char c : S.toCharArray()) {
            if (stack.isEmpty()) {
                stack.push(c);
            } else {
                char tmp = stack.peek();
                if (c == ')' && tmp == '(') {
                    stack.pop();
                } else {
                    stack.push(c);
                }
            }
        }
        return stack.size();
    }

    // 计算 '(' 出现的次数减去 ')' 出现的次数
    public int minAddToMakeValid2(String S) {
        int left = 0, right = 0;
        for (char c : S.toCharArray()) {
            if (c == '(') {
                right++;
            } else {
                right--;
            }
            if (right < 0) {
                left++;
                right++;
            }
        }
        return left + right;
    }
}
