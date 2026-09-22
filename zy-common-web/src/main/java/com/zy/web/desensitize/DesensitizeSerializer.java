package com.zy.web.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 脱敏序列化器：读取字段上的 {@link Desensitize} 注解，输出掩码后的字符串。
 *
 * <p>实现 ContextualSerializer：只在被注解标注的属性上生效，其余 String 走默认序列化。
 */
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private DesensitizeType type = DesensitizeType.CUSTOM;

    private int prefixKeep;

    private int suffixKeep;

    private String mask = "*";

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(desensitize(value));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) {
        if (property == null) {
            return this;
        }
        Desensitize annotation = property.getAnnotation(Desensitize.class);
        if (annotation == null) {
            return this;
        }
        DesensitizeSerializer serializer = new DesensitizeSerializer();
        serializer.type = annotation.value();
        serializer.prefixKeep = annotation.prefixKeep();
        serializer.suffixKeep = annotation.suffixKeep();
        serializer.mask = annotation.mask();
        return serializer;
    }

    /**
     * 按类型脱敏。
     */
    private String desensitize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        switch (type) {
            case NONE:
                return value;
            case NAME:
                return maskRange(value, Math.min(1, value.length()), 0);
            case PHONE:
                return maskRange(value, 3, 4);
            case ID_CARD:
                return maskRange(value, 6, 4);
            case EMAIL:
                return maskEmail(value);
            case BANK_CARD:
                return maskRange(value, 0, 4);
            case ADDRESS:
                return maskRange(value, 6, 0);
            case PASSWORD:
                return "******";
            case CUSTOM:
            default:
                return maskRange(value, prefixKeep, suffixKeep);
        }
    }

    /**
     * 保留首尾，中间用掩码替换；长度不足时全部掩码。
     */
    private String maskRange(String value, int prefixKeep, int suffixKeep) {
        int length = value.length();
        int keep = prefixKeep + suffixKeep;
        if (keep >= length) {
            return repeat(mask, length);
        }
        return value.substring(0, prefixKeep)
                + repeat(mask, length - keep)
                + value.substring(length - suffixKeep);
    }

    private String maskEmail(String value) {
        int at = value.indexOf('@');
        if (at <= 0) {
            return maskRange(value, 1, 0);
        }
        String local = value.substring(0, at);
        String domain = value.substring(at);
        return local.substring(0, 1) + "***" + domain;
    }

    private String repeat(String mask, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(mask);
        }
        return sb.toString();
    }
}
