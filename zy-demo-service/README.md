# zy-demo-service

## 提供能力

可运行的公共模块集成示例：H2 `demo_item` 数据、MyBatis-Plus 分页、JWT 保护、健康检查和 Springdoc。接口包括 `GET /demo/hello`、`POST /demo/validate`、`GET /demo/page?pageNum=1&pageSize=2`。

## Maven 坐标

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-demo-service</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

该模块定位为可执行示例，通常直接运行而非作为其他服务的依赖。

## 最小运行

```bash
export ZY_SECURITY_JWT_SECRET='部署环境提供的 Base64URL 密钥'
mvn -B -pl zy-demo-service -am spring-boot:run
```

本地 H2 会自动执行 `schema.sql`、`data.sql`；健康检查为 `/actuator/health`，文档地址为 `/swagger-ui/index.html`。除健康检查和文档外，`/demo/**` 需要 Bearer JWT。

## 边界

本地模式有意排除 Redis 自动配置，并关闭 Nacos Discovery、Config 与 import 检查，因此不需要 Redis/Nacos 服务。接入真实 Nacos 时启用 discovery/config，并使用 `spring.config.import=optional:nacos:zy-demo-service.yaml?group=DEFAULT_GROUP`；不要使用 `bootstrap.yml`。
