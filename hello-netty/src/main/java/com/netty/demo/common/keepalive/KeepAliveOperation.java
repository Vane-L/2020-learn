package com.netty.demo.common.keepalive;


import com.netty.demo.common.Operation;
import lombok.Data;

@Data
public class KeepAliveOperation extends Operation {

    private long time;

    public KeepAliveOperation() {
        this.time = System.nanoTime();
    }

    @Override
    public KeepAliveOperationResult execute() {
        return new KeepAliveOperationResult(time);
    }
}
