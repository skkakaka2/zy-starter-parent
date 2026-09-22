package com.zy.web.desensitize;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段脱敏注解：序列化时按规则掩码输出。
 *
 * <pre>{@code
 * @Desensitize(DesensitizeType.PHONE)
 * private String phone;
 *
 * @Desensitize(value = DesensitizeType.CUSTOM, prefixKeep = 3, suffixKeep = 4)
 * private String secret;
 * }</pre>
 *
 * <p>通过 {@code @JsonSerialize} 生效，任何 ObjectMapper 都会识别，无需额外注册模块。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏类型。
     */
    DesensitizeType value() default DesensitizeType.CUSTOM;

    /**
     * 前置保留字符数（仅 CUSTOM 生效）。
     */
    int prefixKeep() default 0;

    /**
     * 后置保留字符数（仅 CUSTOM 生效）。
     */
    int suffixKeep() default 0;

    /**
     * 掩码字符。
     */
    String mask() default "*";
}
