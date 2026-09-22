package com.zy.web.filter;

import cn.hutool.core.util.StrUtil;
import com.zy.web.trace.TraceIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceId 过滤器：优先透传上游 {@code X-Trace-Id}，没有则生成，写入 MDC 与响应头。
 *
 * <p>注意：Filter 用 jakarta.servlet 包（Boot 3），不要引 javax.servlet。
 */
public class TraceIdFilter extends OncePerRequestFilter {

    /** 请求/响应头名称 */
    private final String headerName;

    public TraceIdFilter(String headerName) {
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(headerName);
        if (StrUtil.isBlank(traceId)) {
            traceId = TraceIdContext.generateTraceId();
        }
        TraceIdContext.setTraceId(traceId);
        response.setHeader(headerName, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceIdContext.clear();
        }
    }
}
