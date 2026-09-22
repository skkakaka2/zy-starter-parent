package com.zy.feign.autoconfigure;

import java.util.concurrent.TimeUnit;

import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.zy.feign.decoder.FeignErrorDecoder;
import com.zy.feign.interceptor.FeignRequestInterceptor;

/**
 * zy OpenFeign 通用组件自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(Feign.class)
public class FeignAutoConfiguration {

    /**
     * 注册 Servlet 请求头透传拦截器。
     */
    @Bean
    @ConditionalOnMissingBean(FeignRequestInterceptor.class)
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }

    /**
     * 注册不泄露远程响应详情的错误解码器。
     */
    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    public ErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * 注册 2 秒连接、5 秒读取超时的请求选项。
     */
    @Bean
    @ConditionalOnMissingBean(Request.Options.class)
    public Request.Options feignRequestOptions() {
        return new Request.Options(2_000, TimeUnit.MILLISECONDS, 5_000, TimeUnit.MILLISECONDS, true);
    }

    /**
     * 注册显式禁用重试的 Feign 重试器。
     */
    @Bean
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    /**
     * 注册 BASIC 级别的 Feign 日志配置。
     */
    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
