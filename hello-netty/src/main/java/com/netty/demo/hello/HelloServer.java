package com.netty.demo.hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author: wenhongliang
 */
public class HelloServer {
    public static void main(String[] args) {
        new HelloServer().bind();
    }

    public void bind() {

        // 服务器端应用程序使用两个NioEventLoopGroup创建两个EventLoop的组
        // 主线程组, 用于接受客户端的连接，但是不做任何处理，
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 从线程组, 当boss接受连接并注册被接受的连接到worker时，处理被接受连接的流量。
        EventLoopGroup workerGroup = new NioEventLoopGroup(3);

        try {
            // netty服务器启动类的创建, 辅助工具类，用于服务器通道的一系列配置
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 前者用于处理客户端连接事件，后者用于处理网络IO
            serverBootstrap.group(bossGroup, workerGroup)           //绑定两个线程组
                    .channel(NioServerSocketChannel.class)   //指定NIO的模式
                    .childHandler(new ChannelInitializer<SocketChannel>() {  // 子处理器，用于处理workerGroup
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new HelloServerHandler());
                        }
                    });

            // 启动server，绑定端口，开始接收进来的连接，设置8088为启动的端口号，同时启动方式为同步
            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();
            // 监听关闭的channel，等待服务器 socket 关闭 。设置位同步方式
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //退出线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
