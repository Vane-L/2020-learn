package com.test.demo.timing.template;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Ring implements Serializable {
    /***
     * 当前节点地址
     */
    private String node;

    /***
     * 下一个节点地址
     */
    private String nextNode;

    /***
     * 节点任务集合
     */
    private List<BusinessTask> taskList;
}
