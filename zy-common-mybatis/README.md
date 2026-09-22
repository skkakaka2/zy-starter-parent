# zy-common-mybatis

## 提供能力

提供 `BaseEntity`、`BaseMapperPlus`、分页与乐观锁拦截器、逻辑删除和审计字段填充。`BaseEntity` 包含 `id`、创建/更新人和时间、`deleted`、`version`。

## Maven 依赖

```xml
<dependency>
  <groupId>com.zy</groupId>
  <artifactId>zy-common-mybatis</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 最小使用

```java
@TableName("orders")
class Order extends BaseEntity { }

@Mapper
interface OrderMapper extends BaseMapperPlus<Order> { }
```

表结构应包含基类字段；分页参数可通过 `zy.mybatis.pagination-max-limit` 和 `zy.mybatis.pagination-overflow` 调整。MyBatis-Plus 的逻辑删除配置见 `META-INF/zy-common-mybatis/application-common.yaml`。

## 边界

模块只负责持久化能力，不依赖 Web。数据访问异常到 HTTP 状态/`Result` 的映射由 `zy-common-web` 处理。
