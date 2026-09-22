package com.zy.web.config;

import com.zy.web.aspect.WebLogAspect;
import com.zy.web.props.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 访问日志切面注册（需要 spring-boot-starter-aop 与 AopAutoConfiguration 自动代理）。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "zy.web.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebLogConfig {

    @Bean
    @ConditionalOnMissingBean(WebLogAspect.class)
    public WebLogAspect webLogAspect(WebProperties webProperties) {
        return new WebLogAspect(webProperties.getLog().getSlowThresholdMs());
    }
}
