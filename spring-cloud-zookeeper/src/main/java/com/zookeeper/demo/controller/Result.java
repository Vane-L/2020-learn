package com.zookeeper.demo.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @Author: wenhongliang
 */
@Data
public class Result<T> {

    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MSG = "success";

    private int code;

    private String msg;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    private Result() {
    }


    /**
     * 内部使用，用于构造成功的结果
     *
     * @param code
     * @param msg
     * @param data
     */
    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 快速创建成功结果并返回结果数据
     *
     * @param data
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    /**
     * 快速创建成功结果
     *
     * @return Result
     */
    public static <T> Result<T> success() {
        return success(null);
    }


    /**
     * @param code
     * @param msg
     * @return
     */
    public static <T> Result<T> fail(int code, String msg) {
        return fail(code, msg, null);
    }

    /**
     * @param code
     * @param msg
     * @return
     */
    public static <T> Result<T> fail(int code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    /**
     * 成功code=000000
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isSuccess() {
        return SUCCESS_CODE == this.code;
    }

    /**
     * 失败
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isFail() {
        return !isSuccess();
    }

}
