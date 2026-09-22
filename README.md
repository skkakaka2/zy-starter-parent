# zy-starter-parent

Java 21 / Spring Boot 3.5.16 的微服务公共模块集合。新服务只选择实际需要的模块；配置型模块通过 Boot 自动配置生效，无需扫描 `com.zy`，`zy-common-core` 可直接使用其 Java API。

## 新服务接入

### 1. 使用 parent

```xml
<parent>
  <groupId>com.zy</groupId>
  <artifactId>zy-starter-parent</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</parent>
```

### 2. 按需选择公共模块

| 场景 | 模块 |
|---|---|
| 统一返回、异常、工具 | `zy-common-core` |
| MVC、校验、TraceId、CORS | `zy-common-web` |
| MyBatis-Plus 基类与分页 | `zy-common-mybatis` |
| 缓存、RedisTemplate、分布式锁 | `zy-common-redis` |
| OpenFeign 调用 | `zy-common-feign` |
| OpenAPI 文档 | `zy-common-doc` |
| JWT 资源服务 | `zy-common-security` |

示例：

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-web</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 3. 最小应用配置

```yaml
spring:
  application:
    name: order-service

zy:
  security:
    jwt:
      secret: ${ZY_SECURITY_JWT_SECRET}
```

启用安全模块时，部署环境必须提供 `ZY_SECURITY_JWT_SECRET`：它是 Base64URL 编码的 HS256 密钥，解码后至少 32 字节。缺失、格式错误或强度不足都会使应用启动失败；不要在仓库、镜像或配置文件中写入真实密钥。

### 4. 可选 Nacos

Nacos Discovery/Config 由应用自行依赖和配置，不由 Feign 公共模块传递。需要真实 Nacos 时在应用配置中启用：

```yaml
spring:
  config:
    import: optional:nacos:order-service.yaml?group=DEFAULT_GROUP
  cloud:
    nacos:
      discovery:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
      config:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
```

禁止使用 `bootstrap.yml`；统一使用 `spring.config.import`。本仓库的 demo 默认关闭 Nacos，便于无外部服务的本地验证。

### 5. 文档与安全白名单

文档使用 Springdoc/OpenAPI，访问 `/swagger-ui/index.html`，接口描述为 `/v3/api-docs`。安全策略应显式放行 `/v3/api-docs/**`、`/swagger-ui/**`、`/swagger-ui.html` 和 `/webjars/**`。

## 构建、测试与发布

```bash
mvn -B clean verify
mvn -B -pl your-service -am clean verify
```

发布前执行完整测试，注入 JWT 密钥及环境专属配置；按需启用 Redis、Nacos 和监控端点。镜像/部署包不得包含真实密钥，生产环境应复核文档白名单、Nacos 连通性和外部 Redis 地址。
