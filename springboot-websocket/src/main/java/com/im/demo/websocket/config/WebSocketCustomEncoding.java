package com.im.demo.websocket.config;

import com.alibaba.fastjson.JSON;
import com.im.demo.model.User;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * @Author: wenhongliang
 */
public class WebSocketCustomEncoding implements Encoder.Text<User> {
    @Override
    public String encode(User user) throws EncodeException {
        assert user != null;
        return JSON.toJSONString(user);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
