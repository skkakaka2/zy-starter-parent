package com.zy.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 配置。启用 JWT 时必须由部署环境提供 Base64URL 编码的 HMAC 密钥。
 */
@Data
@ConfigurationProperties(prefix = JwtProperties.PREFIX)
public class JwtProperties {

    public static final String PREFIX = "zy.security.jwt";

    /** 是否启用 JWT 安全自动配置。 */
    private boolean enabled = true;

    /**
     * Base64URL 编码的 HMAC 密钥。该项没有默认值，避免使用可预测或自动生成的密钥。
     */
    private String secret;

    /** JWT 签发方。 */
    private String issuer = "zy-starter";

    /** Token 有效期。 */
    private Duration expiration = Duration.ofHours(2);

    /** 无需认证的 Ant 风格路径。 */
    private List<String> permitPaths = new ArrayList<>(List.of(
            "/auth/**",
            "/login",
            "/register",
            "/error",
            "/actuator/health",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**"
    ));
}
