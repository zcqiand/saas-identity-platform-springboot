# SaaS 多租户多应用身份平台 · Spring Boot 后端

SaaS 身份平台的 Java 后端 —— codegen Controller + 手写 Service，对接 PostgreSQL（Flyway 真源在 shared 仓）。

本仓为《Spring Boot 从入门到项目实践》（亚马逊电子书）案例二「SaaS 统一身份管理系统」（第 47-56 章）的可运行配套工程，是书稿代码块的 **source of truth**。

## 快速开始

```bash
mvn verify                    # 全量测试（含 Spotless / SpotBugs）
bash scripts/gen-shared.sh    # 改了 shared 仓后同步 codegen 产物
mvn spring-boot:run           # 本地起服务
```

## 功能特性

- Controller 与 DTO 由 shared 仓 TypeSpec codegen 全覆盖（openapi-generator）
- 手写 Service 与 Repository；TenantGuard 多租户校验
- OAuth2 resource server（JWT）；prod 走 issuer-uri，dev 走 DevJwtDecoder（dev-only）

## 技术栈

| 技术 | 版本 |
| :--- | :--- |
| Java | 21 |
| Spring Boot | 3.4.0 |
| Flyway | 随 spring-boot-starter-parent |
| PostgreSQL driver | 随 spring-boot-starter-parent |
| JUnit 5 | 随 starter-test |
| Maven | 3.9+ |

> 依赖版本与 `version-lock.json` 的 `version_lock` 一致，不引入 lock 外的库。

## 配套书籍及章节映射

> 同一案例仓后续接入其他书籍时，在此节下新增书籍小节。

### 书一《Spring Boot 从入门到项目实践》（亚马逊电子书）

- 书稿基线：tag `v0.2.34-20260926`（冻结，正文代码清单以此为准）
- 书稿定位：案例二「SaaS 统一身份管理系统」，覆盖第 47-56 章

| 章 | 主题 | 对应源文件 |
| :--- | :--- | :--- |
| 47 | 多租户架构设计：租户模型与数据层落点 | `src/main/java/saas/identity/platform/entity/Generated/Tenant.java`、`src/main/java/saas/identity/platform/repository/TenantRepository.java`、`src/main/java/saas/identity/platform/security/TenantGuard.java` |
| 48 | 平台租户管理与应用订阅：Admin API 设计 | `src/main/java/saas/identity/platform/controller/AdminTenantsController.java`、`src/main/java/saas/identity/platform/controller/TenantApplicationsController.java` |
| 49 | 成员管理与密码登录：JWT 签发与失败锁定 | `src/main/java/saas/identity/platform/controller/TenantMembersController.java`、`src/main/java/saas/identity/platform/controller/AuthController.java`、`src/main/java/saas/identity/platform/security/JwtIssuer.java` |
| 50 | 租户上下文与数据隔离：TenantContext 实现行级隔离 | `src/main/java/saas/identity/platform/security/TenantContext.java`、`src/main/java/saas/identity/platform/controller/MeController.java`、`src/main/java/saas/identity/platform/repository/TenantMemberRepository.java` |
| 51 | OAuth2 授权码模式：authorize 与 token 端点实现 | `src/main/java/saas/identity/platform/controller/OauthController.java`、`src/main/java/saas/identity/platform/controller/TokenIssuer.java`、`src/main/java/saas/identity/platform/repository/OauthCodeRepository.java` |
| 52 | OAuth 应用管理：client 注册、元数据与启停 | `src/main/java/saas/identity/platform/controller/AdminClientsController.java`、`src/main/java/saas/identity/platform/repository/OauthClientRepository.java`、`src/main/java/saas/identity/platform/config/SecurityConfig.java` |
| 53 | 审计基建：时间戳拦截器与审计字段规范 | `src/main/java/saas/identity/platform/config/AuditTimestampInterceptor.java`、`src/main/java/saas/identity/platform/config/HibernateAuditConfig.java` |
| 54 | 菜单与权限矩阵：角色授权与有效菜单查询 | `src/main/java/saas/identity/platform/entity/Generated/SysMenu.java`、`src/main/java/saas/identity/platform/repository/SysRoleMenuRepository.java`、`src/main/java/saas/identity/platform/controller/TenantRoleMenusController.java` |
| 55 | Docker 部署与交付物：从镜像到 VPS 上线 | `Dockerfile`、`deploy/setup-vps.sh`、`deploy/saas-identity-platform-springboot.sh` |
| 56 | 项目总结：SaaS 身份平台的复盘与运维清单 | — |

### 书二《PostgreSQL DBA 实战：从开发到数据库专家的转型指南》（第二部分）

- 书稿定位：xr-know-007，第 37、38、39、40、41、42 章正文代码块挂本仓 `source=` 锚点
- 配套版本：`v0.2.34-20260926`（registry 冻结 tag，代码以此为准）

| 章 | 章题 | 对应源文件 |
| :--- | :--- | :--- |
| 37 | 实战二开卷：SaaS 身份平台的合规分析与容灾架构设计 | `src/main/java/saas/identity/platform/repository/TenantRepository.java` + `src/main/java/saas/identity/platform/controller/TenantMembersController.java` + `src/main/java/saas/identity/platform/repository/SysUserRepository.java` + `src/main/java/saas/identity/platform/repository/SysRoleRepository.java` + `src/main/java/saas/identity/platform/controller/TenantApplicationsController.java` |
| 38 | 实战二第二章：身份平台的数据安全三件套 | `src/main/java/saas/identity/platform/controller/AuthController.java` + `src/main/java/saas/identity/platform/security/JwtIssuer.java` + `src/main/java/saas/identity/platform/config/HibernateAuditConfig.java` + `src/main/java/saas/identity/platform/controller/TenantMembersController.java` + `src/main/java/saas/identity/platform/config/AuditTimestampInterceptor.java` |
| 39 | 实战二第三章：令牌数据的一致性与跨中心复制 | `src/main/java/saas/identity/platform/controller/OauthController.java` + `src/main/java/saas/identity/platform/repository/OauthCodeRepository.java` + `src/main/java/saas/identity/platform/controller/TokenIssuer.java` + `src/main/java/saas/identity/platform/repository/OauthRefreshTokenRepository.java` + `src/main/java/saas/identity/platform/repository/TenantMemberRepository.java` + `src/main/java/saas/identity/platform/security/TenantContext.java` |
| 40 | 实战二第四章：全链路压测与故障演练 | `src/main/java/saas/identity/platform/repository/SysMenuRepository.java` + `src/main/java/saas/identity/platform/controller/MeController.java` + `src/main/java/saas/identity/platform/controller/ClientsController.java` |
| 41 | 实战二第五章：运维自动化与 SOP 手册 | `src/main/java/saas/identity/platform/controller/AdminTenantsController.java` + `src/main/java/saas/identity/platform/controller/AdminClientsController.java` + `src/main/java/saas/identity/platform/controller/TenantApplicationsController.java` |
| 42 | 终章：DBA 能力模型与职业路径 | — |
## 快速链接

- [CLAUDE.md](CLAUDE.md) — 开发约定与编码规范
- [架构](ARCHITECTURE.md) — DeepWiki 风格七章速览（结构 / 边界 / 数据流 / 门禁）
- [功能规格.md](docs/functions/function-tree.md) — 功能名称、描述与验收标准
- [未来开发计划](PLAN.md) — 待办与迭代方向
- [更新日志](CHANGELOG.md) — 版本变更记录
