package com.zy.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.core.constant.HeaderConstants;
import com.zy.core.exception.ErrorCode;
import com.zy.core.result.Result;
import com.zy.web.trace.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 已认证但无权限时的 JSON 响应处理器，直接写响应而不暴露授权异常详情。
 */
public class UnAuthAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public UnAuthAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        Result<Void> result = Result.fail(ErrorCode.FORBIDDEN);
        String traceId = TraceIdContext.getTraceId();
        if (traceId == null) {
            traceId = request.getHeader(HeaderConstants.TRACE_ID);
        }
        result.setTraceId(traceId);
        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), result);
    }
}
