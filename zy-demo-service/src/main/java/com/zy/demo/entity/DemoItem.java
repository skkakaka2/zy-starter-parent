package com.zy.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.mybatisplus.entity.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * H2 分页示例数据实体。
 */
@Getter
@Setter
@TableName("demo_item")
public class DemoItem extends BaseEntity {

    /** 示例名称。 */
    private String name;
}
