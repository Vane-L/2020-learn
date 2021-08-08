package com.netty.demo.client;

import com.netty.demo.client.handler.RequestPendingCenter;
import com.netty.demo.client.handler.ResponseDispatcherHandler;
import com.netty.demo.codec.OperationToRequestMessageEncoder;
import com.netty.demo.codec.OrderFrameDecoder;
import com.netty.demo.codec.OrderFrameEncoder;
import com.netty.demo.codec.OrderProtocolDecoder;
import com.netty.demo.codec.OrderProtocolEncoder;
import com.netty.demo.common.OperationResult;
import com.netty.demo.message.RequestMessage;
import com.netty.demo.common.order.OrderOperation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.internal.UnstableApi;

import javax.net.ssl.SSLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * This class hadn't add auth or do other improvements. so need to refer {@link ClientV0}
 */
@UnstableApi
public class ClientV2 {

    public static void main(String[] args) throws InterruptedException, ExecutionException, SSLException {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);

        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group);

            RequestPendingCenter requestPendingCenter = new RequestPendingCenter();

            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            SslContext sslContext = sslContextBuilder.build();

            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    pipeline.addLast(sslContext.newHandler(ch.alloc()));

                    pipeline.addLast(new OrderFrameDecoder());
                    pipeline.addLast(new OrderFrameEncoder());
                    pipeline.addLast(new OrderProtocolEncoder());
                    pipeline.addLast(new OrderProtocolDecoder());

                    pipeline.addLast(new ResponseDispatcherHandler(requestPendingCenter));

                    pipeline.addLast(new OperationToRequestMessageEncoder());

                    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                }
            });

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);
            channelFuture.sync();

            long streamId = UUID.randomUUID().getLeastSignificantBits();

            RequestMessage requestMessage = new RequestMessage(streamId, new OrderOperation(1001, "test2"));

            RequestPendingCenter.OperationResultFuture operationResultFuture = new RequestPendingCenter.OperationResultFuture();
            requestPendingCenter.add(streamId, operationResultFuture);

            channelFuture.channel().writeAndFlush(requestMessage);

            OperationResult operationResult = operationResultFuture.get();
            System.out.println(operationResult);

            channelFuture.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }

    }

}
