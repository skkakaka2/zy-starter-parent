# zy-common-security

## 提供能力

提供无状态 HS256 JWT 签发/校验、Bearer 认证过滤器和 Servlet `SecurityFilterChain`。未认证和无权限响应均为统一 `Result` JSON（HTTP 401/403）；默认放行健康检查和 Springdoc 路径。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-security</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小配置与使用

```yaml
zy:
  security:
    jwt:
      secret: ${ZY_SECURITY_JWT_SECRET}
      issuer: order-service
      expiration: 2h
```

通过 `JwtTokenService.issueToken(subject)` 签发 token；调用接口时使用 `Authorization: Bearer <token>`。可用 `zy.security.jwt.permit-paths` 增补公开路径。

## 边界

`ZY_SECURITY_JWT_SECRET` 必须由部署环境提供，为 Base64URL HS256 密钥且解码后至少 32 字节；缺失、非法或过短会导致启动失败。不要提交真实密钥，也不要用 JWT 关闭开关替代生产安全策略。
