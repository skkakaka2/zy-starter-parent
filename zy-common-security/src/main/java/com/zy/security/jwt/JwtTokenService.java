package com.zy.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 签发与校验服务。
 *
 * <p>所有解析均校验签名、过期时间和 issuer；调用方不得记录 token 或解析异常详情。
 */
public class JwtTokenService {

    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(JwtConfigurationValidator.decodedSecret(properties));
    }

    /**
     * 签发以 subject 标识用户的 HS256 JWT。
     */
    public String issueToken(String subject) {
        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException("JWT subject must not be blank");
        }

        Instant now = clock.instant();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plus(properties.getExpiration()));
        return Jwts.builder()
                .subject(subject)
                .issuer(properties.getIssuer())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 校验 token 并返回 subject。签名不合法、过期、issuer 不符等情况由 JJWT 抛出校验异常。
     */
    public String parseSubject(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("JWT token must not be blank");
        }

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String subject = claims.getSubject();
        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException("JWT subject is required");
        }
        return subject;
    }

}
