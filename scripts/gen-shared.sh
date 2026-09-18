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

# ADR-0026 §2: 写 last-gen-shared.json marker，供 suite 跨仓 staleness check 使用。
# 失败不阻塞 gen-shared.sh —— staleness 是 warning（V1 档）不是 build blocker。
# 失败时 suite 会报 UNKNOWN 让 reviewer 看到，而不是悄悄丢失同步信号。
SHARED_SHA=$(cd "$SHARED_DIR" && git rev-parse HEAD)
MARKER="$ROOT/.state/last-gen-shared.json"
mkdir -p "$ROOT/.state"

if python3 - "$MARKER" "$SHARED_SHA" "$(basename "$0")" "$(basename "$ROOT")" <<'PYEOF'
import datetime, json, sys

marker_path, shared_sha, cmd, repo = sys.argv[1:5]
try:
    with open(marker_path, encoding="utf-8") as f:
        marker = json.load(f)
except (FileNotFoundError, json.JSONDecodeError):
    marker = {}

now = datetime.datetime.now(datetime.timezone.utc).isoformat()
if cmd.startswith("gen-shared"):
    marker["api_synced_sha"] = shared_sha
    marker["api_synced_at"] = now
    marker["api_synced_cmd"] = cmd
elif cmd.startswith("scaffold"):
    marker["db_synced_sha"] = shared_sha
    marker["db_synced_at"] = now
    marker["db_synced_cmd"] = cmd

# shared_sha 取「最近一次同步」对应的 sha：ISO-8601 UTC 时间戳字典序==时间序。
# 勿用 max(sha)——SHA 字典序不是 git 时间序（5.21 事故）。
entries = [
    (marker.get(k + "_at", ""), marker[k + "_sha"])
    for k in ("api_synced", "db_synced")
    if marker.get(k + "_sha")
]
marker["shared_sha"] = max(entries)[1] if entries else shared_sha
marker["consumer_repo"] = repo

with open(marker_path, "w", encoding="utf-8") as f:
    json.dump(marker, f, ensure_ascii=False, indent=2)
    f.write("\n")
PYEOF
then
  echo "[gen-shared]    ADR-0026 marker 已落盘: $MARKER (shared HEAD ${SHARED_SHA:0:7})"
else
  echo "[gen-shared]    WARN: marker 写失败（python3 缺失？权限？）—— staleness 将报 UNKNOWN" >&2
fi