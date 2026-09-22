package com.zy.security.jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String TEST_SECRET = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("test-only-signing-key-material-000".getBytes(StandardCharsets.UTF_8));
    private static final Instant NOW = Instant.parse("2026-09-14T12:00:00Z");

    @Test
    void issuesAndParsesTokenWithInjectedClock() {
        JwtTokenService service = service("zy-starter", Duration.ofHours(2), Clock.fixed(NOW, ZoneOffset.UTC));

        String token = service.issueToken("user-42");

        assertThat(service.parseSubject(token)).isEqualTo("user-42");
    }

    @Test
    void rejectsMalformedExpiredAndWrongIssuerTokens() {
        JwtTokenService service = service("zy-starter", Duration.ofSeconds(1), Clock.fixed(NOW, ZoneOffset.UTC));
        String token = service.issueToken("user-42");
        JwtTokenService expiredClockService = service("zy-starter", Duration.ofSeconds(1),
                Clock.fixed(NOW.plusSeconds(2), ZoneOffset.UTC));
        JwtTokenService wrongIssuerService = service("another-issuer", Duration.ofHours(2), Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> service.parseSubject("not-a-jwt"))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> expiredClockService.parseSubject(token))
                .isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> wrongIssuerService.parseSubject(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMissingAndWeakSecrets() {
        JwtProperties missingSecret = new JwtProperties();
        assertThatThrownBy(() -> new JwtTokenService(missingSecret, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("zy.security.jwt.secret must be configured");

        JwtProperties weakSecret = properties("zy-starter", Duration.ofHours(2));
        weakSecret.setSecret(Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[31]));
        assertThatThrownBy(() -> new JwtTokenService(weakSecret, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 decoded bytes");
    }

    private JwtTokenService service(String issuer, Duration expiration, Clock clock) {
        return new JwtTokenService(properties(issuer, expiration), clock);
    }

    private JwtProperties properties(String issuer, Duration expiration) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setIssuer(issuer);
        properties.setExpiration(expiration);
        return properties;
    }
}
