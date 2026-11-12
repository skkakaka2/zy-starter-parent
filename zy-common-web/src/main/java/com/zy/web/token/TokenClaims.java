package com.zy.web.token;

import lombok.Data;

/**
 * JWT payload 字段名常量。
 */
@Data 
public final class TokenClaims {

    /** 主题，当前用于标识用户 */
    public static final String USER_ID = null;

    /** 过期时间，Unix 时间戳（秒） */
    public static final String EXP = "exp";

}
