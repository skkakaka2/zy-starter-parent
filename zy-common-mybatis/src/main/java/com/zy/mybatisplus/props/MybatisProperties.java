package com.zy.mybatisplus.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zy MyBatis-Plus 公共配置项。
 */
@Getter
@Setter
@ConfigurationProperties("zy.mybatis")
public class MybatisProperties {

    /** 分页单页最大记录数。 */
    private Long paginationMaxLimit = 500L;

    /** 页码越界时是否回到首页。 */
    private boolean paginationOverflow = false;
}
