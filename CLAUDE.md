# CLAUDE.md — SaaS身份平台SpringBoot后端

> 书稿配套仓 + harness 门禁仓双身份。入口，不是手册。L0 门强制上限 60 行。
> 本仓为《（书稿信息待补）》案例（待补）的可运行配套工程，是书稿代码块的 **source of truth**。

## 1. 项目定位

SaaS 多租户多应用身份平台的 Java 后端。Controller 与 DTO 由 shared 仓 TypeSpec codegen 全覆盖；
手写 Service 与 Repository。对接 PostgreSQL（Flyway 迁移真源在 shared 仓）。

## 2. 铁律

- **TDD**：先写失败测试 → 确认红 → 实现 → 确认绿 → commit
- **版本钉死**：依赖与 `version-lock.json` 的 `version_lock` 一致；不引入 lock 外的库
- **tag 即放行**：全量回归绿后打 `v<MAJOR>.<MINOR>.<PATCH>-<YYYYMMDD>`（如 `v0.2.4-20260826`）
- **功能清单是锚点**：改 function-tree 走 `/tree-change`；同 commit；废弃只改状态，编号不复用
- 禁止手写 Controller（codegen 替）
- 禁止字段注入；一律构造器注入
- 禁止跳过 TenantGuard 校验

## 3. 技术栈与版本（钉死于 version-lock.json）

Java 21 + Spring Boot 3.4 + Maven + JUnit5 + Spotless + SpotBugs。明细见 `version-lock.json`。

门禁命令见 `.harness/stack.json`。**不要改它来让门变松。**

## 4. 验收

- suite 根目录跑 `python scripts/gate.py -p saas-identity-platform-springboot`
- 改了 shared → `bash scripts/gen-shared.sh` 再跑门禁

## 5. 指向别处

- 契约真源 → `../saas-identity-platform-shared`
- 决策 → `docs/adr/`；细则 → `docs/conventions/`；待办 → `PLAN.md`；版本 → `CHANGELOG.md`

## 6. 工作循环

1. 改 Service（`src/main/java/.../service/*.java`）
2. gate exit 1 修；exit 2 停下问人
3. `/handoff` 更新 `.state/session.json`
