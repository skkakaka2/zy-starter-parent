package com.zy.web.config;

import com.zy.web.filter.TraceIdFilter;
import com.zy.web.props.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * TraceId 过滤器注册：显式声明顺序，保证早于业务 Filter 与 Spring Security。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "zy.web.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceIdConfig {

    @Bean
    @ConditionalOnMissingBean(TraceIdFilter.class)
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter(WebProperties webProperties) {
        FilterRegistrationBean<TraceIdFilter> registration =
                new FilterRegistrationBean<>(new TraceIdFilter(webProperties.getTrace().getHeaderName()));
        registration.setName("traceIdFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
