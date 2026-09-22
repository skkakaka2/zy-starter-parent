# zy-common-web

## 提供能力

Servlet 自动配置全局异常 `Result` 响应、CORS、`X-Trace-Id` 透传/生成、请求日志、参数校验和 Jackson Web 定制；日志模板位于 `META-INF/zy-common-web/logback-spring.xml.template`。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-web</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小配置

默认即可使用；需要覆盖时配置 `zy.web.*`：

```yaml
zy:
  web:
    trace:
      header-name: X-Trace-Id
    cors:
      allowed-origins: ["https://console.example.com"]
```

复制日志模板为业务服务的 `src/main/resources/logback-spring.xml`，可在日志中输出 `%X{traceId:-}`。

## 边界

数据访问异常的 HTTP 映射由本模块统一处理；`zy-common-mybatis` 不依赖 Web，也不应在 Mapper/实体层处理 HTTP 响应。
