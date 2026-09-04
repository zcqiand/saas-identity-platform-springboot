# CHANGELOG — saas-identity-platform-springboot

格式参照 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [0.2.29] — 2026-09-04

- fix(repository): `RoleMenuGrantRepository.findMenuIdsByRoleIds` native SQL
  `ANY(:roleIds)` 多角色必炸 —— Hibernate 把 Collection&lt;UUID&gt; 展开成多占位符
  （`ANY(?,?)`）PG 语法错；单角色恰好合法所以冒烟测不出。真库测试首跑抓出，
  改 `IN (:roleIds)`。prod 主路径（MeService）此前已绕行 JdbcTemplate，本修
  消灭 jdbc=null 回退分支的隐患。
- test(repository): 删 @Disabled 三个月的 `UserRepositoryDataJpaTest`
  （"Phase 5 Testcontainers"一直未兑现），新增真库版 `RepositoryPgTest`
  （saas_test 硬依赖，连不上即败不 skip）：uuid[] unnest 平铺、IN + unnest +
  DISTINCT、user 分页/status 过滤/唯一约束、真 FK 链（tenant→role→grant）。
  H2 镜像不了的 uuid[]/jsonb/native enum 全走真方言。
- ci.yml：L4 `-DexcludedGroups=pg`（CI=编译+mock / gate=真库分层，全家族统一）。

## [0.2.16] — 2026-08-28

- fix(service): MeService.getMyMenus `unnest()` 查询改走 JdbcTemplate 直连，绕开
  Spring Data `@Query nativeQuery=true` + `List<UUID>` 在 prod PG driver 上的
  JDBC 类型映射失败（v0.2.12/v0.2.13/v0.2.14/v0.2.15 共 4 次 deploy 全部 500，
  mock 单测全绿）。复制 OauthService 已验证的 JdbcTemplate 模式：
    `queryForList(UUID.class, ...) ` 查 role_ids
    `query(... createArrayOf("uuid", ...))` 查 menu_ids
  MeService 构造 +1 JdbcTemplate 形参，单测 jdbc=null 时回退 Spring Data
  仓库（保持 mock 注入兼容）。MeServiceGetMyMenusTest/MeServiceTest 都补参数。
- 教训：mock 单测全绿 ≠ prod 通，**prod PG driver 对 `unnest(uuid[])` 经 Spring Data
  映射 List<UUID> 的兼容性与 JDBC 直连不同**。新加 native query 必须有 prod 烟测，
  不能依赖单测。HikariCP 也无相关配置可救。

## [0.2.15] — 2026-08-28

- chore(repo): findRoleIdsByUserId 简化去掉 status='active' 过滤（v0.2.13 显式
  cast 仍 500 → 应用层由 role_menu_grants 二次过滤隐式排除 removed 成员的
  role_id）。**该版本并未解决 500，留作调查轨迹**。

## [0.2.13] — 2026-08-28

- fix(repo): findRoleIdsByUserId 'active'::membership_status 显式 cast — **无效**，
  仍 500。

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
