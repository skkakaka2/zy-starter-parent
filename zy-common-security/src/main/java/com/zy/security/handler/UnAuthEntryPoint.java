package com.zy.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.core.constant.HeaderConstants;
import com.zy.core.exception.ErrorCode;
import com.zy.core.result.Result;
import com.zy.web.trace.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未认证响应入口，直接写 JSON，避免进入 MVC 异常处理链。
 */
public class UnAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public UnAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {
        write(response, request, ErrorCode.UNAUTHORIZED);
    }

    private void write(HttpServletResponse response, HttpServletRequest request, ErrorCode errorCode) throws IOException {
        Result<Void> result = Result.fail(errorCode);
        String traceId = TraceIdContext.getTraceId();
        if (traceId == null) {
            traceId = request.getHeader(HeaderConstants.TRACE_ID);
        }
        result.setTraceId(traceId);
        response.setStatus(errorCode.getHttpStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), result);
    }
}
