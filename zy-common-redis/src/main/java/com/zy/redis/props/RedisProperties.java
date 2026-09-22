package com.zy.redis.props;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * zy Redis 通用组件配置。
 */
@Getter
@Setter
@ConfigurationProperties("zy.redis")
public class RedisProperties {

    /** 默认缓存存活时间。 */
    private Duration cacheTtl = Duration.ofMinutes(10);

    /** 缓存键前缀；为空时使用应用名称。 */
    private String cachePrefix = "";

    /** 分布式锁键前缀；为空时使用应用名称。 */
    private String lockPrefix = "";

    /** 获取锁的最长等待时间。 */
    private Duration lockWaitTime = Duration.ofSeconds(2);

    /** 获取锁后的自动释放时间。 */
    private Duration lockLeaseTime = Duration.ofSeconds(30);
}
