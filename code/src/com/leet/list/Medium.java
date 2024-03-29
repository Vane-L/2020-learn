package com.leet.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

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

    public ListNode mergeList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode slow = head, fast = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode leftHead = head, rightHead = slow.next;
        slow.next = null;
        leftHead = mergeList(leftHead);
        rightHead = mergeList(rightHead);
        ListNode dummy = new ListNode(0);
        ListNode tmp = dummy;
        while (leftHead != null && rightHead != null) {
            if (leftHead.val < rightHead.val) {
                tmp.next = leftHead;
                leftHead = leftHead.next;
            } else {
                tmp.next = rightHead;
                rightHead = rightHead.next;
            }
            tmp = tmp.next;
        }
        tmp.next = leftHead == null ? rightHead : leftHead;
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

    public ListNode swapPairsDFS(ListNode head) {
        // 只有一个节点
        if (head == null || head.next == null) {
            return head;
        }
        // 2个节点
        if (head.next.next == null) {
            ListNode second = head.next;
            head.next = null;
            second.next = head;
            return second;
        }
        ListNode sessor = swapPairsDFS(head.next.next);
        ListNode newHead = head.next;
        newHead.next = head;
        head.next = sessor;
        return newHead;
    }

    public ListNode insertionSortList(ListNode head) {
        if (head == null) {
            return null;
        }
        List<Integer> list = new ArrayList<>();
        ListNode tmp = head;
        while (tmp != null) {
            list.add(tmp.val);
            tmp = tmp.next;
        }
        Collections.sort(list);
        tmp = head;
        int i = 0;
        while (tmp != null) {
            tmp.val = list.get(i++);
            tmp = tmp.next;
        }
        return head;

        /*ListNode dummy = new ListNode(0);
        ListNode newHead = dummy;
        for (int x : list) {
            dummy.next = new ListNode(x);
            dummy = dummy.next;
        }
        return newHead.next;*/
    }

    /**
     * 输入: -1->5->3->4->0
     * 输出: -1->0->3->4->5
     */
    public ListNode insertionSortList2(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode pre = head, cur = head.next;
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        while (cur != null) {
            if (pre.val < cur.val) {
                pre = cur;
                cur = cur.next;
            } else {
                ListNode p = dummy;
                // 从head开始找
                while (p.next != cur && p.next.val < cur.val) {
                    p = p.next;
                }
                pre.next = cur.next;
                cur.next = p.next;
                p.next = cur;
                cur = pre.next;
            }
        }
        return dummy.next;
    }

    public ListNode oddEvenList(ListNode head) {
        if (head == null) {
            return null;
        }
        // 奇数
        ListNode odd = head;
        // 偶数
        ListNode even = odd.next;
        ListNode evenHead = even;
        while (even != null && even.next != null) {
            odd.next = even.next;
            odd = odd.next;
            even.next = odd.next;
            even = even.next;
        }
        odd.next = evenHead;
        return head;
    }
}
