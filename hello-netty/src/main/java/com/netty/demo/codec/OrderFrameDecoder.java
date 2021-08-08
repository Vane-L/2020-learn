package com.netty.demo.codec;


import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

// 粘包和半包
public class OrderFrameDecoder extends LengthFieldBasedFrameDecoder {
    public OrderFrameDecoder() {
        super(1024 * 10, 0, 2, 0, 2);
    }
}
