package com.netty.demo.common.auth;


import com.netty.demo.common.Operation;
import lombok.Data;
import lombok.extern.java.Log;

@Data
@Log
public class AuthOperation extends Operation {

    private final String userName;
    private final String password;

    @Override
    public AuthOperationResult execute() {
        if ("admin".equalsIgnoreCase(this.userName)) {
            return new AuthOperationResult(true);
        }

        return new AuthOperationResult(false);
    }
}
