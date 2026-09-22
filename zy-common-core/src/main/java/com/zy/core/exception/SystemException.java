package com.zy.core.exception;

import lombok.Getter;

/**
 * 系统异常，用于包装非预期的底层错误。
 */
@Getter
public class SystemException extends RuntimeException {

    /** 业务码 */
    private final int code;

    public SystemException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.INTERNAL_ERROR.getCode();
    }
}
