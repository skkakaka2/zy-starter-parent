package com.zy.core.constant;

/**
 * 请求头常量。
 */
public final class HeaderConstants {

    /** 认证头 */
    public static final String AUTHORIZATION = "Authorization";

    /** 链路追踪 ID 头 */
    public static final String TRACE_ID = "X-Trace-Id";

    /** 租户 ID 头 */
    public static final String TENANT_ID = "X-Tenant-Id";

    private HeaderConstants() {
    }
}
