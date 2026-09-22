package com.zy.web.config;

import com.zy.web.props.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 配置：以 CorsFilter 形式注册，兼容后续接入 Spring Security（Filter 链优先级明确）。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "zy.web.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CorsConfig {

    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    public CorsConfigurationSource corsConfigurationSource(WebProperties webProperties) {
        WebProperties.Cors cors = webProperties.getCors();
        CorsConfiguration config = new CorsConfiguration();
        // 用 allowedOriginPatterns：credentials + "*" 才合法
        config.setAllowedOriginPatterns(cors.getAllowedOrigins());
        config.setAllowedHeaders(cors.getAllowedHeaders());
        config.setAllowedMethods(cors.getAllowedMethods());
        config.setExposedHeaders(cors.getExposedHeaders());
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setMaxAge(cors.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }
}
