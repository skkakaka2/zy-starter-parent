package com.zy.feign.decoder;

import feign.Response;
import feign.codec.ErrorDecoder;

import com.zy.core.exception.BusinessException;
import com.zy.core.exception.ErrorCode;

/**
 * 将远程非成功响应统一转换为安全的业务异常。
 */
public final class FeignErrorDecoder implements ErrorDecoder {

    private static final String REMOTE_CALL_FAILURE_MESSAGE = "远程服务调用失败";

    /**
     * 返回不包含远程响应详情的业务异常。
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        return new BusinessException(ErrorCode.BIZ_ERROR, REMOTE_CALL_FAILURE_MESSAGE);
    }
}
