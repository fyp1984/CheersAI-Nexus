package com.cheersai.nexus.user.common.api;

import lombok.Data;

/**
 * 统一结果响应
 */
@Data
public class Result<T> {

    //状态码
    private Integer code;

    //消息
    private String message;

    //数据
    private T data;

    //成功
    public static <T> Result<T> success() {
        return success(null);
    }

    //成功（携带数据）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    //失败
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    //失败（携带状态码）
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
