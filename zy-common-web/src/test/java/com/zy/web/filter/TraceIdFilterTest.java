package com.zy.web.filter;

import com.zy.core.constant.HeaderConstants;
import com.zy.web.trace.TraceIdContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TraceId 过滤器：生成/透传 + 写入 MDC + 响应头 + 请求结束清理。
 */
class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter(HeaderConstants.TRACE_ID);

    @Test
    void shouldGenerateWhenHeaderAbsentAndCleanMdcAfter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(HeaderConstants.TRACE_ID)).isNotBlank();
        assertThat(TraceIdContext.getTraceId()).isNull();
    }

    @Test
    void shouldReuseUpstreamTraceIdAndPutIntoMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/demo");
        request.addHeader(HeaderConstants.TRACE_ID, "upstream-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res) {
                assertThat(TraceIdContext.getTraceId()).isEqualTo("upstream-001");
            }
        });

        assertThat(response.getHeader(HeaderConstants.TRACE_ID)).isEqualTo("upstream-001");
        assertThat(TraceIdContext.getTraceId()).isNull();
    }
}
