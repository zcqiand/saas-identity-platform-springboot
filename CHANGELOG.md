# CHANGELOG — saas-identity-platform-springboot

格式参照 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [0.2.9] — 2026-08-28

- fix(auth): AuthService（login/refresh）与 MeService（switchTenant）迁移到 JwtIssuer HS256
  真签。此前二者仍手搓 `alg=none + .dev-placeholder` 假 token，而 SecurityConfig Phase 2 起
  只认真签 → 线上登录后所有业务接口 401。对称 saas-aspnetcore AuthController/MeController（v0.2.0）。
- refresh 兼容新旧两种 refresh token 格式（`refresh-<uuid>-<epoch>` / `saas-rt-<uuid>-<ts>-<rand>`）。
- 测试断言 login/switchTenant 返回三段 HS256 且非 dev-placeholder；本地 E2E 验证
  login → Bearer token → `/api/v1/admin/tenants` 200。

## [0.2.8] — 2026-08-28

- fix(deps): swagger-annotations-jakarta 2.2.22 → 2.2.30，对齐 springdoc 2.8.9 传递的
  swagger-core。nearest-wins 让旧 annotations 上 classpath，`/v3/api-docs` 运行时
  `NoClassDefFoundError: Schema$SchemaResolution`，且被 ExceptionTranslationFilter 转成
  401 掩盖（swagger-config 200、精确路径 401）。
- test: SpringdocCompatibilityTest 扩展层 2 守卫——断言 annotations ≥ swagger-models/core。

## [0.2.7] — 2026-08-28

- fix(deps): springdoc-openapi 2.6.0 → 2.8.9。spring-web 6.2（Boot 3.4 自带）删除
  `ControllerAdviceBean.<init>(Object)`，springdoc < 2.7.0 调它 → 线上 `/v3/api-docs`
  运行时 `NoSuchMethodError`（springdoc #2687）。编译期与单测无感知。
- test: 新增 `SpringdocCompatibilityTest` 类路径级二进制兼容守卫（升级前实测红）。

## [0.2.4] — 2026-08-27

- 初始化台账：Java 21 + Spring Boot 3.4 后端。历史变更见 git log 与 `.state/session.json`。
