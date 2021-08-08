package com.netty.demo.message;

import com.netty.demo.common.OperationResult;
import com.netty.demo.common.OperationType;

public class ResponseMessage extends Message<OperationResult> {
    @Override
    public Class getMessageBodyDecodeClass(int opcode) {
        return OperationType.fromOpCode(opcode).getOperationResultClazz();
    }
}
