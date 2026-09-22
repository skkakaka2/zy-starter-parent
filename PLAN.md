# zy-starter-parent 建设计划

> 状态：计划中（仅维护本文件，不进入代码实现）
> 基线：Spring Boot 3 + Java 21 + 多模块 parent + 直接微服务 + MyBatis-Plus 系
> 环境：Java 21.0.10 + Maven 3.9.9（已确认）
> 仓库现状：空仓（仅 `.git/.idea/README.md`）

## 1. 版本基线（锁定）

| 项 | 取值 | 说明 |
|---|---|---|
| Java | 21 | `maven.compiler.release=21`，`UTF-8` |
| Spring Boot | `3.5.16`（2026-09-10 已验中央仓库存在，锁定） | `spring-boot-starter-parent` 做父；3.2+ 即支持 Java 21 |
| Spring Cloud | `2025.0.0`（已验存在；后续 patch 升级走 Step 8） | 对应 Boot 3.5（`spring-cloud-dependencies` BOM） |
| Spring Cloud Alibaba | `2025.0.0.0` | 对应 Boot 3.5 + Cloud 2025.0（Nacos 3.0.3 / Sentinel 1.8.9 / Seata 2.5.0） |
| MyBatis-Plus | `3.5.17` | 必须用 `mybatis-plus-spring-boot3-starter` |
| 安全（已确认） | `spring-boot-starter-security + jjwt` | 2026-09-10 确认用默认方案，不用 Sa-Token |
| 文档/工具 | `springdoc-openapi-starter-webmvc-ui:2.x`、`knife4j-openapi3:4.x`（Boot3 系）、`redisson-spring-boot-starter:3.x`、`hutool-bom`、`fastjson2` | 小版本在施工时用 `mvn versions:display-dependency-updates` 锁最新 patch |

风险提示：Boot 3.5.x / Cloud 2025.0.x 的 OSS 支持已在 2026-06 EOL，最后补丁 `3.5.16`。
本期坚持 Boot 3（生态成熟），预留未来升级 Boot 4 的口子。

## 2. 仓库结构规划

```text
zy-starter-parent/                  # packaging=pom，聚合 + 版本管控，不写业务
├─ pom.xml                          # 父：parent继承 + dependencyManagement + pluginManagement + profiles
├─ zy-common-core/                  # 无Web依赖：Result/异常/枚举/工具/Jackson/常量
├─ zy-common-web/                   # Web横切：全局异常、CORS、Validator、TraceId、日志、脱敏
├─ zy-common-mybatis/               # MyBatis-Plus：BaseEntity、MP配置、分页、MetaObjectHandler
├─ zy-common-redis/                 # Redis/Redisson：Template、Cache、分布式锁
├─ zy-common-security/              # 认证：JWT + Security（已确认，不用Sa-Token）
├─ zy-common-doc/                   # springdoc/knife4j 文档配置
├─ zy-common-feign/                 # 微服务：OpenFeign配置、header透传、降级、Sentinel整合
└─ zy-demo-service/                 # 验证用示例服务（稳定后可删或保留）
```

后续每个业务服务（如 `zy-user-service`）都以 `parent=zy-starter-parent` 方式继承，按需依赖 `zy-common-*`。

## 3. 逐步骤计划

### Step 0 — 约定与地基（约 10 分钟）

- [x] 定 `groupId=com.zy`，parent `artifactId=zy-starter-parent`，`version=1.0.0-SNAPSHOT`，`packaging=pom`（Step 1 落 pom 时执行）
- [x] 新建 `.gitignore`（`target/、*.iml、.idea/、.DS_Store`）、补 `README.md`（版本矩阵 + 模块说明 + 新服务接入三行）
- [x] Maven settings 配阿里云镜像（本机 `~/.m2/settings.xml` 的 `alimaven` 已改为 `https://` 新地址；`dependency:get` 拉取 Boot 3.5.16 pom 验证通过）。公司内部发布私服暂无，`mvn deploy` 推迟到有 Nexus 再说；确认 `mvn -v` 的 Java 是 21
```xml
<mirror>
  <id>aliyun-public</id>
  <mirrorOf>central</mirrorOf>
  <name>Aliyun Public</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```
- [x] 门禁：`java -version` 为 21.0.10（`mvn -v` 一致）；`git status` 仅剩预期内的 README 修改 + `.gitignore`/`PLAN.md` 未跟踪，`.idea/` 已被 ignore

### Step 1 — 父 `pom.xml`（核心，约 30 分钟）

- [x] `parent` 继承 `spring-boot-starter-parent:3.5.16`（`relativePath` 置空，用仓库版父 pom）
- [x] `<properties>` 集中：基础 + 5 个 BOM + springdoc 2.9.1/redisson 3.52.0/knife4j 4.5.0/flatten 1.8.0/versions 2.21.0（大版本均按 Boot3 线选定，拒绝跨大版本）
- [x] `<dependencyManagement>`：5 个 BOM import + 3 个 starter 锁版本 + jjwt-bom 泄漏的 jackson-databind 纠偏回 2.21.4（见变更记录）
- [x] `<dependencies>` 父层：`lombok（optional）`、`validation`、`jsr310`，其余下沉
- [x] `<pluginManagement>`：compiler（release 21）/surefire/failsafe/spring-boot（版本随 Boot）+ flatten/versions（自锁版本）
- [x] `<modules>`：8 个模块 stub pom 已建（7 common + demo，内容 Step 2~9 填充），reactor 有效
- [x] `<profiles>`：dev（默认）/test/prod 占位，切 `profile.env`
- [x] 门禁：`mvn validate` 全 reactor SUCCESS；`dependency:tree` 无冲突（曾抓到 jackson 2.12.7.1 已修复，见变更记录）

### Step 2 — `zy-common-core`（无 Web 依赖，最先，约 40 分钟）

- [x] 依赖：`lombok/validation/jsr310（父 pom 继承） + jackson-databind + hutool-core + junit-jupiter/assertj（test）`（fastjson2 按需后续加，本次未引）
- [x] 代码：`Result<T>（含 traceId）/PageResult`、`ErrorCode`（CODE + HTTP_STATUS + MSG）、`BusinessException/SystemException`、`Assert`、`BeanCopyUtils（Hutool）`、`JacksonConfig（静态工厂、无 Spring 依赖，Step 3 再接入 Spring）`、常量类（Header/Trace/Tenant）
- [x] 单测：ResultTest / JacksonConfigTest（含反序列化）/ AssertTest，共 8 用例全绿
- [x] 门禁：`mvn -pl zy-common-core -am clean install` BUILD SUCCESS；tree 无 spring-web/context（LSP 的 Lombok 误报已证伪）

### Step 3 — `zy-common-web`（依赖 core，约 40 分钟）

- [x] 依赖：`core + spring-boot-starter-web + validation + aop + starter-test`；`zy-common-web` 自身不引任何 Boot2 旧包（`dependency:tree` 已验：jackson 2.21.4 / spring-web 6.2.19 / logback 1.5.34）
- [x] 代码：`GlobalExceptionHandler`（校验/Bind/方法级校验/类型不匹配/405/404/Business/System/兜底 500 统一转 `Result`，并回填 traceId）、`ValidatorConfig`、`CorsConfig`、`TraceIdFilter（MDC）`、`WebLogAspect`、`脱敏注解 @Desensitize + DesensitizeSerializer`、`WebProperties（zy.web.*）`、`logback-spring.xml.template`、`application-common.yaml` 片段；Jakarta 命名空间已遵守（Filter 用 `jakarta.servlet.*`、校验用 `jakarta.validation.*`）；默认 `allow-credentials=false`，仅在来源收窄后由服务显式开启
- [x] 自动装配：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 暴露 `WebAutoConfiguration`，业务服务**无需** `@ComponentScan("com.zy")`；后续 Step 4~7 沿用同一模式
- [x] 门禁：`mvn -pl zy-common-web -am clean verify` BUILD SUCCESS（16 用例全绿：异常 8 / TraceId 2 / 脱敏 3 / 自动装配 3）；根 `mvn validate` 全 reactor 通过
- [x] 已知口径：MP/数据访问异常**不在 mybatis 模块**处理（否则反向依赖 web）；当前由 web 的通用兜底保障响应格式。需要识别重复键/约束冲突时，在后续 Step 4.1 给 `zy-common-web` 增加基于 Spring `DataAccessException` 的通用映射。

### Step 4 — `zy-common-mybatis`（依赖 core，约 40 分钟）

- [x] 依赖：`core + mybatis-plus-spring-boot3-starter + mybatis-plus-jsqlparser（MP 3.5.9+ 分页必需）+ jdbc`；生产数据库驱动由具体业务服务选择，H2 仅 test scope，Hikari 由 Boot JDBC starter 提供
- [x] 代码：`BaseEntity（ASSIGN_ID + createBy/createTime/updateBy/updateTime/deleted/version）`、`BaseMapperPlus`、`MybatisPlusConfig（乐观锁 + 分页，分页不硬编码方言）`、`AuditMetaObjectHandler`、`AuditUserContext`、`MybatisProperties`、自动装配、MP yml 模板；更新审计字段在每次更新时由当前上下文强制回填
- [x] 门禁：`mvn -pl zy-common-mybatis -am clean verify` BUILD SUCCESS；H2 集成 5 用例验证填充、分页、逻辑删除、乐观锁、已加载实体切换用户后审计更新

### Step 4.1 — 数据访问异常 HTTP 映射（后续补强）

- [x] 在 `zy-common-web`（不是 `zy-common-mybatis`）基于最小 `spring-tx` 依赖统一映射 Spring 数据访问异常；`DuplicateKeyException` / `DataIntegrityViolationException` 返回 `ErrorCode.CONFLICT`（HTTP 409），其余 `DataAccessException` 返回不泄露细节的 `ErrorCode.INTERNAL_ERROR`（HTTP 500），保持 mybatis 不反向依赖 web
- [x] 门禁：standalone MockMvc 19 用例覆盖唯一键冲突、完整性约束冲突与通用数据访问异常；`mvn -pl zy-common-web -am clean verify` BUILD SUCCESS

### Step 5 — `zy-common-redis`（依赖 core，约 30 分钟）

- [x] 依赖：`core + spring-boot-starter-data-redis + redisson-spring-boot-starter:3.52.0`；Redisson starter 自动提供 `RedissonClient`，本模块不重复定义；无真实 Redis/Testcontainers 依赖
- [x] 代码：标准名 `redisTemplate`（String key + GenericJackson2Json value，禁用 JDK 序列化）、`RedisCacheManagerBuilderCustomizer`（应用名隔离前缀 + 默认 10m TTL）、`DistributedLock`（三参数 `tryLock(wait, lease)`、中断恢复、仅当前线程解锁）、自动装配及 yml 模板；Redisson 地址与 `spring.data.redis` 地址必须保持一致
- [x] 序列化边界：为通用 `Object` 模板启用受限类型信息，仅允许 `com.zy.*`、时间类型、Number/Boolean、Collection/Map；缓存 DTO 改包/改类名或改变 `@class` 字段前须做缓存迁移或清理，不反序列化不可信 Redis 数据
- [x] 门禁：`mvn -pl zy-common-redis -am clean verify` BUILD SUCCESS；无 Redis 网络连接的 7 用例验证自动配置退避、属性绑定、类型往返、锁成功/失败/中断/异常解锁/前缀

### Step 6 — `zy-common-feign`（微服务关键，依赖 core + web，约 40 分钟）

- [x] 依赖：`core/web + spring-cloud-starter-openfeign + loadbalancer + sentinel-starter（经 SCA）`；按确认的模块边界，Nacos Discovery 由具体应用自行引入，通用 Feign 不传递注册中心实现
- [x] 代码：`FeignRequestInterceptor`（透传 Authorization/X-Trace-Id/X-Tenant-Id，保留调用方已设置的同名 header）、`FeignErrorDecoder`（非 2xx 转不含远端详情的 BusinessException）、默认 2s/5s 超时、禁重试、BASIC 日志配置；通过自动装配注册，模板说明由调用方显式 `@EnableFeignClients`
- [x] 门禁：`mvn -pl zy-common-feign -am clean verify` BUILD SUCCESS；core 8 + web 19 + feign 6 用例，验证 header 透传/无 Servlet 上下文/不覆盖、错误解码、默认值、用户 ErrorDecoder 覆盖及普通拦截器共存

### Step 7 — `zy-common-doc` + `zy-common-security`（约 40 分钟）

- [x] **doc**：用户确认仅采用 `springdoc-starter-webmvc-ui:2.9.1`；移除 Knife4j 4.5.0（其 Springdoc 2.3 传递依赖与 2.9.1 存在运行时链接风险）。实现 Servlet 条件自动装配、分组、Bearer JWT scheme、服务名标题推导与 `/swagger-ui/index.html` 模板；白名单 `/v3/api-docs/**、/swagger-ui/**、/swagger-ui.html、/webjars/**`
- [x] **security（已确认：Security + JWT）**：`core + web + spring-boot-starter-security + jjwt`，无状态 `SecurityFilterChain`、Base64URL HS256（解码后至少 32 字节且无默认密钥）`JwtTokenService`、JWT 过滤器、JSON `Result` 401/403 handler；共享 Web 的 `CorsConfigurationSource`，不保留两套 CORS 策略；文档和 `/actuator/health` 等默认白名单可由配置覆盖
- [x] 门禁：`mvn -pl zy-common-doc,zy-common-security -am clean verify` BUILD SUCCESS；core 8 + web 19 + security 8 + doc 4 用例，覆盖 JWT 签发/校验、弱密钥、无/无效 token 的 401、权限不足 403、CORS 预检、自定义链退避与文档元数据/开关退避

### Step 8 — 全量拉通（约 15 分钟）

- [x] 根目录 `mvn clean install -DskipTests`，再 `mvn clean verify` 全量（含单测）
- [x] `mvn versions:display-dependency-updates` 与 `display-property-updates` 扫描完成：显式 properties 无补丁级候选；候选均跨 minor/major 或版本火车（Jackson 2.22、JJWT 0.13、Redisson 4、Cloud 2025.1、Springdoc 3、versions-plugin 2.22），不破坏已验证的 Boot 3.5/Cloud 2025.0 组合
- [x] 门禁：全绿（core 8 + web 19 + mybatis 5 + redis 7 + security 8 + doc 4 + feign 6 = 57 用例）；`mvn -pl zy-common-mybatis dependency:tree -Dincludes=...` 仅解析 `mybatis-plus-spring-boot3-starter:3.5.17`，无 Boot 2 旧 starter

### Step 9 — `demo-service` 端到端验证（约 40 分钟）

- [x] 新建 `zy-demo-service`：`parent=zy-starter-parent`，依赖 `web + mybatis + redis + feign + doc + security + actuator + nacos-discovery/config`；Redis/Nacos 依赖保留在应用层，默认 local/test 配置显式禁用其外部连接
- [x] 最小闭环：`GET /demo/hello → Result<String>`、`POST /demo/validate（触发校验异常）`、`GET /demo/page（MP 分页 + H2，表名避开 H2 保留字）`、`/v3/api-docs` 与 `/swagger-ui/index.html`、TraceId 响应头/MDC；Springdoc-only，已移除旧 `/doc.html` 断言
- [ ] Nacos 最小验证（外部环境待办）：本地/测试 Nacos 启动后，启用 discovery/config 并使用 `spring.config.import=optional:nacos:...`，验证配置导入与服务注册；不使用 `bootstrap.yml`
- [ ] 可选：开 `spring.threads.virtual.enabled=true` 验证 Java 21 虚拟线程，确认启动无警告
- [x] 门禁（本地）：6 个 MockMvc 端到端用例全过，覆盖 health、401、JWT hello、校验、H2 分页、Springdoc；无 Nacos/Redis 环境下实际启动并 curl 得到 HTTP 200、`X-Trace-Id` 与标准 Result。demo 保留为示例

### Step 10 — 收尾与发版（约 15 分钟）

- [x] 补各模块 `README`（我是谁 / 业务怎么依赖我 / 最小 yml 三段式）+ 根 `README` 接入指南；已同步 Springdoc-only、JWT 密钥、Nacos 应用归属与 Redis/Feign 边界
- [ ] `git add/commit/push`，打 `v1.0.0-SNAPSHOT`；如有私服，配 `distributionManagement` 后 `mvn deploy`
- [x] 输出《新服务接入清单》：根 README 已包含 parent 坐标、`zy-common-*` 选型、JWT/Nacos 配置、logback 与 `/swagger-ui/index.html` 验证步骤

## 4. 待确认事项

- [x] 安全方案：已确认用默认 `Security + JWT`（2026-09-10），不用 `Sa-Token`
- [x] Boot 小版本：锁定 `3.5.16`（2026-09-10 已验中央仓库存在，无需回落）
- [x] 镜像/私服：拉取用阿里云 public 镜像（settings.xml，已验连通）；内部发布私服暂无，`deploy` 延后（2026-09-10 确认）

## 5. 变更记录

- 2026-09-10：初版计划落盘（Step 0~10 + 版本矩阵 + 门禁），后续变更只更新本文件。
- 2026-09-10：确认安全方案为默认 `Security + JWT`，Step 7 与待确认事项同步更新。
- 2026-09-10：自审查修订——锁定 `3.5.16`/Cloud `2025.0.0`（中央仓库已验存在）；修正 doc 白名单（`/doc/**` 匹配不到 `/doc.html`）；Nacos 改 `spring.config.import`（bootstrap 默认禁用）；补充 Jakarta 命名空间、H2 保留字、虚拟线程备注。
- 2026-09-10：镜像/私服确认——拉取用阿里云 public 镜像（已验连通，配 settings.xml）；内部发布私服暂无，deploy 延后。
- 2026-09-10：Step 0 完成（`.gitignore` + README + 阿里云镜像 https 化 + `dependency:get` 链路验证通过）。
- 2026-09-10：Step 1 完成（根 pom + 8 stub，validate/tree 全绿）。附带修复两处：① jjwt-bom 经其父 pom（jjwt-root）泄漏 jackson-databind 2.12.7.1 覆盖 Boot 的 2.21.4，已在父 pom 尾部显式纠偏（升级 Boot 时同步 jackson.version）；② 内网 Nexus 的 `public` 镜像（mirrorOf=*）实际拦截了全部中央仓库流量致阿里云配置不生效，已收窄为 `dyness-*`，现 central 经 alimaven 直连（已验）。
- 2026-09-10：Step 2 完成（core：11 源码 + 3 单测，8 用例全绿，直做——@fixer 曾撞 openai 配额失败，改由当前会话直接实现）。
- 2026-09-11：Step 3 完成（web：14 源码 + 4 单测，16 用例全绿）。关键决策与踩坑：① 横切能力改走 `META-INF/spring/...AutoConfiguration.imports` 自动装配（业务服务 base package 是 `com.zy.xxx`，扫描不到 `com.zy.web`），Step 4~7 沿用；② HTTP 状态口径——BusinessException 返回 HTTP 200 + 业务码（业务失败属可预期结果、非协议错误），SystemException/兜底 500，校验类 400，方法不支持 405、资源不存在 404；③ core 的 `JacksonConfig` 拆出 `javaTimeModule()` / `longToStringModule()` 两个工厂方法，web 用 `Jackson2ObjectMapperBuilderCustomizer` 复用（**不直接替换 ObjectMapper Bean**，否则丢失 `spring.jackson.*` 与 ParameterNamesModule）；④ ValidatorConfig 必须 `@AutoConfigureBefore(ValidationAutoConfiguration.class)`，否则 Boot 的 `defaultValidator` 抢先注册、failFast 配置失效；⑤ Spring 6.2 API 变更实测：`NoHandlerFoundException#getMethod()` 已删除（改用 `getRequestURL()`）、`MethodValidationResult#getAllValidationResults()` 已废弃（改用 `getParameterValidationResults()`）；⑥ CORS 用 `setAllowedOriginPatterns` 而非 `setAllowedOrigins`（credentials + `*` 组合会抛异常）；⑦ 未引 `spring-boot-configuration-processor`，避免与 Lombok 注解处理器抢顺序（配置元数据 Step 8 再补）。
- 2026-09-11：环境备注——本机无独立 JDK/Maven（PATH 里没有 `mvn`），实测可用 IDEA-251 自带组合：`/media/aaa/新加卷/linux program/idea-IU-251.29188.11/jbr`（JBR 21.0.8）+ 同目录 `plugins/maven/lib/maven3/bin/mvn`（Maven 3.9.9），构建命令需先 export `JAVA_HOME`/`PATH`。
- 2026-09-11：Step 3 复验完成（`mvn -pl zy-common-web -am clean verify` 全绿，core 8 + web 16 用例）。复审修复：① CORS 默认 `allowed-origin-patterns=*` 时，`allow-credentials` 必须默认关闭，避免任意来源跨域携带 Cookie；Bearer Token 不受影响；② Jackson Customizer 不再调用 `builder.modules(...)` 覆盖 Boot 自动发现模块，改为按类型注册 LocalDateTime/Long 序列化器与反序列化器。
- 2026-09-14：Step 4 完成（mybatis：7 源码 + H2 集成 5 用例）。核验 MP 3.5.17 后显式加入分页必需的 `mybatis-plus-jsqlparser`；审计更新改为强制写入上下文用户，防止 `strictUpdateFill` 遇到已加载实体的非空旧值而失效；修正原计划，将 `DuplicateKeyException` 等 HTTP 映射移至后续 `zy-common-web` Step 4.1，保持 mybatis 不依赖 web。
- 2026-09-14：Step 5 完成（redis：5 源码 + 2 测试类，7 用例）。复审发现 `GenericJackson2JsonRedisSerializer(customObjectMapper)` 不会自动写类型信息，读取会退化为 Map；已改为复制项目 mapper 后启用严格白名单多态类型，并新增 Long/LocalDateTime 业务 record 往返测试。`DefaultTyping.EVERYTHING` 是保留 final 根 DTO 类型的必要策略，局部抑制 Jackson 2.21 弃用警告并以 PTV 白名单约束，禁止 LaissezFaire/Object 放行。
- 2026-09-14：Step 4.1 完成（web：新增 `spring-tx` 最小依赖，GlobalExceptionHandler 增加三层数据访问映射，19 用例全绿）。业务码新增 `CONFLICT(409, 409, "数据冲突，请勿重复提交")`；DuplicateKey / DataIntegrityViolation 返回 409，其他 DataAccessException 返回通用 500，不向客户端泄露 SQL、约束名或连接信息。
- 2026-09-14：Step 6 完成（feign：3 源码 + 自动装配 + 配置模板 + 3 测试类，6 用例）。按确认的边界移除原计划中的 Nacos Discovery 传递依赖，应用自行选择并配置注册中心；Sentinel 仅提供可选集成且模板默认关闭。复审修正：普通业务 `RequestInterceptor` 不再使公共 header 透传失效，SCA Sentinel 开关使用正确的 `feign.sentinel.enabled` 路径。
- 2026-09-14：Step 7 完成（doc：2 源码 + 4 用例；security：7 源码 + 8 用例；web CORS 自动配置补 1 用例）。文档经用户确认改为 Springdoc-only，并删除 root POM 中的 Knife4j 管理项及 README 的 `/doc.html` 指引，规避 Knife4j 4.5.0 与 Springdoc 2.9.1 的已知兼容风险；Springdoc/Swagger Jakarta 依赖树已收敛至 2.9.1/2.2.55。Security 默认启用但缺少 `zy.security.jwt.secret` 会安全失败，密钥由部署环境以 Base64URL 注入；401/403 直接序列化 Result，CORS 由 Web 与 Security 共享一个 source。
- 2026-09-14：Step 8 完成（全量 `clean install -DskipTests`、`clean verify` BUILD SUCCESS，57 用例全绿）。版本扫描未发现可直接采用的补丁更新，保持已验证的版本火车；内部 dyness 仓库 metadata 探测仍返回 401、阿里云镜像可回退完成扫描，未影响构建或依赖解析。
- 2026-09-14：Step 9 本地闭环完成（demo：5 源码 + schema/data/config/logback + 6 端到端用例）。本地禁用 Redis/Nacos 外部连接，H2/JWT/Springdoc/TraceId 均已实际运行验证；真实 Nacos 注册与配置导入因无外部实例保留待办。
- 2026-09-14：Step 10 文档完成（根 README + 8 模块 README）。统一接入指引已改为 Springdoc `/swagger-ui/index.html`，明确 Base64URL JWT 密钥、应用自管 Nacos、Redis 信任边界与 Feign 默认策略；Git 提交/推送未执行，等待显式指令。
