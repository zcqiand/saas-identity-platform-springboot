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
#   - JWT_SIGNING_KEY **不**写入: SecurityConfig.DevJwtDecoder 仅 dev, prod 准备 = 删 DevJwtDecoder +
#     设 SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI env。本脚本只覆盖环境变量,
#     不动 Java 代码 —— prod 路径由独立 PR 处理。
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

# 健康检查: 容器 healthcheck 应 healthy（springboot Dockerfile: wget /actuator/health,
# Spring Boot 启动 + bean wiring 给 30s start-period 容忍）。
# 循环 120s 不是任意数:
#   start-period=30s (Docker 内部), interval=30s, retries=3
#   worst-case 健康总耗时 = 30 + 30*3 = 120s
# CI v0.1.8 实测: 30 次循环太短, Spring Boot 15s 启动后仍卡 'starting',
#   probe 还没开始算 fail, deploy 脚本先 exit 1。
i=0
while [ $i -lt 120 ]; do
  STATUS=$(docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo "starting")
  if [ "$STATUS" = "healthy" ]; then
    echo "→ container healthy after ${i}s"
    break
  fi
  if [ "$STATUS" = "unhealthy" ]; then
    echo "→ container unhealthy, logs:"
    docker logs --tail 30 "$CONTAINER_NAME"
    exit 1
  fi
  i=$((i+1))
  sleep 1
done

if [ $i -ge 120 ]; then
  echo "→ container failed to become healthy in 120s, logs:"
  docker logs --tail 30 "$CONTAINER_NAME"
  exit 1
fi

echo "→ deploy done at $(date -u)"
