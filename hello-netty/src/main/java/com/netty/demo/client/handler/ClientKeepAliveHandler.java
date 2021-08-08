package com.netty.demo.client.handler;

import com.netty.demo.message.RequestMessage;
import com.netty.demo.common.keepalive.KeepAliveOperation;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@ChannelHandler.Sharable
public class ClientKeepAliveHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT) {
            log.info("write idle happen. so need to send keep alive to keep connection not closed by server");
            KeepAliveOperation keepaliveOperation = new KeepAliveOperation();
            RequestMessage requestMessage = new RequestMessage(UUID.randomUUID().getLeastSignificantBits(), keepaliveOperation);
            ctx.writeAndFlush(requestMessage);
        }
        super.userEventTriggered(ctx, evt);
    }
}
