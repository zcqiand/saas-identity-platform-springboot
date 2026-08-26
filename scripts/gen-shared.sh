#!/bin/bash
# Generate Java client locally from shared's OpenAPI.yaml.
#
# Architecture change (v0.2.0): shared 仓 is now a pure contract source
# (TypeSpec → OpenAPI.yaml only). Language-specific clients are generated
# per consuming project. This script invokes openapi-generator-cli directly
# instead of copying pre-generated Java sources from shared/generated/java/.
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
mkdir -p "$DEST/saas/identity/shared/dto" "$DEST/saas/identity/platform/api"
rm -rf "$DEST/saas/identity/shared/dto"/* "$DEST/saas/identity/platform/api"/*
cp -r "$ROOT/.openapi-tmp/java/src/main/java/saas/identity/shared/dto/." "$DEST/saas/identity/shared/dto/"
cp -r "$ROOT/.openapi-tmp/java/src/main/java/saas/identity/shared/api/." "$DEST/saas/identity/platform/api/"
rm -rf "$ROOT/.openapi-tmp"

# M09.Database (ADR-0007) — DB SQL SSOT 落地
# 防护（2026-08-26 lab 仓 V014/V015 撞号事故教训泛化）：
# 拷贝前若目标同名迁移已存在且内容不同，直接 abort —— 那意味着本仓 flyway 链
# 已与 shared 分叉，静默覆盖会让已应用库 checksum mismatch、容器起崩、prod 502。
# 分叉必须显式处理：要么 shared 收敛对齐本仓（逐字节），要么本仓 knowingly 回退
# 到 shared 版本并接受重建库。绝不静默 cp 覆盖。
echo "[gen-shared] step 3/3 — DB: copy shared/sql/migrations/* → src/main/resources/db/migration/"
SHARED_SQL="$SHARED_DIR/sql/migrations"
if [ -d "$SHARED_SQL" ]; then
  mkdir -p "$ROOT/src/main/resources/db/migration"
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
  [ -f "$SHARED_SQL/README.md" ] && cp "$SHARED_SQL/README.md" "$ROOT/src/main/resources/db/migration/README.md"
else
  echo "[gen-shared] WARN: $SHARED_SQL not found; DB layer skipped"
fi

echo "[gen-shared] OK"