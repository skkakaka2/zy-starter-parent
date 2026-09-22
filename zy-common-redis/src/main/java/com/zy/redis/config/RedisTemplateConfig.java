package com.zy.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.zy.core.config.JacksonConfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;

/**
 * RedisTemplate 序列化配置。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisTemplateConfig {

    /**
     * 注册标准 RedisTemplate，避免 JDK 序列化。
     *
     * <p>Object 多态 JSON 序列化只可用于可信的服务自有 Redis 数据，不能反序列化不可信输入。
     * 仅允许 {@code com.zy.*} 业务类型、时间类型、集合和 Map，禁止开放 Object 的任意子类型。
     */
    @Bean(name = "redisTemplate")
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(JacksonConfig::objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = GenericJackson2JsonRedisSerializer.builder()
                .objectMapper(redisObjectMapper(objectMapper))
                .typeHintPropertyName("@class")
                .build();

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @SuppressWarnings("deprecation") // DefaultTyping.EVERYTHING：见下方受限 PTV 与用途说明。
    private ObjectMapper redisObjectMapper(ObjectMapper projectObjectMapper) {
        ObjectMapper redisObjectMapper = projectObjectMapper.copy();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.zy.")
                .allowIfSubType(Temporal.class)
                // DefaultTyping.EVERYTHING 会为包装数值/布尔值也写入类型提示；它们是安全标量。
                .allowIfSubType(Number.class)
                .allowIfSubType(Boolean.class)
                .allowIfBaseType(Collection.class)
                .allowIfBaseType(Map.class)
                .build();
        // RedisTemplate 写入时会按实际 final 类序列化；只有 EVERYTHING 能保留根业务对象类型。
        // Jackson 2.21 将该枚举标记为弃用，但受限 PTV 是当前兼顾通用 Object 模板与安全的可用方案。
        redisObjectMapper.activateDefaultTyping(typeValidator, DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        return redisObjectMapper;
    }
}
