package com.zy.security.jwt;

import io.jsonwebtoken.io.Decoders;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 在应用启动时校验 JWT 必需配置，即使应用覆盖了 {@link JwtTokenService} 也不会跳过密钥校验。
 */
public final class JwtConfigurationValidator {

    private static final int HS256_MINIMUM_KEY_BYTES = 32;

    public JwtConfigurationValidator(JwtProperties properties) {
        validate(properties);
    }

    /**
     * 校验配置并返回 Base64URL 解码后的密钥材料。
     */
    public static byte[] decodedSecret(JwtProperties properties) {
        validateIssuerAndExpiration(properties);
        String encodedSecret = properties.getSecret();
        if (!StringUtils.hasText(encodedSecret)) {
            throw new IllegalStateException("zy.security.jwt.secret must be configured as a Base64URL value with at least 32 decoded bytes");
        }

        byte[] decodedSecret;
        try {
            decodedSecret = Decoders.BASE64URL.decode(encodedSecret);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("zy.security.jwt.secret must be a valid Base64URL value", ex);
        }
        if (decodedSecret.length < HS256_MINIMUM_KEY_BYTES) {
            throw new IllegalStateException("zy.security.jwt.secret must contain at least 32 decoded bytes for HS256");
        }
        return decodedSecret;
    }

    private static void validate(JwtProperties properties) {
        decodedSecret(properties);
    }

    private static void validateIssuerAndExpiration(JwtProperties properties) {
        if (!StringUtils.hasText(properties.getIssuer())) {
            throw new IllegalStateException("zy.security.jwt.issuer must not be blank");
        }
        Duration expiration = properties.getExpiration();
        if (expiration == null || expiration.isZero() || expiration.isNegative()) {
            throw new IllegalStateException("zy.security.jwt.expiration must be a positive duration");
        }
    }
}
