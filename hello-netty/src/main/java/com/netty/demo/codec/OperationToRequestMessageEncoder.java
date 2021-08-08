package com.netty.demo.codec;

import com.netty.demo.common.Operation;
import com.netty.demo.message.RequestMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.UUID;

public class OperationToRequestMessageEncoder extends MessageToMessageEncoder<Operation> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Operation operation, List<Object> out) throws Exception {
        RequestMessage requestMessage = new RequestMessage(UUID.randomUUID().getLeastSignificantBits(), operation);
        out.add(requestMessage);
    }
}
