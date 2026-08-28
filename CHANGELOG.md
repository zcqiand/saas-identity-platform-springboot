# CHANGELOG — saas-identity-platform-springboot

格式参照 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [0.2.12] — 2026-08-28

- fix(service): MeService.getMyMenus 真实现（M09.F03.I02/I03/I04）。
  线上 `/api/v1/me/menus` 此前返 `Map.of()`（空），前端拿到 200 + 空树 → 菜单不渲染。
  真链路：userId → membership.roleIds (DISTINCT unnest) → role_menu_grants.menuIds (DISTINCT unnest) →
  menus 表 + 父链补全 → 按 app.code 分组输出 `Map<appCode, List<EffectiveMenuNode>>`。
  role_ids / menu_ids 都是 `@Transient` 数组列（hypersistence StringArrayType 误诊未解），
  走 repository `@Query(nativeQuery=true)` 直接 `unnest()`，绕开 entity 映射。
  MeController.meGetMyMenus 同步从 stub 改为真调用。
- test: 新增 `MeServiceGetMyMenusTest` 3 个用例 — 返回树按 app.code 分组、无角色返空 map、
  group 节点不在 grant 中也保留作容器（父链补全，msw mock 语义镜像）。同时给 MeServiceTest
  补齐 5 参构造调用（grantRepository/menuRepository/appRepository mocks）。
- chore(gen-shared): step 2/2 拷贝 codegen 前同步 `rm -rf src/main/java/saas/identity/shared/api`，
  b67419b 之前 codegen 产物放 saas.identity.shared.api 现已迁到 platform/api，
  残留会让 javac 报 duplicate class。

## [0.2.11] — 2026-08-28

- fix(service): 删除 create/rotate 路径的手动 `setId(UUID.randomUUID())`（api-keys /
  tenants / roles / apps / menus 共 5 处）。entity 已有 `@GeneratedValue(UUID)`，
  预置 id 会被 Spring Data 判为 detached → `merge()` → UPDATE 0 行 →
  `StaleObjectStateException` → 线上 POST 500。本地实测 tenant/role create 同炸，
  属系统性问题。
- fix(datasource): Hikari `stringtype=unspecified` —— `AttributeConverter` 输出的
  String 以 unknown 类型绑定，PG 按目标列解析成原生 enum。此前 varchar →
  `tenant_status`/`api_key_status` 列 42804（id 修好后露出的第二层写路径 500）。
  注意 `NAMED_ENUM` + converter 组合启动即崩（`PostgreSQLEnumJdbcType` NPE），不可用。
- test: `TenantApiKeyServiceIsNewTest` isNew 语义守卫（旧代码实测红）——create/rotate
  传入 repository 的 entity id 必须为 null；entity 主键必须保持 `@GeneratedValue`。
- 本地 E2E（连 saas_dev）：POST api-key / tenant / role 全 200；api-key 完整
  生命周期 create→list→rotate→revoke 全 200（rotate 撤旧发新语义正确）。

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
