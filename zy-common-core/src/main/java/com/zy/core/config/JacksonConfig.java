package com.zy.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 全局配置工厂。
 *
 * <p>无 Spring 依赖，由 zy-common-web 在 Step 3 以 Spring 配置类接入。
 */
public final class JacksonConfig {

    /** 日期时间格式 */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 时区 */
    public static final String TIME_ZONE = "Asia/Shanghai";

    private JacksonConfig() {
    }

    /**
     * 创建预配置的 ObjectMapper：LocalDateTime 按 yyyy-MM-dd HH:mm:ss、
     * Long（含 long）转字符串防前端精度丢失、忽略未知字段。
     */
    public static ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(javaTimeModule());
        mapper.registerModule(longToStringModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        mapper.setDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN));
        return mapper;
    }

    /**
     * LocalDateTime 序列化/反序列化模块（yyyy-MM-dd HH:mm:ss）。
     *
     * <p>Step 3 由 zy-common-web 通过 Jackson2ObjectMapperBuilderCustomizer 复用，避免配置重复。
     */
    public static JavaTimeModule javaTimeModule() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(new LocalDateTimeSerializer(formatter));
        module.addDeserializer(java.time.LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
        return module;
    }

    /**
     * Long / long 转字符串模块，防止前端 JS number 精度丢失。
     */
    public static SimpleModule longToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return module;
    }
}
