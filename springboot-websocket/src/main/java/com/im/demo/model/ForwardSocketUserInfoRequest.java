package com.im.demo.model;

import lombok.Data;

/**
 * @Author: wenhongliang
 */
@Data
public class ForwardSocketUserInfoRequest {
    private String sessionId;
    private String userId;
    private Object object;
}
