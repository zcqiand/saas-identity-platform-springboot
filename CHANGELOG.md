# CHANGELOG — saas-identity-platform-springboot

格式参照 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [0.2.10] — 2026-08-28

- fix(entity): ApiKeyEntity.scopes `@Type(StringArrayType)` → `ListArrayType`。StringArrayType
  只支持 `String[]` 属性，用在 `List<String>` 上读回时 hypersistence `ArrayUtil.unwrapArray` →
  `Array.newInstance(null)` NPE → 线上 GET/POST `/api/v1/tenants/{id}/api-keys` 500
  （users/roles 同链路正常，仅 api-keys 中招）。AppEntity 时代（b67419b）曾把同类 NPE
  误诊为「hypersistence 数组 bug」用 `@Transient` 绕过，本案证伪：是 UserType 与属性类型不匹配。
- test: 新增 `ArrayUserTypeMappingGuardTest` 映射守卫——扫描全部 entity，断言数组
  UserType（StringArrayType/UUIDArrayType）不挂在 List 属性、ListArrayType 不挂数组属性。
  旧代码实测红。堵住「单测全 mock、H2 镜像不了 text[]」的映射层盲区。

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
