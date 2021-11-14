package com.leet.week;

import com.leet.list.ListNode;

/**
 * @Author: wenhongliang
 */
public class Week13 {
    public static void main(String[] args) {
        System.out.println(new Week13().timeRequiredToBuy(new int[]{2, 3, 2}, 2)); // output:6
        System.out.println(new Week13().decodeCiphertext("ch   ie   pr", 3)); // output:cipher
        System.out.println(new Week13().decodeCiphertext("iveo    eed   l te   olc", 4)); // output:i love leetcode
    }

    public int timeRequiredToBuy(int[] tickets, int k) {
        int res = 0;
        int idx = 0;
        int len = tickets.length;
        while (tickets[k] != 0) {
            if (tickets[idx % len] != 0) {
                tickets[idx % len]--;
                res++;
            }
            idx++;
        }
        return res;
    }

    public ListNode reverseEvenLengthGroups(ListNode head) {
        int step = 1;
        int group = 1;
        ListNode dummy = new ListNode();
        dummy.next = head;
        ListNode cur = head;
        ListNode p = head;
        while (p.next != null) {
            if (group % 2 == 0) {
                if (step == group) {
                    ListNode[] nodes = reverse(cur.next, p);
                    cur.next = nodes[0];
                    cur = nodes[1];
                    p = nodes[1];
                    group++;
                    step = 0;
                }
            } else {
                if (step == group) {
                    group++;
                    step = 0;
                    cur = p;
                }
            }
            p = p.next;
            step++;
        }
        //不足group，但是满足偶数的情况
        if (step % 2 == 0) {
            ListNode[] nodes = reverse(cur.next, p);
            cur.next = nodes[0];
        }
        return dummy.next;
    }

    public ListNode[] reverse(ListNode head, ListNode tail) {
        ListNode p = head, q;
        ListNode r = tail.next;
        while (r != tail) {
            q = p.next;
            p.next = r;
            r = p;
            p = q;
        }
        return new ListNode[]{r, head};
    }

    public String decodeCiphertext(String encodedText, int rows) {
        if (rows == 1) return encodedText;
        int cols = encodedText.length() / rows;
        StringBuilder sb = new StringBuilder();
        // row 0 1 2
        // 0,0 1,1 2,2
        // 0,1 1,2 2,3
        for (int c = 0; c < cols; c++) {
            int j = c;
            for (int i = 0; i < rows && j < cols; i++, j++) {
                sb.append(encodedText.charAt(i * cols + j));
            }
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private int[] pre;
    private boolean[][] res;

    public boolean[] friendRequests(int n, int[][] restrictions, int[][] requests) {
        pre = new int[n];
        res = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            pre[i] = i;
        }
        for (int i = 0; i < restrictions.length; i++) {
            int x = restrictions[i][0], y = restrictions[i][1];
            res[x][y] = true;
            res[y][x] = true;
        }
        boolean[] ans = new boolean[requests.length];
        for (int i = 0; i < requests.length; i++) {
            int x = find(requests[i][0]), y = find(requests[i][1]);
            if (x == y) {
                ans[i] = true;
            } else {
                if (res[x][y] || res[y][x]) {
                    ans[i] = false;
                } else {
                    pre[x] = y;
                    for (int j = 0; j < n; j++) {
                        if (res[x][j]) {
                            res[y][j] = true;
                            res[j][y] = true;
                        }
                    }
                    ans[i] = true;
                }
            }
        }
        return ans;
    }

    public int find(int x) {
        int r = x;
        while (pre[r] != r) {
            r = pre[r];
        }
        int i = x, j;
        while (i != r) {
            j = pre[i];
            pre[i] = r;
            i = j;
        }
        return r;
    }
}
