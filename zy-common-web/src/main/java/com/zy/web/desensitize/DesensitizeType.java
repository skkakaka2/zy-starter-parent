package com.zy.web.desensitize;

/**
 * 脱敏类型。
 */
public enum DesensitizeType {

    /** 不脱敏 */
    NONE,

    /** 姓名：保留姓，如 张三 → 张* */
    NAME,

    /** 手机号：138****8888 */
    PHONE,

    /** 身份证：前 6 后 4 */
    ID_CARD,

    /** 邮箱：本地部分保留首字符 */
    EMAIL,

    /** 银行卡：保留后 4 位 */
    BANK_CARD,

    /** 地址：保留前 6 位 */
    ADDRESS,

    /** 密码：固定 ****** */
    PASSWORD,

    /** 自定义：按 prefixKeep / suffixKeep 保留 */
    CUSTOM
}
