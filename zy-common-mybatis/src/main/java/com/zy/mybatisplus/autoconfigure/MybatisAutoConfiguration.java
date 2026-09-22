package com.zy.mybatisplus.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.zy.mybatisplus.config.MybatisPlusConfig;
import com.zy.mybatisplus.props.MybatisProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * zy MyBatis-Plus 公共能力自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@EnableConfigurationProperties(MybatisProperties.class)
@Import(MybatisPlusConfig.class)
public class MybatisAutoConfiguration {
}
