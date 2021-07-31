package com.netty.demo.hello;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Author: wenhongliang
 */
public class HelloClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf result = (ByteBuf) msg;
        byte[] bytesMsg = new byte[result.readableBytes()];
        result.readBytes(bytesMsg);
        System.out.println("Server said:" + new String(bytesMsg));
        result.release();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String msg = "hello Server!";
        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
        encoded.writeBytes(msg.getBytes());
        ctx.writeAndFlush(encoded);
    }
}
