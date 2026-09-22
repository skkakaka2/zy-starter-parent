package com.zy.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zy.core.exception.ErrorCode;
import lombok.Data;

/**
 * 统一返回体。
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 业务码，成功固定 200 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 链路追踪 ID，为空时不序列化 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String traceId;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(ErrorCode.SUCCESS.getCode());
        r.setMessage(ErrorCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(ErrorCode.BIZ_ERROR.getCode(), message);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
