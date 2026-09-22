package com.zy.feign.decoder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.zy.core.exception.BusinessException;
import com.zy.core.exception.ErrorCode;

/**
 * {@link FeignErrorDecoder} 的安全错误映射测试。
 */
class FeignErrorDecoderTest {

    @Test
    void returnsSafeBusinessExceptionWithoutUsingRemoteResponseDetails() {
        Exception exception = new FeignErrorDecoder().decode("sensitive#method()", null);

        assertThat(exception).isInstanceOf(BusinessException.class);
        BusinessException businessException = (BusinessException) exception;
        assertThat(businessException.getCode()).isEqualTo(ErrorCode.BIZ_ERROR.getCode());
        assertThat(businessException).hasMessage("远程服务调用失败");
    }
}
