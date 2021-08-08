package com.netty.demo.codec;


import io.netty.handler.codec.LengthFieldPrepender;

// 粘包和半包
public class OrderFrameEncoder extends LengthFieldPrepender {
    public OrderFrameEncoder() {
        super(2);
    }
}
