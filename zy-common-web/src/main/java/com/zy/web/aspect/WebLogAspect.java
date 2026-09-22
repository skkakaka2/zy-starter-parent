package com.zy.web.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 访问日志切面：记录 Controller 的 URI、耗时，慢请求打 WARN。
 *
 * <p>只记录成功请求，异常统一交给 {@code GlobalExceptionHandler} 打印，避免日志重复。
 */
@Slf4j
@Aspect
public class WebLogAspect {

    /** 慢请求阈值（毫秒） */
    private final long slowThresholdMs;

    public WebLogAspect(long slowThresholdMs) {
        this.slowThresholdMs = slowThresholdMs;
    }

    /**
     * 切所有标注了 @RestController / @Controller 的类。
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) "
            + "|| within(@org.springframework.stereotype.Controller *)")
    public void controllerMethods() {
        // 仅作为切点签名
    }

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;

        String uri = currentUri();
        String method = joinPoint.getSignature().toShortString();
        if (cost >= slowThresholdMs) {
            log.warn("慢请求 uri={}, method={}, cost={}ms", uri, method, cost);
        } else {
            log.info("请求完成 uri={}, method={}, cost={}ms", uri, method, cost);
        }
        return result;
    }

    private String currentUri() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String queryString = request.getQueryString();
            return queryString == null ? request.getRequestURI() : request.getRequestURI() + "?" + queryString;
        }
        return "-";
    }
}
