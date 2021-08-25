package com.im.demo.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wenhongliang
 */
@Data
public class SocketUserInfoSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    //WebSocket Session 的sessionId
    private String sessionId;
    //userId
    private String userId;
    //客户端创建连接时候的服务器IP+端口号
    private String ip;
}