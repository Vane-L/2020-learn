package com.netty.demo.message;

import lombok.Data;

@Data
public class MessageHeader {

    private int version = 1;
    private int opCode;
    private long streamId;

}
