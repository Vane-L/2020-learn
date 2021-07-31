package com.netty.demo.hello;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author: wenhongliang
 */
public class HelloClient {
    public static void main(String[] args) {
        HelloClient client = new HelloClient();
        client.connect("127.0.0.1", 8088);
    }

    public void connect(String host, int port) {
        EventLoopGroup worker = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new HelloClientHandler());
                }
            });

            /** 开启客户端监听，连接到远程节点，阻塞等待直到连接完成*/
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            /**阻塞等待数据，直到channel关闭(客户端关闭)*/
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worker.shutdownGracefully();
        }
    }

}
