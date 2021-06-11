package com.thread;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class DeadLockTest {
    static class TreeNode {
        TreeNode parent = null;
        List<TreeNode> children = new ArrayList<>();

        public synchronized void addChild(TreeNode child) {
            if (!this.children.contains(child)) {
                this.children.add(child);
                child.setParentOnly(this);
            }
        }

        public synchronized void addChildOnly(TreeNode child) {
            if (!this.children.contains(child)) {
                this.children.add(child);
            }
        }

        public synchronized void setParent(TreeNode parent) {
            this.parent = parent;
            parent.addChildOnly(this);
        }

        public synchronized void setParentOnly(TreeNode parent) {
            this.parent = parent;
        }
    }

    public static void main(String[] args) {
        TreeNode treeNode = new TreeNode();
        // 可能会发生死锁
        Thread thread1 = new Thread(() -> treeNode.addChild(treeNode));
        Thread thread2 = new Thread(() -> treeNode.setParent(treeNode));
        thread1.start();
        thread2.start();
    }
}
