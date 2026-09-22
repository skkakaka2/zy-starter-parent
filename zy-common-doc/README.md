# zy-common-doc

## 提供能力

Servlet 自动配置 Springdoc/OpenAPI 元数据、默认 `default` 分组和 `bearerAuth`（HTTP bearer JWT）安全方案。默认分组匹配 `/**`；不会添加全局认证要求，公开接口可保持公开文档语义。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-doc</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小配置

```yaml
zy:
  documentation:
    title: Order API
    group-id: order
    group-display-name: Order API
    version: 1.0.0
```

标题未设置时依次使用 `spring.application.name + " API"` 和 `Application API`。访问 `/swagger-ui/index.html`，接口描述位于 `/v3/api-docs`。

## 边界

设置 `zy.documentation.enabled=false` 可关闭本模块自动配置。安全模块接入时，应用必须显式放行 `/v3/api-docs/**`、`/swagger-ui/**`、`/swagger-ui.html`、`/webjars/**`；仅声明 `bearerAuth` 不代表所有接口都需要认证。
