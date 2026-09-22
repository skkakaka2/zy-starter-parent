package com.zy.core.util;

import com.zy.core.exception.BusinessException;
import com.zy.core.exception.ErrorCode;

import java.util.Collection;

/**
 * 断言工具，失败时抛 BusinessException。
 */
public final class Assert {

    private Assert() {
    }

    public static void notNull(Object obj, ErrorCode errorCode) {
        notNull(obj, errorCode, errorCode.getMessage());
    }

    public static void notNull(Object obj, ErrorCode errorCode, String message) {
        if (obj == null) {
            throw new BusinessException(errorCode, message);
        }
    }

    public static void isTrue(boolean condition, ErrorCode errorCode) {
        isTrue(condition, errorCode, errorCode.getMessage());
    }

    public static void isTrue(boolean condition, ErrorCode errorCode, String message) {
        if (!condition) {
            throw new BusinessException(errorCode, message);
        }
    }

    public static void notBlank(String str, ErrorCode errorCode) {
        notBlank(str, errorCode, errorCode.getMessage());
    }

    public static void notBlank(String str, ErrorCode errorCode, String message) {
        if (str == null || str.isBlank()) {
            throw new BusinessException(errorCode, message);
        }
    }

    public static void notEmpty(Collection<?> collection, ErrorCode errorCode) {
        notEmpty(collection, errorCode, errorCode.getMessage());
    }

    public static void notEmpty(Collection<?> collection, ErrorCode errorCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(errorCode, message);
        }
    }
}
