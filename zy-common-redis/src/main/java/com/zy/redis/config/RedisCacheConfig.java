package com.zy.redis.config;

import com.zy.redis.props.RedisProperties;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.util.StringUtils;

/**
 * Redis 缓存管理器构建定制。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisCacheManagerBuilderCustomizer.class)
public class RedisCacheConfig {

    /**
     * 设置缓存过期时间和隔离前缀，不直接创建 CacheManager。
     */
    @Bean(name = "zyRedisCacheManagerBuilderCustomizer")
    @ConditionalOnMissingBean(name = "zyRedisCacheManagerBuilderCustomizer")
    public RedisCacheManagerBuilderCustomizer zyRedisCacheManagerBuilderCustomizer(
            RedisProperties properties,
            Environment environment) {
        String prefix = resolveCachePrefix(properties, environment);
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.getCacheTtl())
                .computePrefixWith(cacheName -> prefix + cacheName + "::");
        return builder -> builder.cacheDefaults(cacheConfiguration);
    }

    private String resolveCachePrefix(RedisProperties properties, Environment environment) {
        if (StringUtils.hasText(properties.getCachePrefix())) {
            return properties.getCachePrefix();
        }
        String applicationName = environment.getProperty("spring.application.name", "zy");
        return (StringUtils.hasText(applicationName) ? applicationName : "zy") + ":cache:";
    }
}
