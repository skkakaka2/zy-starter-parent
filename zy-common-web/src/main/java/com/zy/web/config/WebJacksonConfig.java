package com.zy.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zy.core.config.JacksonConfig;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;
import java.time.format.DateTimeFormatter;

/**
 * 把 core 的 Jackson 约定接入 Spring 管理的 ObjectMapper。
 *
 * <p>用 Customizer 而非直接替换 ObjectMapper Bean：不破坏 Boot 对 spring.jackson.* 的支持，
 * 也不会丢失 ParameterNamesModule 等内建模块。
 */
@Configuration(proxyBeanMethods = false)
public class WebJacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer zyJackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(JacksonConfig.DATE_TIME_PATTERN);
            // 不调用 builder.modules(...)：它会替换 Boot 自动发现的 Jackson 模块。
            builder.serializerByType(java.time.LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            builder.deserializerByType(java.time.LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            builder.timeZone(TimeZone.getTimeZone(JacksonConfig.TIME_ZONE));
            builder.simpleDateFormat(JacksonConfig.DATE_TIME_PATTERN);
        };
    }
}
