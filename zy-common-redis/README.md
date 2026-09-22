# zy-common-redis

## 提供能力

自动配置 `RedisTemplate<String, Object>`、Redis Cache 前缀/TTL 定制和 Redisson `DistributedLock`。键使用字符串序列化，值使用通用 JSON；缓存默认 TTL 为 `10m`，锁默认等待 `2s`、租约 `30s`。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-redis</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小配置与使用

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
zy:
  redis:
    cache-ttl: 10m
    lock-lease-time: 30s
```

```java
boolean executed = distributedLock.tryExecute("order:42", () -> service.process());
```

Redisson 使用独立 `spring.redis.redisson.config` 时，地址必须与 `spring.data.redis` 一致。

## 边界

通用 JSON 带受限类型信息，仅适合可信的服务内部 Redis 数据；禁止反序列化任何不可信输入。锁始终使用租约，且仅在当前线程仍持锁时解锁；业务操作应保持短小并处理未获取锁的返回值。
