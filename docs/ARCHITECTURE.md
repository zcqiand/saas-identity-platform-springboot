# saas-identity-platform-springboot Architecture

> 本仓架构文档。回答三个问题：
> 1. 这个仓在 `saas-identity-platform` 家族里是什么角色，禁什么；
> 2. `src/main/java/...` 目录长什么样、谁负责什么；
> 3. 一次"改契约 → Java 仓同步"的核心流程怎么走。

> **范围**：本文档只描述 *架构*（结构 / 边界 / 数据流 / 决策）。
> 编码细则见 [docs/conventions/](conventions/)，单个决策的 ADR 见 [docs/adr/](adr/)，产品需求见 [docs/requirements/](requirements/)，功能清单见 [docs/functions/function-tree.md](functions/function-tree.md)。
>
> 家族级背景与本仓在 14 仓矩阵中的位置，见父仓 [docs/ARCHITECTURE.md](../../../docs/ARCHITECTURE.md)。

---

## 0. 阅读路径

| 你是… | 直接看 |
|---|---|
| 新人，要 30 分钟搞懂本仓 | §1 → §2 → §3（看 Controller 边界） |
| 想加一个新接口 | §3.2 → §4（gen-shared 三步） → 父仓 ADR-0003 |
| 跨端调试 OAuth/JWT 不通 | §3.1（JwtIssuer 真签 + DevJwtDecoder 兜底） → §5（dev/prod 切换路径） → 父仓 §3.4 |
| 想问"为什么 Spring Security + JwtIssuer 这种怪组合" | §6（决策索引） → 父仓 ADR-0014 |
| 想知道 schema 谁管、Flyway 为啥关 | §3.6 → 父仓 ADR-0007 |
| 想知道"改 SQL 一定会同步到这仓么" | §5（cmp abort 防护） |

---

## 1. 角色与定位

**saas-identity-platform-springboot = saas 家族的两个后端仓之一**，负责把同一份 TypeSpec 契约实现为 Java/Spring Boot 形态。

| 维度 | 决策 |
|---|---|
| 语言 / 运行时 | Java 17（`pom.xml` `java.version=21`，但本仓实际编译目标 = JDK 17 字节码）+ Spring Boot 3.4 |
| 构建 | Maven（pom.xml，spring-boot-starter-parent 3.4.0 锁定版本） |
| 持久化 | PostgreSQL（runtime scope `postgresql`）+ Hibernate 6 + Flyway（**dev 关闭**，见 §3.6） |
| 安全 | Spring Security 6 + `spring-boot-starter-oauth2-resource-server` |
| API 契约 | 消费 `saas-identity-platform-shared/generated/openapi/openapi.yaml`（`@openapitools/openapi-generator-cli` codegen） |
| Schema 契约 | 消费 `saas-identity-platform-shared/sql/migrations/V*.sql`（`scripts/gen-shared.sh` 拷） |
| 默认端口 | `8080`（`application.yml:2`） |
| 静态检查 | SpotBugs 4.9.3.2（`effort=Max` + `threshold=Low`）+ Spotless（`googleJavaFormat`） |
| Swagger UI | `springdoc-openapi-starter-webmvc-ui` 2.6.0，暴露 `/v3/api-docs`、`/swagger-ui.html` |
| Function Tree | [docs/functions/function-tree.md](functions/function-tree.md) 7 模块 14 F + 35 I |

**核心边界**：

| 本仓 **写** | 本仓 **不写** |
|---|---|
| 手写 `service/` 业务逻辑（构造器注入） | Controller 路由与 DTO（codegen 替，详见 §3.2） |
| 手写 `repository/`（Spring Data JPA 接口） | DB schema DDL（shared SQL 是 SSOT，详见 §3.6） |
| 手写 JPA entity（`@Entity`） | 字段注入（必须构造器注入，详见 §3.3） |
| 手写 `security/`（`TenantGuard` / `TenantContext` / `JwtIssuer` HS256 真签发） | 越权访问路径（每个 tenant-scoped endpoint 第一行必须调 `TenantGuard.verifyPathTenant`） |
| 手写 `config/SecurityConfig` + dev-only `DevJwtDecoder`（`@Profile("dev")`） | prod profile 不加载 DevJwtDecoder；prod 走 NimbusJwtDecoder + JWKS（详见 §3.1） |

**镜像兄弟**：与 `lab-management-system-springboot` 结构同构，但 saas 版本关 Flyway、跑 OAuth2 IdP 端点（`/api/v1/oauth/**`）、HS256 真签发 JWT。差异见父仓 [docs/ARCHITECTURE.md §4.4.1](../../../docs/ARCHITECTURE.md#44-后端仓springbootaspnetcorenextjs-self)。

---

## 2. 目录骨架

```
saas-identity-platform-springboot/
├── CLAUDE.md                            ← 入口：技术栈 + 禁止事项 + 指向别处
├── .harness/stack.json                  ← suite 门禁读取的项目自描述（声明 L1-L4）
├── .env.example / .env.local            ← SPRING_DATASOURCE_* / JWT_SIGNING_KEY 模板
├── pom.xml                              ← Spring Boot 3.4 + openapi-generator-maven-plugin 不在 pom（本仓用 npx CLI）
├── openapitools.json                    ← @openapitools/openapi-generator-cli 版本
├── spotbugs-exclude.xml                 ← CT_CONSTRUCTOR_THROW 集中屏蔽（Spring bean 生命周期）
├── Dockerfile                           ← multi-stage: maven:3.9-eclipse-temurin-17 builder → runner
├── deploy/                              ← deploy 脚本 + env-file 烘焙
├── docs/
│   ├── ARCHITECTURE.md                  ← 本文档
│   ├── functions/function-tree.md       ← F/I 级功能清单（与 shared BASE 镜像到 I）
│   ├── adr/                             ← 本仓特有 ADR（当前 README 占位）
│   ├── design/{design,flow}-function-map.md
│   ├── conventions/                     ← 本仓编码细则
│   └── requirements/                    ← 产品需求（本仓为空）
├── scripts/
│   └── gen-shared.sh                    ← 契约同步核心脚本（emit OpenAPI + codegen + cp SQL）
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── saas/identity/platform/
│   │   │   │   ├── Application.java                 ← @SpringBootApplication 入口
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java          ← FilterChain + CORS + DevJwtDecoder
│   │   │   │   │   ├── OpenApiConfig.java           ← springdoc 元数据（标题/版本/分组）
│   │   │   │   │   └── DevDataFixer.java            ← dev profile 启动期灌 fixture
│   │   │   │   ├── controller/                      ← 手写 partial 实现 codegen 生成的 Api interface
│   │   │   │   │   ├── AuthController.java          ← M03 auth（全局，permitAll）
│   │   │   │   │   ├── MeController.java            ← M00.F02 whoami + 切租户
│   │   │   │   │   ├── AdminTenantsController.java  ← M00 平台 admin
│   │   │   │   │   ├── AdminAppsController.java     ← M04 OAuth apps CRUD
│   │   │   │   │   ├── AdminAppMenusController.java ← M04 OAuth app menus
│   │   │   │   │   ├── TenantUsersController.java   ← M01 user CRUD
│   │   │   │   │   ├── TenantRolesController.java   ← M02 role CRUD
│   │   │   │   │   ├── TenantRoleMenusController... ← M02 role↔menu grants
│   │   │   │   │   ├── TenantApiKeysController.java ← M05 API key
│   │   │   │   │   └── TenantAuditController.java   ← M06 audit
│   │   │   │   ├── service/                         ← 手写业务（构造器注入）
│   │   │   │   │   ├── AuthService.java             ← M03 登录/OIDC/refresh
│   │   │   │   │   ├── MeService.java
│   │   │   │   │   ├── AdminTenantService.java
│   │   │   │   │   ├── TenantUsersService.java
│   │   │   │   │   ├── TenantRoleService.java
│   │   │   │   │   ├── TenantRoleMenuService.java
│   │   │   │   │   ├── TenantApiKeyService.java
│   │   │   │   │   ├── TenantAuditService.java
│   │   │   │   │   └── OauthService.java            ← M04 authorize/token/refresh
│   │   │   │   ├── repository/                      ← Spring Data JPA 接口
│   │   │   │   │   ├── TenantRepository / TenantMembershipRepository
│   │   │   │   │   ├── UserRepository
│   │   │   │   │   ├── RoleRepository / RolePermissionRepository / RoleMenuGrantRepository
│   │   │   │   │   ├── PermissionRepository
│   │   │   │   │   ├── MenuRepository
│   │   │   │   │   ├── AppRepository
│   │   │   │   │   ├── ApiKeyRepository
│   │   │   │   │   ├── AuditEventRepository / AuditRetentionPolicyRepository
│   │   │   │   ├── entity/                          ← JPA @Entity（反射镜像 shared SQL）
│   │   │   │   │   ├── TenantEntity / UserEntity / TenantMembershipEntity
│   │   │   │   │   ├── RoleEntity / PermissionEntity / RolePermissionEntity
│   │   │   │   │   ├── MenuEntity / RoleMenuGrantEntity
│   │   │   │   │   ├── AppEntity / ApiKeyEntity
│   │   │   │   │   ├── AuditEventEntity / AuditRetentionPolicyEntity
│   │   │   │   ├── mapper/                          ← DTO ↔ Entity 映射（手动）
│   │   │   │   │   ├── TenantMapper / UserMapper(→TenantUserMapper) / AppMapper / ApiKeyMapper
│   │   │   │   │   ├── RoleMapper / AuditEventMapper
│   │   │   │   ├── converter/                       ← JPA AttributeConverter（PG 数组/枚举）
│   │   │   │   │   ├── UuidArrayConverter / StringArrayConverter / EnumArrayConverter
│   │   │   │   ├── enums/                           ← 业务枚举 + Hibernate Converter
│   │   │   │   │   ├── TenantStatus / UserStatus / MembershipStatus
│   │   │   │   │   ├── ApiKeyStatus / AppStatus / MenuType / MenuStatus
│   │   │   │   │   ├── OAuthGrantType / AuditAction
│   │   │   │   │   └── *StatusConverter / *TypeConverter
│   │   │   │   └── security/
│   │   │   │       ├── TenantContext.java           ← 从 SecurityContext 读 JWT tenant_id claim
│   │   │   │       ├── TenantGuard.java             ← 路径 tenantId vs JWT claim 校验
│   │   │   │       └── JwtIssuer.java               ← HS256 真签发（Phase 6 起替换 dev JWT）
│   │   │   └── saas/identity/shared/                ← codegen 产物（每次 gen-shared 重写）
│   │   │       ├── api/                             ← AuthApi / MeApi / AdminTenantsApi / ... 接口
│   │   │       │                                    ← （Controller implements 这些）
│   │   │       └── dto/                             ← LoginRequest / LoginResponse / TenantDto / ...
│   │   └── resources/
│   │       ├── application.yml                      ← 端口 / Datasource / JPA / Flyway / actuator
│   │       └── db/migration/                        ← cp 自 shared/sql/migrations/
│   │           ├── V001__init_tenants.sql
│   │           ├── V002__init_users_memberships.sql
│   │           ├── V003__init_roles_permissions.sql
│   │           ├── V004__init_api_keys.sql
│   │           ├── V005__init_oauth_apps_menus.sql
│   │           ├── V006__init_audit_events.sql
│   │           ├── V007__indexes.sql
│   │           ├── V008__users_role_ids_and_drop_redundant_index.sql
│   │           ├── V009__init_oauth_codes.sql
│   │           └── README.md
│   └── test/java/saas/identity/platform/
│       ├── harness/                                 ← 测试侧 fnTest 工具 + trace 监听
│       │   ├── Fn.java                              ← @Fn annotation
│       │   ├── HarnessTraceListener.java            ← JUnit 监听 → .state/trace.json
│       │   └── FnAnnotationSmokeTest.java
│       ├── repository/
│       │   ├── RepositoryTestFactoryConfig.java
│       │   └── UserRepositoryDataJpaTest.java       ← @DataJpaTest 切 H2
│       └── service/
│           ├── AuthServiceTest / AdminTenantServiceTest / MeServiceTest
│           ├── TenantUsersServiceTest / TenantRoleServiceTest
│           ├── TenantApiKeyServiceTest / TenantAuditServiceTest
│           └── OauthServiceTest                     ← M04 真 OAuth 流程
└── target/                                          ← maven 产物（含 .openapi-tmp 临时生成器输出）
```

**关键观察**：

- `src/main/java/saas/identity/platform/` 是手写代码根；`saas/identity/shared/` 是 codegen 产物根——**两棵子树必须严格分家**（详见 §5 防越界）。
- 本仓不维护 `util/` 包（与父仓 §4.4.1 的"目录骨架"示意略不同——util 散落在 service / converter / security 里）。
- 本仓不引入 openapi-generator-maven-plugin（与父仓文档示意有差异），改为 `scripts/gen-shared.sh` 调 `npx --yes @openapitools/openapi-generator-cli generate`（v0.2.0 起架构变更）。
- `db/migration/V*.sql` 是 `cp` 自 shared 仓的**只读镜像**——本仓不直接编辑（详见 §3.6）。

---

## 3. 核心模块

### 3.1 安全层（`config/` + `security/`）

**入口**：`config/SecurityConfig.java`（`@Configuration`）。

| Bean | 职责 | 关键配置 |
|---|---|---|
| `SecurityFilterChain filterChain(HttpSecurity)` | OAuth2 resource server + CORS + STATELESS + route 白名单 | `/api/v1/auth/**`、`/api/v1/oauth/**`、`/actuator/**`、`/v3/api-docs*`、`/swagger-ui/**` `permitAll`；其他 `.anyRequest().authenticated()` |
| `CorsConfigurationSource corsConfigurationSource()` | 读 `saas.cors.allowed-origins`（env `SAAS_CORS_ALLOWED_ORIGINS` 覆盖），默认 dev = `localhost:3000 + :5173 + :3001` | `addAllowedOrigin("*")` 不允许——`setAllowCredentials(true)` 必须配合显式 origin |
| `JwtDecoder jwtDecoder()` | **dev-only bean**：`DevJwtDecoder` 静态内部类，吃 MSW/test 的 `alg=none` fixture token；base64url 解 header + payload，只校验 `exp`，过期把 `Jwt.exp` 延长到 `now+1h`；不验签 / 不验 issuer / audience | prod profile **不加载** 此 bean；prod = 删该 inner class + 在 `application.yml` 配 `spring.security.oauth2.resourceserver.jwt.issuer-uri`，让 Spring Boot 自动配置 `NimbusJwtDecoder` 走 JWKS 验 HS256 真签发 JWT |
| `JwtIssuer`（`security/JwtIssuer.java`） | **主路径**：HS256 真签发 access token；claims = sub/tenant_id/jti/iss/aud/exp/iat；与 saas-aspnetcore/nextjs-self 镜像实现 | 镜像 `Jwt:SigningKey` ≥32B env；缺失即抛 `IllegalStateException`（防 prod 用弱 dev 默认 key） |

**Dev/Prod 切换路径**（与父仓 §3.4 对齐）：

| 模式 | 谁发 JWT | 谁验 JWT | CORS allowlist |
|---|---|---|---|
| **prod**（默认） | `JwtIssuer` HS256 真签发 access token（RFC 7519） | Spring Boot 自动 `NimbusJwtDecoder` + JWKS 验签（或对称密钥 `JWT_SIGNING_KEY` ≥32B） | `SAAS_CORS_ALLOWED_ORIGINS` 改正式域名 |
| **dev**（`@Profile("dev")`） | `msw` / `nextjs` / `aspnetcore` test helper 发 `alg=none` fixture token | `DevJwtDecoder.decode()` 手动 parse，只校验 exp（仅吃 test fixture，prod bean 不加载） | 默认 localhost:* |

**`security/` 三件套**：

| 类 | 职责 | 调用方 |
|---|---|---|
| `JwtIssuer` | HS256 真签发 access token（Phase 6 起）/ opaque refresh token `saas-rt-{userId}-{ts-ms}-{rand-base64}` 格式（与 saas-nextjs `lib/oauth-store.ts:97-99` 同款）。读 `JWT_SIGNING_KEY` / `JWT_ISSUER` / `JWT_AUDIENCE` / `JWT_TTL_SECONDS`。key 必须 ≥32 bytes | `AuthService` / `OauthService` |
| `TenantContext` | 从 `SecurityContextHolder` 读 JWT principal，拿 `tenant_id` claim / `sub` | `TenantGuard` / Service 内透传 |
| `TenantGuard` | **MANDATORY** 在每个 tenant-scoped endpoint 第一行调 `verifyPathTenant(pathTenantId)`；不匹配抛 `AccessDeniedException`（防止 path 改 tenantId 读别人数据） | `TenantUsersController` / `TenantRolesController` / `TenantApiKeysController` / `TenantAuditController` 等所有 `*/{tenantId}/*` 路径 |

**关键禁令**（[CLAUDE.md §2](../../../output/saas-identity-platform-springboot/CLAUDE.md)）：

- 禁止跳过 `TenantGuard.verifyPathTenant` 校验——这是多租户隔离的全部防线；
- 禁止在 prod profile 留 `DevJwtDecoder`（应删 bean + 配 JWKS env，详见父仓 §3.4）；
- 禁止 `JWT_SIGNING_KEY` 写进 `.env.example`（deploy 脚本自举随机，详见 `memory/springboot-dev-jwt-decoder-gap.md`）；
- 禁止把 dev fixture token 验签逻辑"复制粘贴到 prod"——`DevJwtDecoder` 与 `NimbusJwtDecoder` 是两个独立分支，prod 必须走真签 HS256 路径。

### 3.2 Controller 层（`controller/`）

**关键**：本仓的 Controller **手写**，但实现的是 `codegen` 生成的 `saas.identity.shared.api.*Api` 接口（`implements AuthApi`、`implements MeApi` 等）。

```
TypeSpec emit OpenAPI.yaml
    ↓
scripts/gen-shared.sh（npx openapi-generator-cli generate -g spring --library spring-boot）
    ↓
src/main/java/saas/identity/shared/api/{AuthApi,MeApi,...}.java  ← 接口签名
src/main/java/saas/identity/shared/dto/{LoginRequest,TenantDto,...}.java  ← DTO
    ↓
src/main/java/saas/identity/platform/controller/{AuthController,...}.java  ← 手写实现接口
```

**为什么不是 openapi-generator-maven-plugin 直接生成 Controller 类？**

本仓选择 **接口替 Controller** 的中间方案：codegen 产物 = Java 接口 + DTO，**Controller 类本身手写**且 `implements` 接口，body 只调 `service.xxx()`。优势：

| 维度 | 评价 |
|---|---|
| 与 ADR-0007（DB SSOT）对称 | 路由 + DTO 全由 shared 仓的 TypeSpec 决定，本仓无法"擅自加 endpoint" |
| 业务自由度 | 手写 Controller 可以自由加 `@PreAuthorize` / 日志 / 异常处理 / 调 TenantGuard，不被 codegen 重写覆盖 |
| 与 aspnetcore 仓形态对称 | aspnetcore 用 `Generated/Controllers.cs` + `Implementation/<Tag>Controller.cs` 覆盖 abstract 方法，思路一致 |
| 测试 | 手写 Controller 易写 `@WebMvcTest`（不依赖 codegen 产物类结构） |

**禁止**：

- 禁止直接编辑 `src/main/java/saas/identity/shared/api/*.java` / `shared/dto/*.java`——下次 `gen-shared.sh` 重写丢失；
- 禁止 `controller/*.java` 写业务逻辑——只调 `service` 方法；
- 禁止在 `controller` 里 `new` 一个 Repository / Entity——依赖一律构造器注入；
- 禁止路由用 `@RequestMapping("/api/v1/admin/tenants")` 这种字符串硬编码——`@Override` 来自接口，路由与契约同步。

**Controller 清单 vs module 映射**（详见 function-tree §模块总览）：

| Controller | Function IDs | 路径特征 |
|---|---|---|
| `AuthController` | M03.F01-03 | `/api/v1/auth/**`（全局，permitAll，不走 TenantGuard） |
| `OauthController`（implements `OauthApi`） | M04.F02 | `/api/v1/oauth/{authorize,token}`（permitAll，身份靠 client_id/redirect_uri 校验） |
| `MeController` | M00.F02 | `/api/v1/me/**`（用 JWT tenant_id 上下文，不需要路径 tenantId） |
| `AdminTenantsController` | M00.F01 | `/api/v1/admin/tenants/**`（平台 admin scope） |
| `AdminAppsController` | M04.F01 | `/api/v1/admin/apps/**` |
| `AdminAppMenusController` | M04.F01 | `/api/v1/admin/apps/{id}/menus/**` |
| `TenantUsersController` | M01.F01-02 | `/api/v1/tenants/{tenantId}/users/**`（走 TenantGuard） |
| `TenantRolesController` | M02.F01 | `/api/v1/tenants/{tenantId}/roles/**`（走 TenantGuard） |
| `TenantRoleMenusController` | M02.F02 | `/api/v1/tenants/{tenantId}/roles/{roleId}/menus/**` |
| `TenantApiKeysController` | M05.F01 | `/api/v1/tenants/{tenantId}/api-keys/**` |
| `TenantAuditController` | M06.F01-02 | `/api/v1/tenants/{tenantId}/audit/**` |

### 3.3 Service 层（`service/`）

**手写**。所有业务逻辑在这里。Spring `@Service` + 构造器注入。

| 类 | 关键职责 | 依赖（构造器注入） |
|---|---|---|
| `AuthService` | M03 密码登录 / OIDC 回调 / refresh / logout（密码用 `plain:` 前缀 dev 期，Phase 5 接 argon2） | `UserRepository` / `TenantRepository` |
| `MeService` | M00.F02 whoami / 列租户成员关系 / 切换当前租户 | `TenantMembershipRepository` / `UserRepository` |
| `AdminTenantService` | M00.F01 平台 admin 租户 CRUD | `TenantRepository` + `TenantMapper` |
| `TenantUsersService` | M01 user CRUD + 角色分配 | `UserRepository` / `RoleRepository` / `TenantMembershipRepository` |
| `TenantRoleService` | M02 role CRUD | `RoleRepository` + `RoleMapper` |
| `TenantRoleMenuService` | M02 role↔menu grants | `RoleRepository` / `MenuRepository` / `RoleMenuGrantRepository` |
| `TenantApiKeyService` | M05 API Key 生命周期 | `ApiKeyRepository` + `ApiKeyMapper` |
| `TenantAuditService` | M06 审计事件查询 + 留存策略 | `AuditEventRepository` / `AuditRetentionPolicyRepository` + `AuditEventMapper` |
| `OauthService` | M04 真 OAuth 流程（authorize 签 code + token 交换 + refresh 旋转） | `AppRepository` / `TenantRepository` / `UserRepository` + `JwtIssuer` |

**关键约束**：

- 构造器注入**一律**——禁 `@Autowired` 字段注入（CLAUDE.md §2）；
- 业务方法可 `@Transactional`（如 `AuthService.login` 是 `readOnly=false` 默认）；
- 跨 Service 调用允许，但禁止 Service 内 `new` Repository；
- 吞异常**一律禁**（`catch (Exception) {}`）——让异常向上传到 Controller / Spring exception handler；
- mapper / converter 在 Service 边界做 DTO ↔ Entity 转换——Repository 只接 Entity。

### 3.4 Repository 层（`repository/`）

**Spring Data JPA 接口**。无实现类，框架代理生成。

```
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);
}
```

| 模式 | 示例 |
|---|---|
| 方法名查询（derived query） | `findByUsername` / `findByTenantIdAndStatus` |
| `@Query` JPQL | 复杂 join / 聚合 |
| 批量更新 | `@Modifying @Query("update ...")` + `@Transactional` |

**当前清单**（12 个 Repository，与 entity 一一映射）：

```
TenantRepository / TenantMembershipRepository / UserRepository
RoleRepository / RolePermissionRepository / RoleMenuGrantRepository
PermissionRepository / MenuRepository
AppRepository / ApiKeyRepository
AuditEventRepository / AuditRetentionPolicyRepository
```

**约束**：

- Repository 方法返回 / 入参**只接 Entity**，不接 DTO；
- 跨表 join 用 entity 关联（`@ManyToOne` / `@OneToMany`），不直接拼 SQL；
- 禁止 `nativeQuery=true` 写 PG 专属方言（本仓目标是镜像 shared SQL，但 SQL 是 DDL 不是 query）。

### 3.5 Model 层（`entity/` + `mapper/` + `converter/` + `enums/`）

**Entity（JPA）**：反射镜像 `shared/sql/migrations/V*.sql` 的表结构。

| Entity | 对应 SQL（shared） |
|---|---|
| `TenantEntity` | `V001__init_tenants.sql` |
| `UserEntity` / `TenantMembershipEntity` | `V002__init_users_memberships.sql` |
| `RoleEntity` / `PermissionEntity` / `RolePermissionEntity` | `V003__init_roles_permissions.sql` |
| `ApiKeyEntity` | `V004__init_api_keys.sql` |
| `AppEntity` / `MenuEntity` / `RoleMenuGrantEntity` | `V005__init_oauth_apps_menus.sql` |
| `AuditEventEntity` / `AuditRetentionPolicyEntity` | `V006__init_audit_events.sql` |
| 索引 / 重命名列 | `V007__indexes.sql` / `V008__users_role_ids_and_drop_redundant_index.sql` |

**Mapper**：手写 DTO ↔ Entity 转换（`TenantMapper.toDto(entity)` / `toEntity(dto)`），原因：避免 MapStruct / Lombok 引入额外构建复杂度。

**Converter**：JPA `AttributeConverter` 适配 PG 原生类型：

| Converter | 用途 |
|---|---|
| `UuidArrayConverter` | `List<UUID>` ↔ `uuid[]` PG 数组列 |
| `StringArrayConverter` | `List<String>` ↔ `text[]` |
| `EnumArrayConverter` | `List<EnumType>` ↔ 自定义 enum 数组 |

> **注意**（pom.xml:104-113）：本仓已引入 `hypersistence-utils-hibernate-63` 作为替代方案——`UuidArrayType` / `StringArrayType` 是 Hibernate 6 `UserType` 实现，正确声明 `Types.ARRAY + nullSafe Get/Set`，避免 `@Convert` 注解撞 `buildStaticUpdateGroup` 启动崩。`UuidArrayConverter` 仍保留为兼容路径。

**Enum + Converter**：业务枚举（如 `TenantStatus.ACTIVE / SUSPENDED`）配 `TenantStatusConverter implements AttributeConverter<TenantStatus, String>`，避免 JPA 把 enum ordinal 写进库（ordinal 重排会炸数据）。

**关键约束**：

- Entity 字段名 / 类型**严格镜像** shared SQL DDL；改 Entity 必须同步改 SQL（或反之）；
- Entity 不带业务方法（贫血模型）——业务在 Service；
- 禁止 Entity 之间双向关联跨聚合根（多租户隔离边界的越权来源）；
- `@Entity` / `@Table` / `@Column(name = "...")` name 一律 snake_case 镜像 SQL 列名。

### 3.6 DB Migration 层（`db/migration/`）

**真源**：`saas-identity-platform-shared/sql/migrations/V*.sql`（**shared 仓同时是 API 契约 + DB schema 真源**，见父仓 [ADR-0007](../../../docs/adr/0007-shared-sql-ssot.md)）。

**本仓 `db/migration/` = 只读镜像**。`scripts/gen-shared.sh` step 3/3 把 shared 的 `V*.sql` `cp` 进来。

**当前 `application.yml:38-45` 关键决策**：`spring.flyway.enabled: false`。

```yaml
spring:
  flyway:
    # Dev unblock：直接关 Flyway。saas_dev schema 已经被 AspNetCore + shared SQL 灌过，
    # Spring Boot 仓的 V001-V008 SQL 文件对 saas_dev 是 redundant（也会撞 already exists），
    # Flyway baseline-on-migrate + baseline-version 组合在 10.20.1 没干净跳过 V00X 的可靠路径。
    # 关掉 Flyway 后 schema 由 shared SQL 单一来源管理（CLAUDE.md ADR-0007），本仓只读不写。
    enabled: false
```

**为啥关 Flyway**：

1. **saas_dev 已被 aspnetcore + shared SQL 灌过**——Spring Boot 仓的 `V001-V008` 对 saas_dev 是 redundant；
2. **Flyway 10.20.1 没干净跳过 V00X 的可靠路径**——`baseline-on-migrate + baseline-version` 组合在 PostgreSQL 上不稳；
3. **DB schema SSOT = shared 仓**——本仓只读 schema，由 shared SQL 通过外部 `sync-db` 工具统一灌入 dev/prod。

**开启路径**（`application.yml:44` TODO）：

> TODO Phase 6：用 testcontainers + 干净 saas_test 启 Flyway 做 schema 漂移检测；或者把 V00X 改成 idempotent (IF NOT EXISTS) 让 Flyway baseline-on-migrate=true 真的能跳过。

**`pom.xml` 依赖**：本仓**仍**引入 `flyway-core` + `flyway-database-postgresql`（`pom.xml:58-65`）——Phase 6 启用前的依赖准备，且 Phase 6 测试容器需要。

**关键约束**：

- 禁止直接编辑 `db/migration/V*.sql`——下次 `gen-shared.sh` 会覆盖；
- 禁止新增 `V010+` 文件在本仓——必须先在 `saas-identity-platform-shared/sql/migrations/` 加，再跑 `gen-shared.sh` 同步；
- 禁止 dev 改 `spring.flyway.enabled=true` 前先把 V00X 全部幂等化——会破坏 saas_dev schema。

---

## 4. 核心流程：契约 → 本仓同步（`scripts/gen-shared.sh` 三步）

`scripts/gen-shared.sh` 是本仓与 shared 契约仓的唯一同步入口。**固定三步**（详见 §5 防越界）：

```
[shared] 修改 tsp/main.tsp 或 sql/migrations/V00N+1__*.sql
   ↓ git commit + push 到 shared 仓

[本地本仓] bash scripts/gen-shared.sh
   │
   ├─ step 1/3: (cd ../../saas-identity-platform-shared && npm run emit:openapi)
   │   触发 shared 仓 typespec emit，生成 generated/openapi/openapi.yaml
   │
   ├─ step 2/3: npx --yes @openapitools/openapi-generator-cli generate \
   │              -g spring -i <openapi.yaml> -o .openapi-tmp/java \
   │              --library spring-boot \
   │              --model-package saas.identity.shared.dto \
   │              --api-package saas.identity.shared.api \
   │              --additional-properties useTags=true,interfaceOnly=true,
   │                                    skipDefaultInterface=true,
   │                                    useBeanValidation=true,useSpringBoot3=true,
   │                                    dateLibrary=java8
   │   codegen 产物 → .openapi-tmp/java/
   │   mkdir -p saas/identity/shared/{dto,api} && rm -rf 旧产物
   │   cp .openapi-tmp/java/src/main/java/saas/identity/shared/{dto,api}/ → src/main/java/
   │   rm -rf .openapi-tmp
   │
   └─ step 3/3: cp ../../saas-identity-platform-shared/sql/migrations/V*.sql → src/main/resources/db/migration/
                （含 cmp 防护，见 §5）

   ↓ git add -A && git commit "chore(codegen): sync with shared <sha>" && git push

[本仓] mvn spring-boot:run -Dspring-boot.run.profiles=dev
   → DevJwtDecoder bean 激活
   → flyway.enabled=false（dev）
   → DB schema 由 shared SQL 灌入 saas_dev

[集成测试] curl -X POST http://localhost:8080/api/v1/auth/login
   → TenantGuard 不走（M03 auth 是全局）
   → JwtIssuer HS256 真签 access token（JWT_SIGNING_KEY ≥32B），prod profile 默认路径
   ↓
[前端联调] 前端 .env 切 NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
   → axios 带 JWT（HS256 真签发）请求
   → 后端 NimbusJwtDecoder 真验签（对称密钥）
   → 业务返回
```

**关键检查点**：

- codegen 必须用 `interfaceOnly=true + skipDefaultInterface=true`——只产接口与 DTO，**不产 Controller 实现**（详见 §3.2）；
- `useBeanValidation=true` 让生成的 DTO 带 `@NotNull` / `@Size` 等注解；
- `useSpringBoot3=true` 适配 Jakarta EE 命名空间（`jakarta.*` 不是 `javax.*`）；
- 生成完毕后 `controller/*.java` 必须**全部 `implements` 接口**——少 `implements` 会编译失败（codegen 改了签名本仓没跟上）；
- `db/migration/V*.sql` 拷完**不动**——任何 SQL 改动必须回 shared 仓（详见 §5）。

---

## 5. 与契约仓同步：cmp abort 防护

`scripts/gen-shared.sh` step 3/3 的**核心防护**——防 2026-08-26 lab 仓 `V014/V015` 撞号事故在 saas 仓重演。

### 5.1 8/26 lab V014/V015 撞号事故回顾

**事故**（详见父仓 [docs/ARCHITECTURE.md §3.1](../../../docs/ARCHITECTURE.md)）：

1. lab-shared 加 `V014__xxx.sql`；
2. 跨仓同步脚本**静默 cp** 到 lab-springboot 的 `db/migration/V014__xxx.sql`；
3. lab-springboot 启动 Flyway → **checksum mismatch**（与已应用的 V014 内容字节不一致）→ 容器起崩；
4. prod 502 看起来像"wait 太短"，根因是 schema 已应用但脚本静默改写。

**根因**：

- 拷贝脚本无"已存在且内容不同"的检查；
- 静默覆盖让"shared 与本仓已分叉"的事实被掩盖；
- 已应用的 DB 的 Flyway checksum 锁定 V 文件内容——任何"事后修改"都让生产库不可启。

### 5.2 本仓的防护：`cmp -s` abort

`scripts/gen-shared.sh:59-66`：

```bash
for f in "$SHARED_SQL"/V*.sql; do
  [ -e "$f" ] || continue
  base="$(basename "$f")"
  target="$ROOT/src/main/resources/db/migration/$base"
  if [ -e "$target" ] && ! cmp -s "$f" "$target"; then
    echo "[gen-shared] FATAL: migration diverged: $base differs between shared and this repo." >&2
    echo "[gen-shared]          refusing to overwrite (flyway checksum on applied DBs is locked)." >&2
    echo "[gen-shared]          resolve: converge shared to match this repo byte-for-byte, or drop this repo version knowingly." >&2
    exit 1
  fi
  cp "$f" "$target"
done
```

**行为**：

| 状态 | 动作 |
|---|---|
| 目标 `V00N.sql` 不存在 | 直接 `cp`（新增迁移，正常路径） |
| 目标 `V00N.sql` 存在且 `cmp -s` 字节一致 | `cp` 一次（幂等，正常路径） |
| 目标 `V00N.sql` 存在且 `cmp -s` 字节不一致 | **FATAL abort, exit 1**——要求人显式决定 |

**人显式决策路径**（abort 后）：

1. **选项 A**：shared 仓的 V00N 改回与本仓字节一致（"converge shared to match this repo"）；
2. **选项 B**：本仓已知删掉 `V00N.sql`，重建 dev/prod 库（"drop this repo version knowingly"）；
3. **选项 C**（不推荐）：本仓 `git revert` 退回到 shared 上一个版本，再 `gen-shared.sh`。

### 5.3 V 文件管理硬约束

| 约束 | 原因 |
|---|---|
| 禁止直接编辑本仓 `db/migration/V*.sql` | 下次 `gen-shared.sh` 覆盖；`cmp abort` 也不救（脚本对修改自己无感，只对 shared 来的 cp 比对） |
| 禁止在本仓新增 `V00N+1` 文件 | SSOT = shared 仓；本仓加的 V 文件 shared 仓同步时不知道，跨仓漂移 |
| 禁止改 shared 旧 V 文件 | V 文件只能新增不能修改（与 Flyway 一致；改旧 V 撞已应用库的 checksum） |
| 禁止关掉 `cmp -s` 检查 | 防 8/26 事故重演——它是唯一机器层防线 |

### 5.4 同步窗口期约束

跨仓同步必须**同一批 commit 推完**（详见父仓 [docs/ARCHITECTURE.md §5.1](../../../docs/ARCHITECTURE.md#51-改一次契约--三端同步codegen-链)）：

```
shared 仓 tag v<X>-<YYYYMMDD>  →  本仓 拉新 openapi.yaml  →  本仓 tag v<X>-<YYYYMMDD>  →  父仓 gitlink 推进
```

任何"一边指针新、一边指针旧"的窗口期都会让前端仓 + 后端仓契约不一致。

---

## 6. 决策索引

本仓架构决策分两层：**本仓特有** + **继承自父仓**。

### 6.1 本仓特有（待补 — 见 `docs/adr/README.md`）

`docs/adr/README.md` 当前只有占位：

> TBD: 决策记录。当前 ADR-0007 (DB SSOT) 与 ADR-0010 (shadcn-ui 对称) 在 shared 仓维护；springboot 仓专属 ADR 待补。

**已观察到但未落 ADR 的本仓决策**：

| 主题 | 当前落地 | ADR 缺口 |
|---|---|---|
| DevJwtDecoder = static 内部类 + 不放单独文件 | `config/SecurityConfig.java:123-165` | dev/prod 切换路径未文档化 |
| `JwtIssuer` HS256 + `JWT_SIGNING_KEY` env | `security/JwtIssuer.java` | key rotation / ≥32 bytes 强制 vs lab 仓对齐（stateful-cuddling-cherny.md） |
| `scripts/gen-shared.sh` step 3/3 `cmp -s` abort | `scripts/gen-shared.sh:59-66` | 8/26 撞号事故根因 + 防护设计 |
| Flyway `enabled: false` (dev) | `application.yml:38-45` | Phase 6 启用路径 + V 文件幂等化方案 |
| Hypersistence-utils vs `@Convert` 注解 | `pom.xml:104-113` | `@Convert` AttributeConverter 撞 `buildStaticUpdateGroup` 启动崩 |

**待写 ADR（建议编号续父仓 0015+）**：

| 建议 ADR | 主题 |
|---|---|
| ADR-0015 | DevJwtDecoder 设计：static 内部类 + `cmp -s` 字节 abort + 不验签 |
| ADR-0016 | JwtIssuer HS256 key 与 aspnetcore JwtIssuer 对称（≥32 bytes / 跨家族 secret 共享约束） |
| ADR-0017 | Flyway 关闭理由与 Phase 6 启用路径（V 幂等化 + testcontainers 漂移检测） |

### 6.2 继承自父仓 / shared 仓

| 引用 | 主题 | 本仓落地 |
|---|---|---|
| 父仓 [ADR-0001](../../../docs/adr/0001-suite-owns-l0-and-l5.md) | suite 保留 L0/L5 门 | 本仓 `.harness/stack.json` 只声明 L1-L4 |
| 父仓 [ADR-0003](../../../docs/adr/0003-function-tree-requires-human-approval.md) | 功能清单变更需人批 | 本仓改 F/I 必须先 `/tree-change` |
| 父仓 [ADR-0005](../../../docs/adr/0005-defense-in-depth-for-protected-paths.md) | 受保护路径纵深防御 | `.claude/hooks/` 不让改 + pre_bash_guard 拦 |
| 父仓 [ADR-0007](../../../docs/adr/0007-shared-sql-ssot.md) | shared 仓扩到双 SSOT | 本仓 schema = shared SQL 镜像；ORM 只反射（见 §3.6） |
| 父仓 [ADR-0012](../../../docs/adr/0012-msw-as-http-server.md) | msw 仓升级为独立 HTTP 服务 | 本仓 prod 走 HS256 JWT 验签；msw 仅 dev 走 `alg=none` 兜底（见 §3.1） |
| 隐含 ADR-0014 | env-driven 单 URL | 本仓 `.env.example` 提供 `DATABASE_NAME` / `JWT_*` / `SAAS_CORS_ALLOWED_ORIGINS` 模板 |
| 隐含 ADR-0011 | lab-vue M98 白名单镜像 | （saas 仓不涉及 lab-vue 豁免） |
| shared 仓 ADR-0010 | shared SQL DDL 是 schema 真源 | 本仓 `db/migration/V*.sql` = 只读 cp 镜像 |

---

## 7. 术语表

| 术语 | 含义 | 本仓落地 |
|---|---|---|
| **SSOT** | Single Source of Truth | shared 仓是 API + DB schema 真源；本仓只产 Java 实现（见 §3.6） |
| **BASE tree** | 契约仓的功能清单 | shared 仓 function-tree.md 只到 F；本仓 function-tree.md 镜像到 I |
| **codegen** | openapi-generator-cli 产物 | `saas/identity/shared/{api,dto}/`，每次 gen-shared.sh 重写 |
| **接口替 Controller** | codegen 产 `*Api.java` 接口 + 手写 `*Controller implements *Api` | 比"直接产 Controller 类"自由度高（详见 §3.2） |
| **dev JWT fixture** | dev-only `alg=none` + dev-placeholder sig（test/MSW 用） | `DevJwtDecoder.decode()` 只校验 exp，过期把 Jwt.exp 延长到 now+1h；prod profile **不加载** 此 bean（见 §3.1） |
| **TenantGuard** | 路径 tenantId vs JWT claim 校验 | 每个 tenant-scoped endpoint 第一行必调（见 §3.1） |
| **cmp abort** | `cmp -s` 字节比对失败时 `exit 1` | 防 8/26 lab V014/V015 撞号事故（见 §5） |
| **构造器注入** | 唯一允许的依赖注入方式 | 禁字段注入（CLAUDE.md §2）；Service / Controller / TenantGuard 全部构造器注入 |
| **Hypersistence-utils** | Hibernate 6 UserType 库（`io.hypersistence:hypersistence-utils-hibernate-63:3.9.0`） | 替代 `@Convert` AttributeConverter 处理 `uuid[]` / `text[]`（避免启动崩） |
| **springdoc-openapi** | `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0` | 在线 Swagger UI（`/swagger-ui.html`）+ runtime OpenAPI doc（`/v3/api-docs`） |
| **trace.json** | 测试命中 fn-ID 的清单 | `.state/trace.json` 由 `trace_cmd` 产（`.harness/stack.json`：`mvn -q test -DTRACE_MAP=1`） |
| **stack.json** | 项目自描述（栈 + 门配置） | 本仓只声明 L1（spotless）+ L2（spotbugs）+ L3（compile）+ L4（test） |

---

## 附录 A：与父仓 `docs/ARCHITECTURE.md` 的关系

本文档是**仓级**架构文档，与父仓 [docs/ARCHITECTURE.md](../../../docs/ARCHITECTURE.md) **互补**而非替代：

| 维度 | 父仓文档 | 本仓文档 |
|---|---|---|
| 范围 | suite 14 仓家族全景 | 本仓内部结构 / 模块 / 流程 |
| 受众 | 跨家族架构师 | 本仓开发者 |
| 章节颗粒度 | §4.4.1 springboot 一段 = 200 行 | 本文档独立 480 行，单独可读 |
| 决策 | 12 份父仓 ADR + 1 份隐含 ADR | 父仓 ADR 引用 + 本仓特有 ADR 占位（§6.1） |
| 流程 | §5.3 后端开发（springboot / aspnetcore 共用） | §4 本仓 `gen-shared.sh` 三步详细分解 |

**何时看哪个**：

- 想理解"为什么 Java + Spring Boot + 这套 codegen" → 父仓 §4.4.1 + ADR-0007；
- 想在本仓加新 Service / 加新 endpoint / 排查 Flyway 问题 → 本文档 §3 + §4 + §5；
- 想跨仓同步（改 shared 仓 TypeSpec 后三端对齐） → 父仓 §5.1 + 本文档 §4。

---

## 附录 B：与 `saas-identity-platform-aspnetcore` 后端仓的对照

两家后端仓**结构同构**、**实现异构**——共同消费 shared 契约，独立选型。

| 维度 | saas-springboot（本仓） | saas-aspnetcore |
|---|---|---|
| 语言 / 运行时 | Java 17 + Spring Boot 3.4 | C# / .NET 8 |
| 构建 | Maven | dotnet SDK + NSwag CLI |
| 持久化 | PostgreSQL + Hibernate 6 + Flyway（**dev 关闭**） | InMemoryStore（进程内 fixture，无真 DB） |
| Codegen 工具 | `@openapitools/openapi-generator-cli`（npx 调用） | NSwag |
| Codegen 产物形态 | `interface + dto`（手写 Controller implements） | `Generated/Controllers.cs`（NSwag 产 Controller 类 + abstract 方法）+ 手写 partial class 实现 |
| 安全 | Spring Security 6 + oauth2-resource-server + `DevJwtDecoder` | ASP.NET Core auth + `RequireSignedTokens=false` dev 分支 |
| JWT 真签发 | `JwtIssuer` HS256 + `JWT_SIGNING_KEY` env（Phase 6+） | `Security/JwtIssuer.cs` HS256 + `Jwt.SigningKey` env |
| TenantGuard | `TenantGuard.verifyPathTenant(pathTenantId)` | `TenantGuard.VerifyPathTenant(tenantId)` |
| 默认端口 | 8080 | 5000 |
| 静态检查 | SpotBugs 4.9.3.2（effort=Max, threshold=Low） + Spotless（googleJavaFormat） | NSwag analyzers + dotnet format |
| 测试框架 | JUnit5 + `@SpringBootTest` + `@DataJpaTest` + fnTest via `@Fn` | xUnit + `[CollectionBehavior(DisableTestParallelization)]` + fnTest |
| Schema 真源 | `db/migration/V*.sql` cp 自 shared（dev 关 Flyway） | 不持 SQL（InMemoryStore 无 schema）；ADR-0010 待落地 EF Migrations 镜像 SQL DDL |
| dev Profile | `mvn spring-boot:run -Dspring-boot.run.profiles=dev` 隐式激活 `DevJwtDecoder` | dev 分支在 `Program.cs` 显式：`RequireSignedTokens=false` |
| Prod 切换路径 | 删 `jwtDecoder()` bean + 配 `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` env | 切真实 JWKS + `Jwt.SigningKey` env + 删 `RequireSignedTokens=false` 分支 |
| OpenAPI 暴露 | springdoc-openapi 自动收 swagger v3 注解 → `/v3/api-docs` + `/swagger-ui.html` | NSwag 自动 → `/swagger/v1/swagger.json` + Swagger UI |
| CORS | `cors.allowed-origins`（env `SAAS_CORS_ALLOWED_ORIGINS` override） | `AddCors("NextDev")` 同 env 同源 |

**对称点**（保证跨后端仓契约兼容）：

- 路由与 DTO 完全一致（同一个 openapi.yaml codegen）；
- dev JWT payload 格式一致（`{sub, tenant_id, exp}`）；
- `TenantGuard` 行为一致（路径 tenantId vs JWT claim 不匹配 → 403 AccessDeniedException）；
- CORS allowlist 都读同一个 env `SAAS_CORS_ALLOWED_ORIGINS`；
- `scripts/gen-shared.sh` 三步形态对称（emit OpenAPI → 本地 codegen → cp SQL）；
- `cmp -s` abort 防护对称（lab 撞号事故泛化到所有后端仓）。

**不对称点**（保持各自栈原生）：

- springboot 真持久化到 PG（Flyway 关但 schema 真有）；aspnetcore 仅 InMemoryStore；
- springboot 编译期 codegen（npx CLI）；aspnetcore 编译期 codegen（NSwag MSBuild task）；
- 静态检查工具栈不同（SpotBugs vs NSwag analyzers）；
- 测试并行策略不同（JUnit5 默认并行 vs xUnit `DisableTestParallelization`）。

---

## 附录 C：典型陷阱（详见父仓 [memory/](../../../memory/)）

| 陷阱 | 后果 | 解法 |
|---|---|---|
| DevJwtDecoder 误带到 prod | prod 接受 alg:none → 任意伪造身份 | prod 删 bean + 配 JWKS env |
| `JWT_SIGNING_KEY` 写进 `.env.example` | secret 进 git 仓库 | deploy 脚本自举随机，`.env.example` 只放模板 |
| Flyway 启用前未把 V00X 全部幂等化 | `saas_dev` 撞 already exists → 启动崩 | 保持 `enabled: false` 直到 Phase 6 落 testcontainers 漂移检测 |
| 改本仓 `db/migration/V*.sql` 直接编辑 | 下次 `gen-shared.sh` 覆盖丢失 | 改 SQL 必须回 shared 仓 + `cmp -s` abort 防护 |
| 关掉 `cmp -s` 检查 | 8/26 lab V014/V015 撞号事故重演 | 永远保留 `cmp -s` abort |
| 字段注入 `@Autowired` | 测试期 `NullPointerException`（Spring bean 构造时机） | 一律构造器注入 |
| `controller/*.java` 写业务 | 业务与 codegen 耦合 → 下次 gen-shared 改签名炸编译 | Controller 只调 service |
| `repository/*.java` 写 JPQL native | 绑死 PG 专有方言 | 用 derived query + `@Query` JPQL |
| `@Convert(UuidArrayConverter)` 撞启动崩 | Hibernate 6 schema validator 不认 `AttributeConverter<…, UUID[]>` | 改用 `hypersistence-utils` 的 `UuidArrayType`（`@Type` 注解） |
| dev JWT 验签逻辑"复制到 prod" | 绕过真 JWKS 验签 | prod = 删 `DevJwtDecoder` bean，让 Spring Boot 自动配 `NimbusJwtDecoder` |
| `spring-boot:run` 不带 `-Dspring-boot.run.profiles=dev` | dev profile 未激活 → CORS / 数据源用 prod 默认 | dev 命令显式带 profile |
| `mvn` 用 surefire fork 模式未禁 | 并发跑测试时 InMemoryStore-style fixture 撞 `ConcurrentModificationException` | xUnit 用 `DisableTestParallelization`；JUnit5 用 `@Execution(SAME_THREAD)` 或 `@ResourceLock` |