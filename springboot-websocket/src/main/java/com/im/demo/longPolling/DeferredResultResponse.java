package com.im.demo.longPolling;

import lombok.Data;
import lombok.Getter;

/**
 * @Author: wenhongliang
 */
@Data
public class DeferredResultResponse {
    private Integer code;
    private String msg;

    public enum Msg {
        TIMEOUT("超时"),
        FAILED("失败"),
        SUCCESS("成功");

        @Getter
        private String desc;

        Msg(String desc) {
            this.desc = desc;
        }
    }
}
