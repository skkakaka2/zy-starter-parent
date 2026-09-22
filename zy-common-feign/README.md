# zy-common-feign

## 提供能力

自动配置 Feign 请求头透传、统一错误解码和默认请求选项。Servlet 请求会透传 `Authorization`、`X-Trace-Id`、`X-Tenant-Id`，且不会覆盖调用方已设置的同名请求头；非 Servlet 上下文不透传。

默认值：连接超时 `2s`、读取超时 `5s`、日志 `BASIC`、`Retryer.NEVER_RETRY`。非 2xx 响应统一转换为不暴露远程细节的业务异常。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-feign</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小使用

调用方在启动类显式开启客户端扫描：

```java
@EnableFeignClients(basePackages = "com.example.client")
@SpringBootApplication
class Application { }
```

按需覆盖 `spring.cloud.openfeign.client.config.default.connect-timeout`、`read-timeout`、`logger-level`；重试必须显式配置并确认接口幂等性。Sentinel 开关为 `feign.sentinel.enabled`，默认关闭。

## 边界

本模块不传递 Nacos Discovery/Config 依赖，注册中心由业务应用自行选择、引入和配置。Header 透传不保存请求状态，异步/非 Servlet 调用需要由调用方自行传递上下文。
