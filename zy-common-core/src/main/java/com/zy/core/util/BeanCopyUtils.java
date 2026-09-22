package com.zy.core.util;

import cn.hutool.core.bean.BeanUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Bean 拷贝工具（基于 Hutool）。
 */
public final class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    /**
     * 单对象转换，source 为 null 时返回 null。
     */
    public static <T> T toBean(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        return BeanUtil.toBean(source, clazz);
    }

    /**
     * 属性复制，任一为空时直接返回。
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target);
    }

    /**
     * 集合转换，空输入返回空 List（非 null）。
     */
    public static <T> List<T> toList(Collection<?> source, Class<T> clazz) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(source, clazz);
    }
}
