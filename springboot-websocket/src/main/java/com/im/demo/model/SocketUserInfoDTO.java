package com.im.demo.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
@Data
public class SocketUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Map<String, SocketUserInfoSessionDTO>> listMap;
}

