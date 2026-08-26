# SaaS 多租户多应用身份平台 · Spring Boot 后端

SaaS 身份平台的 Java 后端 —— codegen Controller + 手写 Service，对接 PostgreSQL（Flyway 真源在 shared 仓）。

本仓为《（书稿信息待补）》案例（待补）的可运行配套工程，是书稿代码块的 **source of truth**。

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

| 章 | 主题 | 对应源文件 |
| :--- | :--- | :--- |
| （待补） | | |

## 快速链接

- [CLAUDE.md](CLAUDE.md) — 开发约定与编码规范
- [系统架构.md](docs/ARCHITECTURE.md) — 结构 / 边界 / 数据流 / 决策
- [功能规格.md](docs/functions/function-tree.md) — 功能名称、描述与验收标准
- [未来开发计划](PLAN.md) — 待办与迭代方向
- [更新日志](CHANGELOG.md) — 版本变更记录
