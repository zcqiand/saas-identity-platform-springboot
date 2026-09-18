#!/usr/bin/env bash
# scripts/scaffold-entities.sh — JDBC 反向工程从真库生成 JPA entity（DB-First, ADR-0025）
#
# 设计：
# - 取代原 hibernate-tools-maven-plugin 路径（与 Hibernate 6 集成不稳定）
# - 实际实现：scripts/scaffold-entities.mjs（pg driver + JDBC 直读 information_schema）
# - 输出：src/main/java/saas/identity/platform/entity/Generated/<Table>.java
# - 手写 @PreUpdate / @Convert 等业务逻辑通过 extends Generated.<Entity> 叠加（ADR-0025 D7）
#
# 用法：
#   bash scripts/scaffold-entities.sh                       # default saas_dev @ 100.79.128.25
#   DATABASE_URL=postgresql://... bash scripts/scaffold-entities.sh
#
# 退出码：
#   0 — scaffold OK
#   1 — DB 不通 / 生成失败

set -euo pipefail

# 不要用 `git rev-parse --show-toplevel` —— 本仓是 submodule，
# 该命令返回外层 xr-code-suite 根而不是本仓根，让 Generated/ 路径算错。
# 用脚本自身所在目录的父目录锚定到仓根。
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/.."

echo "[scaffold-entities] step 1/4 — node scripts/scaffold-entities.mjs"
node scripts/scaffold-entities.mjs

# step 2/4: scaffold 产出的 Generated/*.java 不带 google-java-format 格式（jdbc 反推是裸 Java）。
# 直接跑 mvn spotless:apply 重排版，否则后续 gate L1 格式门会因 scaffold 触发的 spotless cache 失败。
# clear spotless-index 防止上次缓存命中导致 apply 跳过。
echo "[scaffold-entities] step 2/4 — mvn spotless:apply（消除 scaffold 触发的 L1 格式漂移）"
rm -f target/spotless-index && mvn spotless:apply -q

echo "[scaffold-entities] step 3/4 — git diff entity/Generated/"
if ! git diff --exit-code --quiet src/main/java/saas/identity/platform/entity/Generated/ 2>/dev/null; then
  echo "[scaffold-entities] FATAL: scaffold 产物与 git HEAD 不一致" >&2
  echo "[scaffold-entities]        处理：确认 DB 是最新（shared 已 db:migrate），" >&2
  echo "[scaffold-entities]        然后 git add src/main/java/saas/identity/platform/entity/Generated/ && git commit" >&2
  exit 1
fi

echo "[scaffold-entities] step 4/4 — OK"
echo "[scaffold-entities]    entity 类已与 DB 同步；DB-First sync 绿"

# ADR-0026 §2: 写 last-gen-shared.json marker（DB 类别），失败不阻塞 scaffold。
ROOT="$(pwd)"
SHARED_DIR="$(cd "${ROOT}/../saas-identity-platform-shared" && pwd)"
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
  echo "[scaffold-entities]    ADR-0026 marker 已落盘: $MARKER (shared HEAD ${SHARED_SHA:0:7})"
else
  echo "[scaffold-entities]    WARN: marker 写失败（python3 缺失？）—— staleness 将报 UNKNOWN" >&2
fi
