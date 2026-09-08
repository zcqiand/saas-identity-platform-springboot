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

cd "$(git rev-parse --show-toplevel)"

echo "[scaffold-entities] step 1/3 — node scripts/scaffold-entities.mjs"
node scripts/scaffold-entities.mjs

echo "[scaffold-entities] step 2/3 — git diff entity/Generated/"
if ! git diff --exit-code --quiet src/main/java/saas/identity/platform/entity/Generated/ 2>/dev/null; then
  echo "[scaffold-entities] FATAL: scaffold 产物与 git HEAD 不一致" >&2
  echo "[scaffold-entities]        处理：确认 DB 是最新（shared 已 db:migrate），" >&2
  echo "[scaffold-entities]        然后 git add src/main/java/saas/identity/platform/entity/Generated/ && git commit" >&2
  exit 1
fi

echo "[scaffold-entities] step 3/3 — OK"
echo "[scaffold-entities]    entity 类已与 DB 同步；DB-First sync 绿"

# ADR-0026 §2: 写 last-gen-shared.json marker（DB 类别），失败不阻塞 scaffold。
SHARED_DIR="$(cd "$(git rev-parse --show-toplevel)/../saas-identity-platform-shared" && pwd)"
ROOT="$(git rev-parse --show-toplevel)"
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

shas = [s for s in (marker.get("api_synced_sha"), marker.get("db_synced_sha")) if s]
marker["shared_sha"] = max(shas) if shas else shared_sha
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
