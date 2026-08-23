# =============================================================================
# saas-identity-platform-springboot — 生产镜像
#
#   builder  → 安装 deps + mvn package（生成 Spring Boot fat jar）
#   runtime  → eclipse-temurin:17-jre-slim + app.jar，监听 SERVER_PORT=8080
#
# 数据库：PostgreSQL（远程）。容器内不持有 DB 文件 —— 运行期必须通过
#         SPRING_DATASOURCE_URL 环境变量注入连接串（由 VPS springboot.env 注入）。
#
# 端口：容器内 Spring Boot 监听 :8080；VPS nginx 反代到 publish 出的端口（默认 8023）。
#
# 镜像族系（与 nextjs 同侧）：
#   - nextjs builder+runtime 用 node:24-slim（Debian slim）
#   - react builder 用 node:20-alpine + runtime 用 nginx:alpine
#   - springboot builder 用 maven:3.9-eclipse-temurin-17, runtime 用 eclipse-temurin:17-jre-slim（Debian slim）
# runtime 选 slim 而非 alpine 的原因：避免 musl libc + 偶尔出现的 native deps 冲突
# （hypersistence-utils 是纯 Java，但 postgresql/nimbus-jose-jwt 都有过的 surprise）。
# 与 nextjs 同走 slim：suite 镜像族系一致。
# =============================================================================


# ---------- Stage 1: builder ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 缓存友好的层：先只复制 pom.xml 跑 dependency:go-offline，
# 再 copy src。多数 commit 只动 src, deps 缓存命中。
COPY pom.xml ./
RUN mvn -B -e -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -e -ntp -DskipTests package \
 && cp target/platform-0.1.0.jar /app/app.jar


# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-slim AS runtime
WORKDIR /app

# slim 缺 wget —— Docker HEALTHCHECK 需要它探 /actuator/health
RUN apt-get update \
 && apt-get install -y --no-install-recommends wget ca-certificates \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/app.jar /app/app.jar

# JVM 在容器内堆上限参考 cgroup 内存限额（默认 75%）
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENV SERVER_PORT=8080
ENV TZ=UTC

EXPOSE 8080

# Bean wiring tolerance: Spring Boot 冷启动 5-15s @ 小 VPS。
# probe 走 /actuator/health（spring-boot-starter-actuator + management.endpoint.health.probes.enabled:true
# 在 application.yml 里开）。如果 servlet 链还没就绪, /actuator/health 会返回 503,
# Docker HEALTHCHECK exit 1, container restart-loop —— 这是想要的 fail-loud 行为。
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
