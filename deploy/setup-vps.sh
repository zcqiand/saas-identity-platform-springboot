#!/bin/sh
# setup-vps.sh — VPS 一次性 bootstrap (Ubuntu/Debian) — saas-identity-platform-springboot
#
# 用法:
#   sudo sh deploy/setup-vps.sh saas-springboot.example.com [cert-basename]
#
# 同一台 VPS 上要先跑 saas-identity-platform-nextjs 的 setup-vps.sh（如果同时托管
# 两个服务, 或同仓 lab-nextjs 也占 nginx）。本脚本只负责 springboot 部分: 目录,
# deploy 用户, 不同 default_server。
#
# Spring Boot 是后端, 与 nextjs 一样需要 PostgreSQL 远程连接, 但本脚本**不**生成
# springboot.env（避免把数据库密码写进 setup）：springboot.env 由 deploy 脚本首启自举
# 时从环境 DATABASE_URL/USER/PASSWORD 读入。本脚本只保证:
#   1. apt 装 nginx、docker (如未装, 幂等)
#   2. 创建 deploy 用户 (key-only SSH) + 加进 docker 组
#   3. 建 /home/deploy/saas-identity-platform-springboot/
#   4. 渲染 deploy/nginx-vps.conf.example → /etc/nginx/sites-available/$DOMAIN
#   5. 启用 sites-enabled symlink; 删 Ubuntu 默认页避免 default_server 冲突
#   6. nginx -t && reload
#
# 你**还要做**的（不在脚本里）:
#   a) 把 .crt / .key 放到 /etc/nginx/ssl/your-cert.{crt,key}（复用 saas-nextjs 的 cert 则跳过）
#   b) 本地跑: ssh-copy-id -i ~/.ssh/id_ed25519_gh-deploy.pub deploy@VPS（saas-nextjs 已做则跳过）
#   c) saas-springboot repo 的 GitHub Repository Secrets 加:
#        DOCKER_USERNAME / DOCKER_PASSWORD / VPS_HOST / VPS_USER / VPS_SSH_KEY
#      以及首次 deploy 时 CLI 提供 DATABASE_URL_PROD（或 setup 后手工写 springboot.env）。

set -eu

DOMAIN="${1:-}"
if [ -z "$DOMAIN" ]; then
  echo "Usage: $0 <saas-springboot.example.com>" >&2
  exit 1
fi

BASE="/home/deploy/saas-identity-platform-springboot"

log() { printf '→ %s\n' "$*"; }

# ── 1. 系统包 ─────────────────────────────────────
if ! command -v nginx >/dev/null 2>&1; then
  log "install nginx"
  apt-get update
  apt-get install -y nginx
fi
if ! command -v docker >/dev/null 2>&1; then
  log "install docker.io"
  apt-get install -y docker.io
fi

# ── 2. deploy 用户（无密码、SSH key only）─────────
if ! id deploy >/dev/null 2>&1; then
  log "create deploy user"
  adduser --disabled-password --gecos "" --shell /bin/bash deploy
fi
log "ensure deploy in docker group"
usermod -aG docker deploy

# ── 3. 部署目录 ───────────────────────────────────
# springboot 用 PostgreSQL 远程, 容器内不需要 data/ 卷。只建工作目录即可。
log "create $BASE"
sudo -u deploy mkdir -p "$BASE"

# cert 目录占位
mkdir -p /etc/nginx/ssl
chmod 700 /etc/nginx/ssl

# ── 4. 渲染 nginx vhost template ──────────────────
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEMPLATE="${SCRIPT_DIR}/nginx-vps.conf.example"
if [ ! -f "$TEMPLATE" ]; then
  echo "Missing template: $TEMPLATE" >&2
  echo "Either run this from the deploy/ directory or git checkout first." >&2
  exit 2
fi

# CERT_BASENAME: cert 文件 basename（不含扩展名），默认 your-cert。
# 用环境变量或第 2 个位置参数覆盖。cert/key 必须在 /etc/nginx/ssl/${CERT_BASENAME}.{crt,key}。
CERT_BASENAME="${CERT_BASENAME:-${2:-your-cert}}"
log "render → /etc/nginx/sites-available/${DOMAIN} (cert=${CERT_BASENAME})"
TARGET="/etc/nginx/sites-available/${DOMAIN}"
sed \
  -e "s/saas.YOUR_DOMAIN/${DOMAIN}/g" \
  -e "s|/etc/nginx/ssl/your-cert.cert|/etc/nginx/ssl/${CERT_BASENAME}.cert|g" \
  -e "s|/etc/nginx/ssl/your-cert.key|/etc/nginx/ssl/${CERT_BASENAME}.key|g" \
  "$TEMPLATE" > "$TARGET"

# ── 5. 启用 + 解决 default_server 冲突 ─────────────
# springboot vhost 不持有 default_server（同 VPS 兄弟仓若占了 :80 default_server,
# springboot 这边重复声明 nginx -t 会直接报错 —— 务必先装 saas 并把 default 占位收掉,
# 或确认其他仓也明确不加 default_server）。
log "enable site, drop sites-enabled/default if no other default_server holder"
ln -sf "$TARGET" "/etc/nginx/sites-enabled/${DOMAIN}"
# 仅在 sites-enabled/default 真实存在 且 没有别的 vhost 持有 default_server 时删。
# 有的话留给那个 vhost 的 default_server 声明继续生效（防裸 IP 暴露归那个 vhost 管）。
if [ -f /etc/nginx/sites-enabled/default ] && ! grep -l "default_server" /etc/nginx/sites-enabled/* 2>/dev/null | grep -v "${DOMAIN}" >/dev/null; then
  rm -f /etc/nginx/sites-enabled/default
fi

# ── 6. nginx 检查 + reload ────────────────────────
log "nginx -t"
nginx -t
log "reload"
systemctl reload nginx

log "saas-springboot VPS 配置完成"
log "剩下手工:"
log "  1) cert: /etc/nginx/ssl/${CERT_BASENAME}.{crt,key}（复用 saas-nextjs 的可跳过）"
log "  2) ssh-copy-id -i ~/.ssh/id_ed25519_gh-deploy.pub deploy@\$(hostname -I | awk '{print \$1}')（saas-nextjs 已做可跳过）"
log "  3) saas-springboot repo GitHub Secrets: DOCKER_USERNAME / DOCKER_PASSWORD / VPS_HOST / VPS_USER / VPS_SSH_KEY"
log "  4) 首次 deploy 前, 在 deploy 上下文提供 DATABASE_URL/USER/PASSWORD env，deploy 脚本会自举 springboot.env"
log "     或 deploy 后手工编辑 \$BASE/springboot.env（SPRING_DATASOURCE_URL/USERNAME/PASSWORD + SAAS_CORS_ALLOWED_ORIGINS）"
log "  5) prod 路径: 后续需要删 SecurityConfig.DevJwtDecoder + 设 SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI env"
