package com.netty.demo.common.auth;

import com.netty.demo.common.OperationResult;
import lombok.Data;

@Data
public class AuthOperationResult extends OperationResult {

    private final boolean passAuth;

}
