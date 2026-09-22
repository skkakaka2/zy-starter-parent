package com.zy.mybatisplus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zy.mybatisplus.handler.AuditMetaObjectHandler;
import com.zy.mybatisplus.props.MybatisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 公共插件配置。
 */
@Configuration(proxyBeanMethods = false)
public class MybatisPlusConfig {

    /**
     * 注册乐观锁与分页拦截器。
     *
     * @param properties 分页配置
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MybatisProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        pagination.setMaxLimit(properties.getPaginationMaxLimit());
        pagination.setOverflow(properties.isPaginationOverflow());
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }

    /**
     * 注册默认审计字段填充器。
     *
     * @return 审计字段填充器
     */
    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public MetaObjectHandler auditMetaObjectHandler() {
        return AuditMetaObjectHandler.systemDefault();
    }
}
