package com.zy.feign.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import feign.RequestTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.zy.core.constant.HeaderConstants;

/**
 * {@link FeignRequestInterceptor} 的请求上下文测试。
 */
class FeignRequestInterceptorTest {

    private final FeignRequestInterceptor interceptor = new FeignRequestInterceptor();

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void propagatesServletHeadersWithoutOverwritingOutgoingHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderConstants.AUTHORIZATION, "Bearer inbound-token");
        request.addHeader(HeaderConstants.TRACE_ID, "trace-001");
        request.addHeader(HeaderConstants.TENANT_ID, "tenant-inbound");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();
        template.header(HeaderConstants.TENANT_ID, "tenant-outgoing");

        interceptor.apply(template);

        assertThat(template.headers().get(HeaderConstants.AUTHORIZATION)).containsExactly("Bearer inbound-token");
        assertThat(template.headers().get(HeaderConstants.TRACE_ID)).containsExactly("trace-001");
        assertThat(template.headers().get(HeaderConstants.TENANT_ID)).containsExactly("tenant-outgoing");
    }

    @Test
    void doesNothingWithoutServletRequest() {
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertThat(template.headers()).isEmpty();
    }
}
