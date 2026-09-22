package com.zy.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据库实体公共审计字段。
 */
@Getter
@Setter
public class BaseEntity {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    protected Long id;

    /** 创建人。 */
    @TableField(fill = FieldFill.INSERT)
    protected Long createBy;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    /** 更新人。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected Long updateBy;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    /** 逻辑删除标记。 */
    @TableLogic
    protected Integer deleted;

    /** 乐观锁版本号。 */
    @Version
    protected Integer version;
}
