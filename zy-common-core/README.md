# zy-common-core

## 提供能力

提供无 Web 依赖的 `Result`、`PageResult`、错误码/异常、断言与工具类；`JacksonConfig` 提供统一的时间、Long 字符串化和未知字段忽略配置工厂。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-core</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小使用

```java
return Result.ok(data);
// throw new BusinessException(ErrorCode.BIZ_ERROR, "业务提示");
```

纯 Java 场景可直接调用 `JacksonConfig.objectMapper()`；Web 应用引入 `zy-common-web` 后会复用其 Jackson 约定。

## 边界

本模块不注册 Spring Bean，也不处理 HTTP。HTTP 状态、请求校验和异常响应由 `zy-common-web` 负责。
