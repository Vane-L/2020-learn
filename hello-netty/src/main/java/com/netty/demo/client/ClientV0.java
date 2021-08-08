package com.netty.demo.client;

import com.netty.demo.client.handler.ClientIdleCheckHandler;
import com.netty.demo.client.handler.ClientKeepAliveHandler;
import com.netty.demo.codec.OrderFrameDecoder;
import com.netty.demo.codec.OrderFrameEncoder;
import com.netty.demo.codec.OrderProtocolDecoder;
import com.netty.demo.codec.OrderProtocolEncoder;
import com.netty.demo.message.RequestMessage;
import com.netty.demo.common.auth.AuthOperation;
import com.netty.demo.common.order.OrderOperation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.util.UUID;

public class ClientV0 {

    public static void main(String[] args) throws InterruptedException, SSLException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(NioChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000);

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap.group(group);

            ClientKeepAliveHandler clientKeepaliveHandler = new ClientKeepAliveHandler();
            LoggingHandler debugHandler = new LoggingHandler(LogLevel.DEBUG);
            LoggingHandler infoHandler = new LoggingHandler(LogLevel.INFO);

            // ssl
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);

            SslContext sslContext = sslContextBuilder.build();

            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("debugLog", debugHandler);

                    pipeline.addLast("idle", new ClientIdleCheckHandler());

                    pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));

                    pipeline.addLast("frameDecoder", new OrderFrameDecoder());
                    pipeline.addLast("frameEncoder", new OrderFrameEncoder());

                    pipeline.addLast("protocolDecoder", new OrderProtocolDecoder());
                    pipeline.addLast("protocolEncoder", new OrderProtocolEncoder());

                    pipeline.addLast("infoLog", infoHandler);
                    pipeline.addLast(clientKeepaliveHandler);
                }
            });

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);
            channelFuture.sync();

            // auth
            AuthOperation authOperation = new AuthOperation("admin", "password");
            channelFuture.channel().writeAndFlush(new RequestMessage(UUID.randomUUID().getLeastSignificantBits(), authOperation));
            // request
            RequestMessage requestMessage = new RequestMessage(UUID.randomUUID().getLeastSignificantBits(), new OrderOperation(1001, "test0"));
            channelFuture.channel().writeAndFlush(requestMessage);

            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

    }

}
