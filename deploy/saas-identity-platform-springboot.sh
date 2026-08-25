#!/bin/sh
# Usage: saas-identity-platform-springboot.sh <DOCKER_USERNAME> <DOCKER_PASSWORD> [VERSION]
#
# 由 .github/workflows/ci.yml 的 deploy job 远程调用:
#   ssh deploy@vps -- cd /home/deploy/saas-identity-platform-springboot
#                    && sh saas-identity-platform-springboot.sh $DOCKER_USERNAME $DOCKER_PASSWORD $VERSION
#
# VERSION 默认是 latest。tag-based deploy 时显式传 tag 名（v0.1.x-YYYYMMDD）。
# CI 同时 push :latest + :<tag> 两份镜像,回滚只要手动指定旧 tag 再跑一次本脚本。
#
# 与姊妹仓 saas-identity-platform-nextjs.sh 的差异:
#   - 数据库：PostgreSQL 远程, SPRING_DATASOURCE_URL 从 springboot.env 注入（无本地 ./data 卷）
#   - 容器内是 Java 17 (Spring Boot) 监听 :8080 → -p 127.0.0.1:8023:8080
#   - 密钥走 ./springboot.env (SPRING_DATASOURCE_URL/USERNAME/PASSWORD + CORS),由 setup-vps.sh 渲染时
#     不预生成（fail-fast 不便）,本脚本首启自举。
#   - JWT_SIGNING_KEY: v0.2.0 起 JwtIssuer(HS256) 构造 fail-fast 必填。缺失时本脚本
#     生成随机密钥 append 进 env-file（持久化, 不覆盖已有）。消费方走 whoami 内省不本地验签。
#
# 前置: deploy 用户需在 docker 组中(sudo usermod -aG docker deploy)。
#        springboot.env 必须由 setup-vps.sh 或本脚本首启生成(SPRING_DATASOURCE_URL 必填)。

set -eu

USERNAME="${1:-}"
PASSWORD="${2:-}"
VERSION="${3:-latest}"
IMAGE="${USERNAME}/saas-identity-platform-springboot:${VERSION}"
BASE="/home/deploy/saas-identity-platform-springboot"
CONTAINER_NAME="saas-identity-platform-springboot"
HOST_PORT=8023

# nginx domain（deploy 脚本渲染 nginx vhost 时用）
NGINX_DOMAIN="${NGINX_DOMAIN:-saas-springboot.xiangru.uk}"
NGINX_CERT_BASENAME="${NGINX_CERT_BASENAME:-xiangru-uk}"

if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ]; then
  echo "Usage: $0 <DOCKER_USERNAME> <DOCKER_PASSWORD> [VERSION]" >&2
  exit 2
fi

# springboot.env 自举保护: 缺失时, 如 $DATABASE_URL + $DATABASE_USER + $DATABASE_PASSWORD 在环境里,
# 自动生成（含 CORS 默认白名单 + SERVER_PORT）; 否则 fail fast。
# setup-vps.sh 仍是首推（VPS 一次性, 生成 nginx vhost + 目录）, 本分支仅给
# "先有 DATABASE_URL 临时上线"的场景。
if [ ! -f "$BASE/springboot.env" ]; then
  if [ -n "${DATABASE_URL:-}" ] && [ -n "${DATABASE_USER:-}" ] && [ -n "${DATABASE_PASSWORD:-}" ]; then
    echo "→ bootstrapping $BASE/springboot.env from env DATABASE_URL/USER/PASSWORD"
    umask 077
    {
      printf 'SPRING_DATASOURCE_URL=%s\n' "$DATABASE_URL"
      printf 'SPRING_DATASOURCE_USERNAME=%s\n' "$DATABASE_USER"
      printf 'SPRING_DATASOURCE_PASSWORD=%s\n' "$DATABASE_PASSWORD"
      printf 'SERVER_PORT=8080\n'
      # 默认 CORS 白名单：react SPA + saas-nextjs + 本仓域名。运维可在 setup-vps 之后手工追加 origin。
      printf 'SAAS_CORS_ALLOWED_ORIGINS=https://%s,https://saas-react.xiangru.uk,https://saas-nextjs.xiangru.uk\n' "$NGINX_DOMAIN"
    } > "$BASE/springboot.env"
    chown deploy:deploy "$BASE/springboot.env" 2>/dev/null || true
    chmod 600 "$BASE/springboot.env"
  else
    echo "ERROR: $BASE/springboot.env missing. Set DATABASE_URL/USER/PASSWORD env (e.g. DATABASE_URL=jdbc:postgresql://host/saas_prod DATABASE_USER=postgres DATABASE_PASSWORD=... sudo -E sh deploy/setup-vps.sh saas-springboot.example.com) or run setup-vps.sh first." >&2
    exit 1
  fi
fi
# 校验 springboot.env 里有 SPRING_DATASOURCE_URL（即使 env-file 已存在, 内容可能是上一次失败留下的）
if ! grep -q '^SPRING_DATASOURCE_URL=' "$BASE/springboot.env"; then
  echo "ERROR: $BASE/springboot.env has no SPRING_DATASOURCE_URL line" >&2
  exit 1
fi

# nginx vhost 自举（缺时创建, 不 reload —— reload 要 root）:
# 检测 /etc/nginx/sites-enabled/<NGINX_DOMAIN> 是否存在; 缺时从 nginx-vps.conf.example
# 模板渲染, 做 symlink。reload 需 sudo, 留给手工:
#   sudo nginx -t && sudo systemctl reload nginx
NGINX_SITES_AVAILABLE="/etc/nginx/sites-available"
NGINX_SITES_ENABLED="/etc/nginx/sites-enabled"
NGINX_VHOST_FILE="${NGINX_SITES_AVAILABLE}/${NGINX_DOMAIN}"
NGINX_VHOST_LINK="${NGINX_SITES_ENABLED}/${NGINX_DOMAIN}"
NGINX_TEMPLATE="${BASE}/nginx-vps.conf.example"

# 拉模板（deploy/ 目录随仓库 deploy 脚本一起, 但首次拉时可能不存在, 补一下）
if [ ! -f "${NGINX_TEMPLATE}" ]; then
  echo "→ fetching nginx-vps.conf.example template"
  curl -fsSL "https://raw.githubusercontent.com/zcqiand/saas-identity-platform-springboot/refs/heads/master/deploy/nginx-vps.conf.example" -o "${NGINX_TEMPLATE}"
fi

if [ -e "${NGINX_VHOST_LINK}" ] || [ -e "${NGINX_VHOST_FILE}" ]; then
  echo "→ nginx vhost ${NGINX_VHOST_FILE} already exists, skip bootstrap"
else
  echo "→ nginx vhost missing, bootstrapping ${NGINX_VHOST_FILE} (domain=${NGINX_DOMAIN} cert=${NGINX_CERT_BASENAME})"
  umask 022
  sed \
    -e "s/saas.YOUR_DOMAIN/${NGINX_DOMAIN}/g" \
    -e "s|/etc/nginx/ssl/your-cert.cert|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.cert|g" \
    -e "s|/etc/nginx/ssl/your-cert.key|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.key|g" \
    "${NGINX_TEMPLATE}" > "${NGINX_VHOST_FILE}"
  ln -sf "${NGINX_VHOST_FILE}" "${NGINX_VHOST_LINK}"
  echo "→ nginx vhost created. To enable: sudo nginx -t && sudo systemctl reload nginx"
fi

# 必要时补 SAAS_CORS_ALLOWED_ORIGINS（SecurityConfig 走 saas.cors.allowed-origins 通过 @Value 绑定）。
# 已有则不覆盖（bootstrap 那段 line 36-53 首启会写; 后续 deploy 重跑只会缺失时 append,
# 运维手工补的 prod origin 不会丢）。
if ! grep -q '^SAAS_CORS_ALLOWED_ORIGINS=' "$BASE/springboot.env"; then
  echo "→ append SAAS_CORS_ALLOWED_ORIGINS to existing $BASE/springboot.env"
  umask 077
  printf 'SAAS_CORS_ALLOWED_ORIGINS=https://%s,https://saas-react.xiangru.uk,https://saas-nextjs.xiangru.uk\n' "$NGINX_DOMAIN" >> "$BASE/springboot.env"
fi

# v0.2.0+: JwtIssuer(HS256 签 access token) 构造 fail-fast 要求 JWT_SIGNING_KEY(≥32B)。
# v0.1.x 时代自举的 env 没这行 → 容器起来即崩（v0.2.0 deploy 挂即此因）。
# 首次缺失时生成随机密钥持久化进 env-file（append-only, 已有则不覆盖 —— 重启/重部署
# 签名密钥保持稳定, 已签发的 token 不失效）。消费方（lab 后端）走 whoami 内省,
# 不本地验签 saas token, 无需与消费方共享密钥。
if ! grep -q '^JWT_SIGNING_KEY=' "$BASE/springboot.env"; then
  echo "→ append JWT_SIGNING_KEY (random, persisted) to existing $BASE/springboot.env"
  umask 077
  printf 'JWT_SIGNING_KEY=%s\n' "$(head -c 48 /dev/urandom | base64 | tr -d '\n')" >> "$BASE/springboot.env"
fi

echo "→ image: $IMAGE"
echo "→ docker login"
printf '%s' "$PASSWORD" | docker login -u "$USERNAME" --password-stdin

echo "→ docker pull"
docker pull "$IMAGE"

echo "→ docker stop & rm $CONTAINER_NAME"
docker stop "$CONTAINER_NAME" 2>/dev/null || true
docker rm "$CONTAINER_NAME" 2>/dev/null || true

echo "→ docker run"
docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  -p "127.0.0.1:${HOST_PORT}:8080" \
  --env-file "$BASE/springboot.env" \
  "$IMAGE"

echo "→ docker image prune"
docker image prune -f

echo "→ docker ps"
docker ps --filter name="$CONTAINER_NAME"

# 健康检查: 直接 wget /actuator/health 探 200, 不依赖 Docker HEALTHCHECK 语义。
# v0.1.8/v0.1.9 实测 ${STATUS} 不可靠 —— start-period=30s 窗口边界与
# 'unhealthy' 判定时机在 Docker daemon 不同版本上行为不一致, 86s
# 内就被标 unhealthy。改用直接 HTTP 探针 (Spring Boot 3.x /actuator/health
# 200+body UP 时是真 ready)。
#
# ⚠️ v0.1.10 的 bug: 写的是 127.0.0.1:8080 (容器内部端口), 但 deploy 脚本
# 跑在 HOST 上, 容器端口映射是 127.0.0.1:${HOST_PORT}:8080 (HOST_PORT=8023).
# host 上 wget 127.0.0.1:8080 = 连接 host 的 8080 (没服务), connect-refused, 120 次都失败。
# Docker HEALTHCHECK (Dockerfile:38) 走的是容器 network namespace 内的 127.0.0.1:8080,
# 所以日志里 02:52:26 Spring DispatcherServlet init 出现一次是 Docker HEALTHCHECK 命中,
# 而非 deploy 脚本 wget。
#
# wget --tries=1 --timeout=3 -q: 不重试, 3s timeout, 静默。
i=0
while [ $i -lt 120 ]; do
  if wget --tries=1 --timeout=3 -q "http://127.0.0.1:${HOST_PORT}/actuator/health" -O /dev/null 2>/dev/null; then
    echo "→ /actuator/health 200 (host 127.0.0.1:${HOST_PORT}) after ${i}s"
    break
  fi
  # 容器实际死亡 (OOM / start-cmd failure / 立刻 crash) 提前终止循环, 立刻报失败。
  if ! docker inspect --format='{{.State.Running}}' "$CONTAINER_NAME" 2>/dev/null | grep -q true; then
    echo "→ container not running, logs:"
    docker logs --tail 30 "$CONTAINER_NAME"
    exit 1
  fi
  i=$((i+1))
  sleep 1
done

if [ $i -ge 120 ]; then
  echo "→ /actuator/health 仍未 200（120s 上限）, logs:"
  docker logs --tail 30 "$CONTAINER_NAME"
  exit 1
fi

echo "→ deploy done at $(date -u)"
