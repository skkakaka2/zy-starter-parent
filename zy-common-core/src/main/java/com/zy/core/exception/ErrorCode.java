package com.zy.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码：业务码 + HTTP 状态 + 默认提示。
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, 200, "操作成功"),
    BAD_REQUEST(400, 400, "请求参数错误"),
    UNAUTHORIZED(401, 401, "未认证"),
    FORBIDDEN(403, 403, "无权限"),
    NOT_FOUND(404, 404, "资源不存在"),
    CONFLICT(409, 409, "数据冲突，请勿重复提交"),
    TOO_MANY_REQUESTS(429, 429, "请求过于频繁"),
    PARAM_VALID_ERROR(40001, 400, "参数校验失败"),
    BIZ_ERROR(50001, 500, "业务处理失败"),
    INTERNAL_ERROR(500, 500, "系统繁忙，请稍后重试");

    /** 业务码 */
    private final int code;

    /** 对应的 HTTP 状态 */
    private final int httpStatus;

    /** 默认提示信息 */
    private final String message;
}
