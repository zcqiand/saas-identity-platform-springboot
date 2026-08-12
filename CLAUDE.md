# saas-identity-platform-springboot

> Java 17 + Spring Boot 3.4 + Maven + JUnit5。消费 shared 仓 TypeSpec codegen 产物（Java POJO + Controller）。

## 1. 这是什么

saas-identity-platform 的 Java 后端。Controller 与 DTO 由 codegen 全覆盖；手写 Service 与 Repository。

## 2. 禁止事项

- 禁止手写 Controller（codegen 替）
- 禁止字段注入；一律构造器注入
- 禁止跳过 TenantGuard 校验

## 3. 指向别处

- shared 仓：`../saas-identity-platform-shared`
- function-tree：`docs/functions/function-tree.md`

## 4. 工作循环

1. 改 Service（`src/main/java/.../service/*.java`）
2. `bash scripts/gen-shared.sh`（如改了 shared）
3. `python scripts/gate.py -p saas-identity-platform-springboot`
