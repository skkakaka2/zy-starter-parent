package com.zy.feign.interceptor;

import java.util.Map;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.zy.core.constant.HeaderConstants;

/**
 * 将当前 Servlet 请求中的公共请求头透传到 Feign 请求。
 */
public final class FeignRequestInterceptor implements RequestInterceptor {

    /**
     * 透传认证、链路和租户请求头；非 Servlet 请求不作处理。
     */
    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        propagateHeader(template, request, HeaderConstants.AUTHORIZATION);
        propagateHeader(template, request, HeaderConstants.TRACE_ID);
        propagateHeader(template, request, HeaderConstants.TENANT_ID);
    }

    private void propagateHeader(RequestTemplate template, HttpServletRequest request, String headerName) {
        if (hasHeader(template, headerName)) {
            return;
        }
        String headerValue = request.getHeader(headerName);
        if (StringUtils.hasText(headerValue)) {
            template.header(headerName, headerValue);
        }
    }

    private boolean hasHeader(RequestTemplate template, String headerName) {
        for (Map.Entry<String, java.util.Collection<String>> header : template.headers().entrySet()) {
            if (header.getKey().equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }
}
