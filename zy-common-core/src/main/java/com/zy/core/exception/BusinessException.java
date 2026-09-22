package com.zy.core.exception;

import lombok.Getter;

/**
 * 业务异常，可直接转换为 Result。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务码 */
    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
