package com.liang.list;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * @Author: wenhongliang
 */
public class Medium {

    public static void main(String[] args) {
        Medium medium = new Medium();
    }

    public ListNode removeNthFromEnd(ListNode head, int n) {
        if (head == null) {
            return null;
        }
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode slow = dummy, fast = dummy;
        while (n-- >= 0) {
            fast = fast.next;
        }
        while (fast != null) {
            slow = slow.next;
            fast = fast.next;
        }
        slow.next = slow.next.next;
        return dummy.next;
    }

    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) {
            return l2;
        }
        if (l2 == null) {
            return l1;
        }
        ListNode dummy = new ListNode(0);
        ListNode newHead = dummy;
        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                dummy.next = l1;
                l1 = l1.next;
            } else {
                dummy.next = l2;
                l2 = l2.next;
            }
            dummy = dummy.next;
        }
        if (l1 != null) {
            dummy.next = l1;
        }
        if (l2 != null) {
            dummy.next = l2;
        }
        return newHead.next;
    }

    public ListNode mergeKLists(ListNode[] lists) {
        // (v1, v2) -> v1.val - v2.val !!!!!!!
        Queue<ListNode> queue = new PriorityQueue<>((v1, v2) -> v1.val - v2.val);
        for (ListNode node : lists) {
            if (node != null) {
                queue.offer(node);
            }
        }
        ListNode dummy = new ListNode(0);
        ListNode newHead = dummy;
        while (!queue.isEmpty()) {
            ListNode tmp = queue.poll();
            dummy.next = tmp;
            tmp = tmp.next;
            dummy = dummy.next;
            if (tmp != null) {
                queue.offer(tmp);
            }
        }
        return newHead.next;
    }

    // 输入：head = [1,2,3,4]
    // 输出：[2,1,4,3]
    public ListNode swapPairs(ListNode head) {
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode tmp = dummy;
        while (tmp.next != null && tmp.next.next != null) {
            ListNode p = tmp.next;
            ListNode q = tmp.next.next;
            tmp.next = q;
            p.next = q.next;
            q.next = p;
            tmp = p;
        }
        return dummy.next;
    }
}
