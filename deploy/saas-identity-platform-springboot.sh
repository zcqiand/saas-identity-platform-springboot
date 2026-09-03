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
#   - 数据库：PostgreSQL 远程, DATABASE_URL 从 springboot.env 注入（无本地 ./data 卷）
#   - 容器内 Java 21 (Spring Boot 3.4) 监听 :5105；host=container=5105（ADR-0018 单层
#     port 方案，docker run -p 127.0.0.1:5105:5105；saas 家族 X05 段）
#   - 密钥走 ./springboot.env (DATABASE_URL/USERNAME/PASSWORD + CORS),由 setup-vps.sh 渲染时
#     不预生成（fail-fast 不便）,本脚本首启自举。
#   - JWT_SIGNING_KEY: v0.2.0 起 JwtIssuer(HS256) 构造 fail-fast 必填。缺失时本脚本
#     生成随机密钥 append 进 env-file（持久化, 不覆盖已有）。消费方走 whoami 内省不本地验签。
#
# 前置: deploy 用户需在 docker 组中(sudo usermod -aG docker deploy)。
#        springboot.env 必须由 setup-vps.sh 或本脚本首启生成(DATABASE_URL 必填)。

set -eu

USERNAME="${1:-}"
PASSWORD="${2:-}"
VERSION="${3:-latest}"
IMAGE="${USERNAME}/saas-identity-platform-springboot:${VERSION}"
BASE="/home/deploy/saas-identity-platform-springboot"
CONTAINER_NAME="saas-identity-platform-springboot"

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
    echo "→ bootstrapping $BASE/springboot.env from env DATABASE_URL/USER/PASSWORD (key 集合 = .env.production)"
    umask 077
    {
      printf 'DATABASE_URL=%s\n' "$DATABASE_URL"
      printf 'DATABASE_USER=%s\n' "$DATABASE_USER"
      printf 'DATABASE_PASSWORD=%s\n' "$DATABASE_PASSWORD"
      printf 'DATABASE_NAME=saas_prod\n'
      printf 'SERVER_PORT=5105\n'
      # 默认 CORS 白名单：react SPA + saas-nextjs + 本仓域名。运维可在 setup-vps 之后手工追加 origin。
      printf 'SAAS_CORS_ALLOWED_ORIGINS=https://%s,https://saas-react.xiangru.uk,https://saas-nextjs.xiangru.uk\n' "$NGINX_DOMAIN"
      # JWT 三件套显式写(JwtIssuer @Value 默认值兜底是反模式,禁;值=契约文件值)
      printf 'JWT_ISSUER=saas-identity-platform\n'
      printf 'JWT_AUDIENCE=saas-identity-platform-clients\n'
      printf 'JWT_TTL_SECONDS=3600\n'
      # JWT_SIGNING_KEY 首启随机生成,append-only 持久化(见下方 v0.2.0 段注释)
      printf 'JWT_SIGNING_KEY=%s\n' "$(head -c 48 /dev/urandom | base64 | tr -d '\n')"
    } > "$BASE/springboot.env"
    chown deploy:deploy "$BASE/springboot.env" 2>/dev/null || true
    chmod 600 "$BASE/springboot.env"
  else
    echo "ERROR: $BASE/springboot.env missing. Set DATABASE_URL/USER/PASSWORD env (e.g. DATABASE_URL=jdbc:postgresql://host/saas_prod DATABASE_USER=postgres DATABASE_PASSWORD=... sudo -E sh deploy/setup-vps.sh saas-springboot.example.com) or run setup-vps.sh first." >&2
    exit 1
  fi
fi
# 校验 springboot.env 里有 DATABASE_URL（即使 env-file 已存在, 内容可能是上一次失败留下的）
if ! grep -q '^DATABASE_URL=' "$BASE/springboot.env"; then
  echo "ERROR: $BASE/springboot.env has no DATABASE_URL line" >&2
  exit 1
fi

# nginx vhost 重渲染（每次 deploy 都跑,ADR-0018:容器端口变了 vhost 必须跟）:
# 模板从 master 拉,渲染后写入 sites-available,symlink sites-enabled,再 sudo nginx -t + reload。
# diff 检测:内容未变跳过 reload (nginx -t 也省)。
NGINX_SITES_AVAILABLE="/etc/nginx/sites-available"
NGINX_SITES_ENABLED="/etc/nginx/sites-enabled"
NGINX_VHOST_FILE="${NGINX_SITES_AVAILABLE}/${NGINX_DOMAIN}"
NGINX_VHOST_LINK="${NGINX_SITES_ENABLED}/${NGINX_DOMAIN}"
NGINX_TEMPLATE="${BASE}/nginx-vps.conf.example"

# 拉模板:每次都从 master 拉最新 —— VPS 本地会留 7 月老模板(801x 端口时代),
# fetch-if-missing 让它永不更新 → 渲染出 proxy_pass 8010 全家族 502
# (2026-09-03 事故根因之二;set -eu 下 curl 失败即 fail-fast)
echo "→ fetching nginx-vps.conf.example template (always fresh from master)"
curl -fsSL "https://raw.githubusercontent.com/zcqiand/saas-identity-platform-springboot/refs/heads/master/deploy/nginx-vps.conf.example" -o "${NGINX_TEMPLATE}"

# 渲染到临时文件 —— sed 同时覆盖 3 种 placeholder:
#   Style A (lab-vue/react):      <domain>
#   Style B/C (nextjs/sp/aspc):   lab.YOUR_DOMAIN / saas.YOUR_DOMAIN
#   cert 路径: your-cert.{crt,cert} / <domain>.crt → 统一到 ${NGINX_CERT_BASENAME}.cert
TMP_VHOST="$(mktemp -t vpstpl.XXXXXX)"
# cert 归一化规则必须排在 <domain>/YOUR_DOMAIN 通配之前:sed -e 按顺序执行,
# 先替换 <domain> 会把 cert 路径里的占位符一并吃掉,后面的 cert 规则全部失配
# (2026-09-03 VPS nginx -t "cannot load certificate <域名>.crt" 事故根因)
sed \
  -e "s|/etc/nginx/ssl/<domain>\.crt|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.cert|g" \
  -e "s|/etc/nginx/ssl/<domain>\.cert|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.cert|g" \
  -e "s|/etc/nginx/ssl/<domain>\.key|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.key|g" \
  -e "s|/etc/nginx/ssl/your-cert\.crt|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.cert|g" \
  -e "s|/etc/nginx/ssl/your-cert\.cert|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.cert|g" \
  -e "s|/etc/nginx/ssl/your-cert\.key|/etc/nginx/ssl/${NGINX_CERT_BASENAME}.key|g" \
  -e "s|<domain>|${NGINX_DOMAIN}|g" \
  -e "s|lab\.YOUR_DOMAIN|${NGINX_DOMAIN}|g" \
  -e "s|saas\.YOUR_DOMAIN|${NGINX_DOMAIN}|g" \
  "${NGINX_TEMPLATE}" > "${TMP_VHOST}"

# diff 检测:已有 vhost 且内容相同就 skip,不同才重写 + reload
if [ -e "${NGINX_VHOST_FILE}" ] && diff -q "${TMP_VHOST}" "${NGINX_VHOST_FILE}" >/dev/null 2>&1; then
  echo "→ nginx vhost ${NGINX_VHOST_FILE} unchanged, skip"
  rm -f "${TMP_VHOST}"
else
  echo "→ rendering nginx vhost ${NGINX_VHOST_FILE} (domain=${NGINX_DOMAIN} cert=${NGINX_CERT_BASENAME})"
  # 写入 sites-available (deploy 用户可能没写权限,需要 sudoers 配 nginx 白名单)
  if [ -w "${NGINX_SITES_AVAILABLE}" ]; then
    cp "${TMP_VHOST}" "${NGINX_VHOST_FILE}"
  else
    sudo cp "${TMP_VHOST}" "${NGINX_VHOST_FILE}" \
      || { echo "ERROR: sudo cp ${NGINX_VHOST_FILE} failed"; rm -f "${TMP_VHOST}"; exit 1; }
  fi
  # symlink sites-enabled
  if [ -w "${NGINX_SITES_ENABLED}" ]; then
    ln -sf "${NGINX_VHOST_FILE}" "${NGINX_VHOST_LINK}"
  else
    sudo ln -sf "${NGINX_VHOST_FILE}" "${NGINX_VHOST_LINK}" \
      || { echo "ERROR: sudo ln ${NGINX_VHOST_LINK} failed"; rm -f "${TMP_VHOST}"; exit 1; }
  fi
  rm -f "${TMP_VHOST}"
  # nginx config test + reload (CI 自动完成,不再依赖手工)
  echo "→ nginx -t"
  sudo nginx -t
  echo "→ systemctl reload nginx"
  sudo systemctl reload nginx
  echo "✓ nginx reloaded"
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

# 2026-08-28 key 对齐: 老 env-file 逐 key append-if-missing 到 .env.production 全集
# (key 集合契约由 suite L0.5 check_deploy_parity 锁死;JwtIssuer @Value 默认值兜底
# 是反模式 —— 显式写值,漂移在 deploy 期暴露而不是运行期静默吃默认)。
if [ -f "$BASE/springboot.env" ]; then
  append_if_missing() {
    key="$1"; val="$2"
    if ! grep -q "^${key}=" "$BASE/springboot.env"; then
      echo "→ append ${key} to existing $BASE/springboot.env"
      umask 077
      printf '%s=%s\n' "$key" "$val" >> "$BASE/springboot.env"
    fi
  }
  append_if_missing DATABASE_NAME 'saas_prod'
  append_if_missing SERVER_PORT '5105'
  append_if_missing JWT_ISSUER 'saas-identity-platform'
  append_if_missing JWT_AUDIENCE 'saas-identity-platform-clients'
  append_if_missing JWT_TTL_SECONDS '3600'

  # 一次性 stale 值 reconcile —— append_if_missing 只补 key, 不覆盖值。
  # v0.2.0 之前老 env-file 写 SERVER_PORT=8080,与 Dockerfile ENV SERVER_PORT=5105 +
  # docker run -p ...:5105 不一致,导致 JVM 监听 8080、容器 publish 5105 无 listener,
  # healthcheck 永远 connection-refused, deploy 120s 超时。检测到旧值就 sed 覆盖。
  # 同类迁移按此模式续写(migrate_if_stale KEY OLD NEW)。
  if grep -q '^SERVER_PORT=8080$' "$BASE/springboot.env"; then
    sed -i 's/^SERVER_PORT=8080$/SERVER_PORT=5105/' "$BASE/springboot.env"
    echo "→ reconcile SERVER_PORT: 8080 → 5105 (V018 port migration)"
  fi
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
  -p "127.0.0.1:5105:5105" \
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
# ⚠️ v0.1.10 的 bug: 写的是当时的容器内部端口, 但 deploy 脚本
# 跑在 HOST 上, 容器端口映射是 127.0.0.1:5105:5105 (host=container=5105).
# host 上 wget 容器内部端口 = 连接 host 的同名端口 (没服务), connect-refused, 120 次都失败。
# Docker HEALTHCHECK (Dockerfile:38) 走的是容器 network namespace 内的 127.0.0.1:5105,
# 所以日志里 Spring DispatcherServlet init 出现一次是 Docker HEALTHCHECK 命中,
# 而非 deploy 脚本 wget。
# 注：v0.2.0 起容器端口从 8080 迁到 5105（端口分段 §6），deploy 脚本里 -p 同步，
# 但老 springboot.env 若写 SERVER_PORT=8080 仍会让 JVM 监听 8080 → publish 5105 无 listener。
# 见上方 reconcile 段，append_if_missing 不覆盖值需要 sed 兜底。
#
# wget --tries=1 --timeout=3 -q: 不重试, 3s timeout, 静默。
i=0
while [ $i -lt 120 ]; do
  if wget --tries=1 --timeout=3 -q "http://127.0.0.1:5105/actuator/health" -O /dev/null 2>/dev/null; then
    echo "→ /actuator/health 200 (host 127.0.0.1:5105) after ${i}s"
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
