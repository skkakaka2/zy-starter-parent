package com.zy.redis.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.zy.redis.lock.DistributedLock;
import com.zy.redis.props.RedisProperties;

/**
 * Redis 自动配置的纯 Mock 上下文测试。
 */
class RedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class))
            .withUserConfiguration(RedisDependencies.class);

    @Test
    void registersTemplateLockAndBindsPropertiesWithoutConnectingToRedis() {
        contextRunner.withPropertyValues(
                "spring.application.name=payment",
                "zy.redis.cache-ttl=15m",
                "zy.redis.cache-prefix=payment:cache:",
                "zy.redis.lock-prefix=payment:mutex:",
                "zy.redis.lock-wait-time=5s",
                "zy.redis.lock-lease-time=1m")
                .run(context -> {
                    assertThat(context).hasBean("redisTemplate");
                    assertThat(context).hasSingleBean(DistributedLock.class);

                    RedisTemplate<?, ?> redisTemplate = context.getBean("redisTemplate", RedisTemplate.class);
                    assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
                    assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(StringRedisSerializer.class);
                    assertThat(redisTemplate.getValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class)
                            .isNotInstanceOf(JdkSerializationRedisSerializer.class);
                    assertThat(redisTemplate.getHashValueSerializer()).isInstanceOf(GenericJackson2JsonRedisSerializer.class);

                    @SuppressWarnings("unchecked")
                    RedisSerializer<Object> serializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
                    CacheValue expected = new CacheValue(9007199254741993L,
                            LocalDateTime.of(2026, 9, 14, 16, 45));
                    assertThat(serializer.deserialize(serializer.serialize(expected))).isEqualTo(expected);

                    RedisProperties properties = context.getBean(RedisProperties.class);
                    assertThat(properties.getCacheTtl()).isEqualTo(Duration.ofMinutes(15));
                    assertThat(properties.getCachePrefix()).isEqualTo("payment:cache:");
                    assertThat(properties.getLockPrefix()).isEqualTo("payment:mutex:");
                    assertThat(properties.getLockWaitTime()).isEqualTo(Duration.ofSeconds(5));
                    assertThat(properties.getLockLeaseTime()).isEqualTo(Duration.ofMinutes(1));
                });
    }

    @Test
    void backsOffWhenUserDefinesStandardRedisTemplate() {
        contextRunner.withUserConfiguration(UserRedisTemplate.class)
                .run(context -> {
                    assertThat(context).hasBean("redisTemplate");
                    assertThat(context.getBeansOfType(RedisTemplate.class)).hasSize(1);
                    assertThat(context.getBean("redisTemplate", RedisTemplate.class).getKeySerializer())
                            .isInstanceOf(StringRedisSerializer.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class RedisDependencies {

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }

        @Bean
        RedissonClient redissonClient() {
            return mock(RedissonClient.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserRedisTemplate {

        @Bean(name = "redisTemplate")
        RedisTemplate<String, Object> userRedisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(connectionFactory);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return redisTemplate;
        }
    }

    record CacheValue(Long id, LocalDateTime createdAt) {
    }
}
