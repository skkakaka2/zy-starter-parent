package com.zy.web.trace;

import cn.hutool.core.util.IdUtil;
import com.zy.core.constant.TraceConstants;
import org.slf4j.MDC;

/**
 * TraceId 上下文：读写 MDC，供全局异常处理器回填 Result.traceId、日志框架输出 %X{traceId}。
 */
public final class TraceIdContext {

    private TraceIdContext() {
    }

    /**
     * 当前线程 TraceId，不存在返回 null。
     */
    public static String getTraceId() {
        return MDC.get(TraceConstants.MDC_KEY);
    }

    /**
     * 写入当前线程 TraceId。
     */
    public static void setTraceId(String traceId) {
        MDC.put(TraceConstants.MDC_KEY, traceId);
    }

    /**
     * 生成 TraceId（32 位无横线 UUID）。
     */
    public static String generateTraceId() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * 清理当前线程 TraceId，必须在请求结束时调用，防止线程复用串号。
     */
    public static void clear() {
        MDC.remove(TraceConstants.MDC_KEY);
    }
}
