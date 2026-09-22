package com.zy.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.mybatisplus.entity.BaseEntity;

/**
 * 项目统一 Mapper 基类。
 *
 * @param <T> 实体类型
 */
public interface BaseMapperPlus<T extends BaseEntity> extends BaseMapper<T> {
}
