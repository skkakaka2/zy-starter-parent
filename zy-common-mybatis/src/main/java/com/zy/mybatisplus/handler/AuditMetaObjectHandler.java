package com.zy.mybatisplus.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zy.mybatisplus.audit.AuditUserContext;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import org.apache.ibatis.reflection.MetaObject;

/**
 * 为实体审计字段提供自动填充。
 */
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;

    /**
     * 使用指定时钟创建审计填充器。
     *
     * @param clock 审计时间来源
     */
    public AuditMetaObjectHandler(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * 使用上海时区系统时钟创建审计填充器。
     *
     * @return 默认审计填充器
     */
    public static AuditMetaObjectHandler systemDefault() {
        return new AuditMetaObjectHandler(Clock.system(ZoneId.of("Asia/Shanghai")));
    }

    /**
     * 填充新增记录的创建与更新审计字段。
     *
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = AuditUserContext.getUserId().orElse(AuditUserContext.SYSTEM_USER_ID);
        LocalDateTime now = LocalDateTime.now(clock);
        strictInsertFill(metaObject, "createBy", Long.class, userId);
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateBy", Long.class, userId);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 填充更新记录的更新审计字段。
     *
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = AuditUserContext.getUserId().orElse(AuditUserContext.SYSTEM_USER_ID);
        LocalDateTime now = LocalDateTime.now(clock);
        // 已查询的实体通常带有非空 updateBy/updateTime；strictUpdateFill 不会覆盖它们。
        // 审计字段必须反映本次实际更新操作，因此由服务端强制回填。
        setFieldValByName("updateBy", userId, metaObject);
        setFieldValByName("updateTime", now, metaObject);
    }
}
