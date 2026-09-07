#!/usr/bin/env bash
# scripts/scaffold-entities.sh — Hibernate 反向工程从真库生成 JPA entity 类（DB-First, ADR-0025）
#
# 设计：
# - shared 仓 schema-first（src/db/schema.ts）是 SSOT；drizzle-kit migrate 应用到 DB
# - springboot 仓不手写 entity；本脚本跑 Hibernate 反向工程从真库重生 entity
# - 输出：src/main/java/saas/identity/platform/entity/Generated/*.java
# - 手写 entity（手维护的 @PreUpdate 等业务逻辑）通过 extends Generated.TenantEntity 叠加
#
# 用法：
#   bash scripts/scaffold-entities.sh                       # default saas_dev @ 100.79.128.25
#   DATABASE_URL=postgresql://... bash scripts/scaffold-entities.sh
#
# 前提：
# - shared 仓已 `npm run db:migrate`（目标 DB schema 与 src/db/schema.ts 一致）
# - mvn 本地可跑（首次会下 hibernate-tools 依赖）
# - 实际运行受 Hibernate 6 + hibernate-tools-maven-plugin 兼容性约束；
#   若 plugin 跑不通，可改用本仓 scripts/ 下的 jdbc-scaffold.mjs（JDBC 直读 schema 生成）
#
# 退出码：
#   0 — scaffold OK
#   1 — DB 不通 / mvn 失败 / 产物与 git HEAD drift

set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

PG_HOST="${PG_HOST:-100.79.128.25}"
PG_PORT="${PG_PORT:-5432}"
PG_DATABASE="${PG_DATABASE:-saas_dev}"
PG_USER="${PG_USER:-postgres}"
PG_PASSWORD="${PG_PASSWORD:-}"

if [ -z "$PG_PASSWORD" ]; then
  echo "[scaffold-entities] WARN: PG_PASSWORD 未设；走 ~/.pgpass 或环境 DATABASE_URL"
fi

echo "[scaffold-entities] step 1/3 — mvn hibernate-tools:hbm2java"
mvn -q hibernate-tools:hbm2java \
    -Dhibernate.connection.url="jdbc:postgresql://${PG_HOST}:${PG_PORT}/${PG_DATABASE}" \
    -Dhibernate.connection.user="$PG_USER" \
    -Dhibernate.connection.password="$PG_PASSWORD" \
    -Dhibernate.reveng.xml=src/main/resources/hibernate.reveng.xml \
    -Dhibernate.tool.hbm2java.output.dir=src/main/java/saas/identity/platform/entity/Generated

echo "[scaffold-entities] step 2/3 — git diff src/main/java/saas/identity/platform/entity/Generated/"
if ! git diff --exit-code --quiet src/main/java/saas/identity/platform/entity/Generated/ 2>/dev/null; then
  echo "[scaffold-entities] FATAL: scaffold 产物与 git HEAD 不一致" >&2
  echo "[scaffold-entities]        处理：确认 DB 是最新（shared 已 db:migrate），" >&2
  echo "[scaffold-entities]        然后 git add src/main/java/saas/identity/platform/entity/Generated/ && git commit" >&2
  exit 1
fi

echo "[scaffold-entities] step 3/3 — OK"
echo "[scaffold-entities]    entity 类已与 DB 同步；DB-First sync 绿"
