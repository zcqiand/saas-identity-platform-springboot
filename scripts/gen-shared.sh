#!/bin/bash
# Generate Java client locally from shared's OpenAPI.yaml.
#
# Architecture (ADR-0007 + ADR-0025):
# - shared 仓是 schema-first 双 SSOT 仓（TypeSpec → OpenAPI.yaml + Drizzle TS → SQL）
# - 本仓走 DB-First：entity 由 hibernate-tools 反向工程从真库 scaffold（见 scripts/scaffold-entities.sh）
# - 不再跑 Flyway；schema 由 shared 仓 drizzle-kit migrate 统一管理

set -euo pipefail

SHARED_DIR="$(cd "$(dirname "$0")/../../saas-identity-platform-shared" && pwd)"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEST="$ROOT/src/main/java"

echo "[gen-shared] step 1/2 — shared: emit OpenAPI.yaml..."
(cd "$SHARED_DIR" && npm run emit:openapi)

OPENAPI="$SHARED_DIR/generated/openapi/openapi.yaml"
if [ ! -f "$OPENAPI" ]; then
  echo "[gen-shared] ERROR: missing $OPENAPI" >&2
  exit 1
fi

echo "[gen-shared] step 2/2 — springboot: openapi-generator → src/main/java/..."

# Use npx to resolve @openapitools/openapi-generator-cli (matches shared 仓's config).
# Config mirrors emit-java.ts in shared (was): spring-boot library, interfaceOnly,
# useSpringBoot3, etc.
npx --yes @openapitools/openapi-generator-cli generate \
  -g spring \
  -i "$OPENAPI" \
  -o "$ROOT/.openapi-tmp/java" \
  --library spring-boot \
  --model-package saas.identity.shared.dto \
  --api-package saas.identity.shared.api \
  --invoker-package saas.identity.shared \
  --additional-properties useTags=true,interfaceOnly=true,skipDefaultInterface=true,useBeanValidation=true,useSpringBoot3=true,dateLibrary=java8

# Move generated dto + api into the springboot source tree.
mkdir -p "$DEST/saas/identity/shared/dto" "$DEST/saas/identity/shared/api"
rm -rf "$DEST/saas/identity/shared/dto"/* "$DEST/saas/identity/shared/api"/*
# 清掉老路径(b67419b 之前 codegen 产物错误 cp 到 saas/identity/platform/api/, 让 java package
# saas.identity.shared.api 与目录 saas/identity/platform/api/ 不匹配, javac 找不到所有
# AdminAppMenusApi 等 controller 接口 — start-family.sh 实测发现. 现已迁回 saas/identity/shared/api/)
rm -rf "$DEST/saas/identity/platform/api"
cp -r "$ROOT/.openapi-tmp/java/src/main/java/saas/identity/shared/dto/." "$DEST/saas/identity/shared/dto/"
cp -r "$ROOT/.openapi-tmp/java/src/main/java/saas/identity/shared/api/." "$DEST/saas/identity/shared/api/"
rm -rf "$ROOT/.openapi-tmp"

echo "[gen-shared] OK"
echo "[gen-shared]    DB schema 同步请跑: bash scripts/scaffold-entities.sh（共享已 db:migrate 之后）"