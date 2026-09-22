package com.zy.mybatisplus.audit;

import java.util.OptionalLong;

/**
 * 当前线程的审计用户上下文。
 */
public final class AuditUserContext {

    /** 未登录或系统任务使用的默认用户 ID。 */
    public static final long SYSTEM_USER_ID = 0L;

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private AuditUserContext() {
    }

    /**
     * 获取当前线程中的用户 ID。
     *
     * @return 当前用户 ID，不存在时为空
     */
    public static OptionalLong getUserId() {
        Long userId = USER_ID_HOLDER.get();
        return userId == null ? OptionalLong.empty() : OptionalLong.of(userId);
    }

    /**
     * 设置当前线程中的用户 ID。
     *
     * @param userId 当前用户 ID
     */
    public static void setUserId(long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 清理当前线程中的用户上下文。
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
