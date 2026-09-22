package com.zy.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.zy.demo.entity.DemoItem;
import com.zy.mybatisplus.mapper.BaseMapperPlus;

/**
 * 示例数据 Mapper。
 */
@Mapper
public interface DemoItemMapper extends BaseMapperPlus<DemoItem> {
}
