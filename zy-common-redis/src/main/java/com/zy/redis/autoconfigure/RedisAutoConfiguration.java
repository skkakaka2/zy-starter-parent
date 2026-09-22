package com.zy.redis.autoconfigure;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.zy.redis.config.RedisCacheConfig;
import com.zy.redis.config.RedisTemplateConfig;
import com.zy.redis.lock.DistributedLock;
import com.zy.redis.props.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * zy Redis 通用组件自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory.class)
@EnableConfigurationProperties(RedisProperties.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore(
        org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@Import({RedisTemplateConfig.class, RedisCacheConfig.class})
public class RedisAutoConfiguration {

    /**
     * 在 Redisson 客户端存在时注册分布式锁执行器。
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean
    public DistributedLock distributedLock(
            RedissonClient redissonClient,
            RedisProperties properties,
            Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "zy");
        return new DistributedLock(redissonClient, properties,
                StringUtils.hasText(applicationName) ? applicationName : "zy");
    }
}
