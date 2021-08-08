package com.netty.demo.common.keepalive;

import com.netty.demo.common.OperationResult;
import lombok.Data;

@Data
public class KeepAliveOperationResult extends OperationResult {

    private final long time;

}
