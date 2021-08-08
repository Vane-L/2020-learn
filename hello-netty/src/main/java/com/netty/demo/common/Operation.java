package com.netty.demo.common;

import com.netty.demo.message.MessageBody;

public abstract class Operation extends MessageBody {

    public abstract OperationResult execute();

}
