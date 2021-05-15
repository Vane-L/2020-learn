package com.leet.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class Top {

    class Node {
        int val;
        Node next;
        Node random;

        public Node(int val) {
            this.val = val;
            this.next = null;
            this.random = null;
        }
    }

    public Node copyRandomList(Node head) {
        if (head == null) {
            return null;
        }
        Node p = head;
        while (p != null) {
            // 原来节点的下一个节点
            Node tmp = p.next;
            // 复制一个节点在原来节点的下一个节点
            p.next = new Node(p.val);
            // 复制节点的下一个节点是原来节点的下一个节点
            p.next.next = tmp;
            // p->q 变成 p->p1->q
            p = tmp;
        }
        p = head;
        while (p != null) {
            if (p.random != null) {
                // p1节点的random节点指向复制的random节点
                p.next.random = p.random.next;
            }
            p = p.next.next;
        }
        p = head;
        Node newHead = p.next;
        Node clone = newHead;
        while (clone.next != null) {
            // 原链表节点p还原，p->p1->q 变成 p->q
            p.next = p.next.next;
            p = p.next;
            // 新链表节点连接，p1->q1
            clone.next = clone.next.next;
            clone = clone.next;
        }
        p.next = null;
        return newHead;
    }

    public boolean hasCycle(ListNode head) {
        if (head == null || head.next == null) {
            return false;
        }
        ListNode slow = head;
        ListNode fast = head.next;
        while (slow != fast) {
            if (fast == null || fast.next == null) {
                return false;
            }
            slow = slow.next;
            fast = fast.next.next;
        }
        return true;
    }

    public ListNode sortList(ListNode head) {
        ListNode cur = head;
        List<Integer> list = new ArrayList<>();
        while (cur != null) {
            list.add(cur.val);
            cur = cur.next;
        }
        Collections.sort(list);
        ListNode newHead = new ListNode(-1);
        cur = newHead;
        for (int x : list) {
            ListNode tmp = new ListNode(x);
            cur.next = tmp;
            cur = cur.next;
        }
        return newHead.next;
    }

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        int lenA = 0, lenB = 0;
        ListNode tmpA = headA, tmpB = headB;
        while (tmpA != null) {
            tmpA = tmpA.next;
            lenA++;
        }
        while (tmpB != null) {
            tmpB = tmpB.next;
            lenB++;
        }
        if (lenA > lenB) {
            int k = lenA - lenB;
            while (k > 0) {
                headA = headA.next;
                k--;
            }
        } else {
            int k = lenB - lenA;
            while (k > 0) {
                headB = headB.next;
                k--;
            }
        }
        while (headA != headB) {
            headA = headA.next;
            headB = headB.next;
        }
        return headA;
    }

    public ListNode reverseList(ListNode head) {
        if (head == null) {
            return head;
        }
        // p q
        // 1 2 3 4 5
        //
        // 5 4 3 2 1
        ListNode p = head, q = head.next, r = null;
        while (p.next != null) {
            ListNode tmp = p;
            p.next = r;
            p = q;
            q = q.next;
            r = tmp;
        }
        p.next = r;
        return p;
    }

    public void deleteNode(ListNode node) {
        node.val = node.next.val;
        node.next = node.next.next;
    }

    public ListNode oddEvenList(ListNode head) {
        if (head == null) {
            return head;
        }
        ListNode evenHead = head.next;
        ListNode odd = head, even = evenHead;
        while (even != null && even.next != null) {
            odd.next = even.next;
            odd = odd.next;
            even.next = odd.next;
            even = even.next;
        }
        odd.next = evenHead;
        return head;
    }


    public int titleToNumber(String columnTitle) {
        char[] chars = columnTitle.toCharArray();
        int res = 0;
        for (char c : chars) {
            res = res * 26 + c - 'A' + 1;
        }
        return res;
    }

    public int fourSumCount(int[] A, int[] B, int[] C, int[] D) {
        HashMap<Integer, Integer> mapAB = new HashMap<>();
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B.length; j++) {
                int sumAB = A[i] + B[j];
                mapAB.put(sumAB, mapAB.getOrDefault(sumAB, 0) + 1);
            }
        }
        int res = 0;
        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < D.length; j++) {
                int sumCD = C[i] + D[j];
                res += mapAB.getOrDefault(-sumCD, 0);
            }
        }
        return res;
    }
}
