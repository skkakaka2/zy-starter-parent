package com.zy.web.props;

import com.zy.core.constant.HeaderConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * zy-common-web 统一配置前缀：{@code zy.web.*}。
 *
 * <p>所有开关都有默认值，业务服务零配置即可生效，需要覆盖时只写差异项。
 */
@Data
@ConfigurationProperties(prefix = WebProperties.PREFIX)
public class WebProperties {

    public static final String PREFIX = "zy.web";

    /** CORS 跨域 */
    private Cors cors = new Cors();

    /** TraceId 链路追踪 */
    private Trace trace = new Trace();

    /** 访问日志切面 */
    private Log log = new Log();

    /** 参数校验 */
    private Validation validation = new Validation();

    /**
     * CORS 配置。
     *
     * <p>注意：使用 allowedOriginPatterns 而非 allowedOrigins，
     * 因为 allowCredentials=true 与 allowedOrigins=* 组合会抛异常。
     */
    @Data
    public static class Cors {

        /** 是否启用 */
        private boolean enabled = true;

        /** 允许的来源模式 */
        private List<String> allowedOrigins = List.of("*");

        /** 允许的请求头 */
        private List<String> allowedHeaders = List.of("*");

        /** 允许的方法 */
        private List<String> allowedMethods = List.of("*");

        /** 暴露给前端的响应头 */
        private List<String> exposedHeaders = List.of(HeaderConstants.TRACE_ID);

        /**
         * 是否允许浏览器携带 Cookie 等凭证；默认关闭，避免任意来源（*）跨域携带会话。
         *
         * <p>Bearer Token 可通过 Authorization 请求头发送，不依赖此开关。
         */
        private boolean allowCredentials;

        /** 预检请求缓存时间（秒） */
        private long maxAge = 3600L;
    }

    /**
     * TraceId 配置。
     */
    @Data
    public static class Trace {

        /** 是否启用 TraceIdFilter */
        private boolean enabled = true;

        /** 请求/响应头名称 */
        private String headerName = HeaderConstants.TRACE_ID;
    }

    /**
     * 访问日志配置。
     */
    @Data
    public static class Log {

        /** 是否启用 WebLogAspect */
        private boolean enabled = true;

        /** 慢请求阈值（毫秒），超过打 WARN */
        private long slowThresholdMs = 3000L;
    }

    /**
     * 校验器配置。
     */
    @Data
    public static class Validation {

        /** 快速失败：只返回第一个校验错误（默认 false，返回全部） */
        private boolean failFast = false;
    }
}
