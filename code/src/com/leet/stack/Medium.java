package com.leet.stack;

import java.util.Stack;

/**
 * @Author: wenhongliang
 */
public class Medium {
    public static boolean isValidSerialization1(String preorder) {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        for (int i = 0; i < preorder.length(); i++) {
            if (stack.isEmpty()) {
                return false;
            }
            char ch = preorder.charAt(i);
            if (ch == ',') {
                continue;
            }
            if (ch == '#') {
                int tmp = stack.pop() - 1;
                if (tmp > 0) {
                    stack.push(tmp);
                }
            } else if (Character.isDigit(ch)) {
                while (i < preorder.length() - 1 && preorder.charAt(i + 1) != ',') {
                    i++;
                }
                int tmp = stack.pop() - 1;
                if (tmp > 0) {
                    stack.push(tmp);
                }
                stack.push(2);
            }
        }
        return stack.isEmpty();
    }

    public static boolean isValidSerialization(String preorder) {
        int slot = 1;
        for (int i = 0; i < preorder.length(); i++) {
            if (slot == 0) {
                return false;
            }
            char ch = preorder.charAt(i);
            if (ch == ',') {
                continue;
            }
            if (ch == '#') {
                slot--;
            } else if (Character.isDigit(ch)) {
                while (i < preorder.length() - 1 && preorder.charAt(i + 1) != ',') {
                    i++;
                }
                slot++;
            }
        }
        return slot == 0;
    }
}
