# PLAN — SaaS 多租户多应用身份平台 · Spring Boot 后端

> 待办与迭代方向。详细上下文见 `.state/session.json` 与 `docs/adr/`。

## 待办

### [BUG] DateTime.MinValue（`-292275055-05-16T23:00:00Z`）出现在响应 createdAt 字段

- **状态**: 待定位
- **首次发现**: 2026-09-01 live mode 全量 contract-test run
- **关联 ADR**: [docs/adr/0015-amend-timestamps.md](../../../docs/adr/0015-amend-timestamps.md) §「已知非契约问题」
- **关联合约测试**: `M96.F02.I10 GET /tenants/{t}/users 四方比对`

### 症状（活证据）

live mode 全量跑 I10，springboot 在 `users[].createdAt` 字段返回 `-292275055-05-16T23:00:00Z`，
即 `DateTime.MinValue` 的 ISO 8601 UTC 字符串化。同窗口的 msw / aspnetcore / nextjs 都返回有效时间戳
（msw 是 fixture 静态值 `2026-01-20T08:00:00.000Z`，其余两家用 `Date.now()`）。

### 已知事实

- 13 个 entity（含 `UserEntity.java`）都挂了 `@PrePersist` + `@PreUpdate` 回调，
  回调内显式 `if (createdAt == null) createdAt = OffsetDateTime.now()`。**所以 `@PrePersist` 路径本身不应兜底为 MinValue。**
- 该值不在 entity 层出现 —— entity 字段类型是 `OffsetDateTime`，默认 `null`，
  走 Jackson 序列化应是 `null` 而不是 `-292275055-05-16T23:00:00Z`。

### 两条候选根因方向

1. **DTO mapper 兜底为 MinValue**：检查 `saas/identity/shared/dto/User.java`（及同目录其他 DTO），
   看是否有显式 `return createdAt == null ? OffsetDateTime.MIN : createdAt` 之类的兜底，
   或 BeanUtils / MapStruct 配置里设了 `nullValueMappingStrategy=RETURN_DEFAULT`。
2. **绕过 entity 的写入路径**：用户创建可能走 JdbcTemplate `INSERT ... RETURNING *` 或
   `nativeQuery=true @Query`，这种路径不会触发 `@PrePersist`。先 grep
   `JdbcTemplate` / `@Query(nativeQuery=true)` / `entityManager.createNativeQuery` 在
   `*Service.java` 的出现位置，看 user create 走的是哪条。

### 调查步骤（按代价从小到大）

- [ ] 1. 直 curl `POST /tenants/{t}/users`（任意 target），再 `GET` 列表，看 springboot
      返回 createdAt 是否为 MinValue —— 确认 bug 是否每次复现
- [ ] 2. `git log -p saas/identity/shared/dto/User.java | head -100` 看 DTO 是否有
      「null → MIN」的提交历史
- [ ] 3. 搜 `MinValue` / `OffsetDateTime.MIN` / `Instant.MIN` 全仓出现位置
- [ ] 4. 定位 user create 的 service 层代码，看持久化走 `userRepository.save(entity)` 还是
      自定义 SQL
- [ ] 5. 如果是 DTO 兜底：改兜底为 null + 加 Jackson `WRITE_DATES_AS_TIMESTAMPS=false`
      + 加 NON_NULL；如果是 native query 绕过：迁回 entity `save()` 或在 SQL 里 `now()`

### 修复后回归

- [ ] contract-test `M96.F02.I10` 全等（msw 静态值仍存在，但 springboot 不再 MinValue）
- [ ] mvn test 全绿
- [ ] 本机 prod-build smoke（启动 → 创建 user → GET 列表 → 看到 ISO 8601 毫秒 UTC）

### 推荐默认值（user 拍板 2026-09-01）

entity 字段如果需要兜底默认值，**不要用 `OffsetDateTime.MIN` / `Instant.MIN` / `null`**，用 **Unix 纪元**:

```java
// Java Instant / LocalDateTime
public static final Instant CREATED_AT_DEFAULT = Instant.EPOCH;  // = 1970-01-01T00:00:00Z
// 或:
// public static final LocalDateTime CREATED_AT_DEFAULT = LocalDateTime.of(1970, 1, 1, 0, 0);
// Instant.EPOCH 在 Jackson `JavaTimeModule` 默认输出 "1970-01-01T00:00:00Z"，contract-test [1970, 2100] 合法
```

**注意**: `LocalDateTime.of(1970, 1, 1, 0, 0)` 经 Jackson 序列化为 `"1970-01-01T00:00"`（无 Z 后缀，因 LocalDateTime 无时区），**`assertTimestampShape` 的 ISO regex 要求 `Z` 或 `+00:00`**，所以必须用 `Instant.EPOCH` 或在 mapper 层加 `.atZone(ZoneOffset.UTC).toInstant()`。

### 风险

合约测试 ADR-0015-amend 通过后，I10 会改用「格式断言」比较 4 后端时间戳格式
（`…Z` 毫秒）。**新断言下 `-292275055-05-16T23:00:00Z` 仍是合法格式（毫秒 + Z），
仍然不会红** —— 所以**这条 bug 在 ADR 通过后会被合约侧盖住，必须在此工单锁死修复日期**，
否则 Hibernate 行为变化时再暴露没人会发现。

### 本会话根因实证（2026-09-01 SQL 日志 + V016 seed 调研后）

**SQL 日志证据**（`.runtime-logs/springboot.log` 3693 条 binding trace,1397 条 Hibernate SQL）：

12+ 次 `INSERT INTO users` 的 createdAt bind value：
- 真实时间戳 `2026-09-01T19:58:08.183257+08:00` — `@CreationTimestamp` 路径生效
- 真实时间戳 `2026-09-01T19:58:23.733429+08:00` — mapper 显式 `OffsetDateTime.now()`
- `1970-01-01T00:00:00Z` — 本会话 `@PrePersist` fallback 改 `Instant.EPOCH.atOffset(UTC)` 生效
- **`-infinity` bind: 0 次**

**结论**: 本会话 live 跑中,**没有任何新 INSERT 把 -infinity 写进 PG**。`-292275055-05-16T23:00:00Z` 是**历次跑残留**(ASP.NET EF 6/7 时代的 PG `timestamptz '-infinity'` 兜底 + JDBC 读回映射成 `OffsetDateTime.MIN`)。

`binding parameter (1:TIMESTAMP_UTC) <- [null]` 出现 5+ 次 — 全是 `update api_keys set ... expires_at=?, last_used_at=?, revoked_at=?` 的 nullable 字段(PATCH 时业务上允许 null),**不是 `created_at`**。

**根因 = 历史残留行 + V016 seed 部分字段被旧版 EF 写过 -infinity**,**不是当前 INSERT 路径**。

### 推荐修法（按代价从小到大）

- [ ] **第一步（必做，下个会话跑）**：一次性 PG UPDATE 把现存 `-infinity` 行改成 `'1970-01-01 00:00:00+00'`：
  ```sql
  UPDATE users SET created_at='1970-01-01T00:00:00+00' WHERE created_at='-infinity'::timestamptz;
  UPDATE api_keys SET created_at='1970-01-01T00:00:00+00' WHERE created_at='-infinity'::timestamptz;
  -- 重复 SELECT 验证 count(*) = 0
  ```
  跨环境安全（本机 saas_dev,共享 PG,3 后端共用）。
- [ ] **第二步**：改 [saas-identity-platform-shared/sql/migrations/V016__seed_family_fixtures.sql](../saas-identity-platform-shared/sql/migrations/V016__seed_family_fixtures.sql) seed 显式设 `'1970-01-01T00:00:00+00'`,防止 V016 被任何路径覆盖时引入 `-infinity`。
- [ ] **第三步**：本会话已落（contract-test cleanup-pg.ts + 8 entity `@PrePersist` Instant.EPOCH fallback）防新写 `-infinity`。
- [ ] **不再需要 A/B/C 路径** — B 路径（OffsetDateTime → Instant 技术选型 ADR-0016）取消,根因不是字段类型;A 路径（DB DEFAULT 接管）+ C 路径（SELECT 包装）不需要,本会话 INSERT 路径已修。

### 本会话新发现（2026-09-01 live 实证后追加）

- 三轮修法都未根除：
  1. `UserEntity`/`RoleEntity`/`ApiKeyEntity` 改 `@PrePersist` → `@CreationTimestamp`/`@UpdateTimestamp`
  2. `TenantApiKeyService` / `TenantUsersService` 显式 `e.setCreatedAt(OffsetDateTime.now())`
  3. `RoleMapper` / `TenantUserMapper` 同样 setter
- **直接调用时 createdAt 是有效值；只有 contract-test 4 后端并发触发时（~50%）才出现 `-infinity`**。
- 触发概率说明问题在「共享连接批 flush」或「并发写入下 `@CreationTimestamp` 触发条件」层面，**不是单点 setter 缺失**。
- 实证调研发现：所有 13 个 entity 时间列类型为 `OffsetDateTime`，DB 列是 `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`，正常路径不可能落 `-infinity`。**真凶不在 entity / DTO 层**。
- 建议下一步**先开 SQL 日志**（`application.yml` 加 `logging.level.org.hibernate.SQL: DEBUG` + `spring.jpa.show-sql: true`），重跑 contract-test I10，**对比拿到 `-infinity` 时的真 `INSERT INTO users(...) VALUES (...)`**，**再**动手改 Hibernate / entity 类型（候选：全部 entity `OffsetDateTime` → `Instant`，mapper 加 `Instant → OffsetDateTime` 转换）。

### 跨语言 MinValue 对照（2026-09-01 user 拍板，下个会话调试参考）

| Language / Framework | Minimum Date Value | Standard Code Representation | 本仓抓到的具体字符串 | 备注 |
|---|---|---|---|---|
| C# (.NET) | `0001-01-01 00:00:00` | `DateTime.MinValue` | `0001-01-01T00:00:00+00:00` | **本仓是 `DateTimeOffset.MinValue`（值类型，非可空）**，STJ round-trip 加 `T` + `+00:00`（不是 `Z`）。详见 [aspnetcore PLAN.md `+00:00` 注释](../saas-identity-platform-aspnetcore/PLAN.md) |
| Java (`java.time`) | `-999999999-01-01T00:00:00` | `LocalDateTime.MIN` | `-292275055-05-16T23:00:00Z` | **本仓抓到不是 `LocalDateTime.MIN` 标准 toString**。年份 `-292275055` 不是 `-999999999` —— 真凶是 Hibernate 6 + PG `timestamptz '-infinity'` 经 `OffsetDateTime` 映射的内部 sentinel 值（Jackson `JavaTimeModule` 默认输出形态）。`LocalDateTime.MIN.toString()` 实际是 `-999999999-01-01T00:00`（无 Z、无毫秒、无偏移）。 |
| Next.js (JS/TS) | `-271821-04-20T00:00:00Z` | `new Date(-8640000000000000)` | 未实测（本会话 nextjs 没起 4 后端） | contract-test `M96.F02.I15` / `I71` 抓到的 nextjs 时间戳是 `undefined`（推测 Drizzle `defaultNow()` 没填上），session.json 已记。 |

**调试提示**：contract-test `assertTimestampShape` 用 `年份 [2000,2100]` 断言抓所有 MinValue —— 上述 3 个语言 MinValue 都不在区间，所以都能抓到。但**字符串形态与语言/框架/序列化器组合强相关**，下次会话调试 Hibernate `-infinity` 真凶时，应**先看真 SQL + 真 Java 对象类型**，不要被 `-292275055` 这个怪数字带偏。

### 推荐修法（按「先看 SQL → 再改 Hibernate → 最后改 DTO」三步）

- [ ] **第一步（必须先做）**：`src/main/resources/application.yml` 加 SQL 日志:
      ```yaml
      logging:
        level:
          org.hibernate.SQL: DEBUG
          org.hibernate.orm.jdbc.bind: TRACE
      spring.jpa.show-sql: true
      ```
      重跑 contract-test I10 拿到真 INSERT SQL。
- [ ] **第二步（看 SQL 后）**：根据真 SQL 决定 A / B / C 路径：
  - **A. SQL 里 createdAt 是 `NULL` 字面量**：PG `DEFAULT CURRENT_TIMESTAMP` 也未生效。改 entity 字段 `insertable = false, updatable = false`，让 PG DEFAULT 接手。
  - **B. SQL 里 createdAt 是 `-infinity` 字面量**：Hibernate 绑了 `OffsetDateTime.MIN` 静态值。多半是 `@PrePersist` / `@CreationTimestamp` 之间的 race。修法 = 全 entity `OffsetDateTime` → `Instant`，mapper 层加转换。**这是「技术选型变更」，需开 ADR**（建议 `docs/adr/0016-offsetdatetime-vs-instant.md`）。
  - **C. SQL 一切正常但查出来 `-infinity`**：DB 那行确实被另进程写入了 `-infinity`（PG `::timestamptz '-infinity'` 是合法的）。修法 = DB 触发器或 SELECT 包装 `CASE WHEN created_at = '-infinity'::timestamptz THEN NOW() ELSE created_at END`。
- [ ] **第三步（防御性 + 序列化层）**：`application.yml` 加 Jackson 配置 + 新建 `JacksonConfig.java` 自定义 `JavaTimeModule`，对 `OffsetDateTime.MIN` 加 override 输出 `null`。

### [BUG] audit `?action=api_key_created` 5 页内找不到 `metadata.apiKeyId=xxx` 事件

- **状态**: 待定位
- **首次发现**: 2026-09-01 live mode 全量 contract-test run
- **关联 ADR**: 无（建议补 `0017-audit-insert-best-effort.md`）
- **关联合约测试**: `M96.F02.I18 写端点副作用 — api_key_created/revoked 事件进 audit_events`

### 症状（活证据）

live mode 全量跑 I18，springboot 端 `GET /tenants/{t}/audit-events?action=api_key_created&pageSize=100` 翻 5 页（500 行）找不到 `metadata.apiKeyId=5cbafcf1-...` 的事件。同测试在 aspnetcore 端绿。说明 springboot 端 audit 写入路径有断链。

### 已知事实

- `TenantApiKeysController.tenantApiKeysCreateApiKey()` 直接调 `service.create()`，**无 AOP / Interceptor 写 audit**（✓ 设计如此）。
- `TenantApiKeyService.create()` line 58-65 调 `auditWriter.write(tenantId, ..., "api_key_created", null, Map.of("apiKeyId", resp.getApiKey().getId().toString()))`。action 字符串是 `"api_key_created"`（snake_case 小写），metadata key 是 `"apiKeyId"`（camelCase）。**两端命名都和 contract-test 期望对齐**。
- `AuditWriter.write()` line 47-54 有个早期 return 防御：
  ```text
  if (metadata != null && metadata.get("apiKeyId") == null) { return; }
  ```
  这个检查**只对 metadata key 名为 `apiKeyId` 时生效**，但与上面 service 调用对得上。**理论上不该 swallow**。
- `AuditWriter` 在 action 字符串与 enum 桥接上有 `valueOf(...)` 双重转换（`AuditAction.fromValue(s).name()` → `valueOf` 映射到本地 enum），任何一处对不上就静默 swallow。
- `TenantAuditController` 接 `@RequestParam AuditAction action`，**Spring 默认 binding 用 `name()`（UPPER_SNAKE）**，与 contract-test 发的 `?action=api_key_created`（lower_snake）**可能 binding 失败**，controller 收到 `action == null` → 查全表（500 行可能仍翻不到具体行）。
- `AuditEventEntity.metadata` 是 `Map<String, Object>` + `JdbcTypeCode(SqlTypes.JSON)` + `jsonb NOT NULL DEFAULT '{}'`，Hibernate 用 Jackson serialize。Jackson 默认保留 camelCase key 名不 snake_case 化（✓ 与 service 写入对齐）。
- `metadata` 列有 GIN 索引（`V007__indexes.sql:23` `idx_audit_events_metadata_gin ON audit_events USING gin (metadata)`）。
- unit test `TenantApiKeyServiceTest` 用 `mock(AuditWriter.class)`，**完全没 verify `auditWriter.write(...)` 是否真的被调用**——走过等于"测了"是假绿。

### 三条候选根因方向（按概率从高到低）

1. **TenantAuditController.action 参数 binding 失败**：Spring 默认 `@RequestParam AuditAction action` 用 `name()` 匹配，contract-test 发的是 `api_key_created`（小写下划线）→ 收 `null` → 不按 action 过滤 → 翻全表 500 行没找到。
2. **AuditWriter.write line 47-54 early-return swallow**：如果某次调用 metadata.get("apiKeyId") 在序列化前已经被 Jackson 转 snake_case（不太可能但要排）。
3. **AuditAction 双重 enum 桥接失败**：`AuditAction.fromValue("api_key_created").name()` 必须返回 `API_KEY_CREATED` 才能映射到本地 enum。任何改名或大小写变化都会 `IllegalArgumentException` 被 swallow（要 grep `catch (IllegalArgumentException)` 看是否有 swallow）。

### 调查步骤（按代价从小到大）

- [ ] 1. **必做**：直 curl `GET /tenants/{t}/audit-events?action=api_key_created` 看 springboot 是否返 200 + 全表数据（验证根因 #1）
- [ ] 2. **必做**：`SELECT action, metadata FROM audit_events ORDER BY occurred_at DESC LIMIT 20;` 看 DB 实际有没有 `api_key_created` 事件行（如果有说明写入 OK，问题在 controller binding；如果没有说明写入断了，问题在 service / AuditWriter）
- [ ] 3. `grep -n "catch.*Exception\|catch.*Throwable" src/main/java/saas/identity/platform/service/AuditWriter.java` 看是否有 swallow
- [ ] 4. `grep -rn "AuditAction.fromValue\|@JsonCreator" src/main/java/saas/identity/` 看 enum 桥接配置
- [ ] 5. 修 controller 显式 `@RequestParam("action") String action`（不接 enum），在 service 层用 `AuditAction.fromValue(action)` 转一次
- [ ] 6. 修 AuditWriter line 47-54：删 swallow，改成 `log.warn` 但继续 save（`apiKeyId == null` 不应该 skip）
- [ ] 7. 抽常量 `METADATA_API_KEY_ID = "apiKeyId"` 避免 magic string
- [ ] 8. 改 `TenantApiKeyServiceTest` 加 `verify(auditWriter).write(eq(tid), any(), eq("api_key_created"), isNull(), argThat(m -> m != null && m.get("apiKeyId") != null))`
- [ ] 9. 加 `@DataJpaTest` 跑 `AuditEventRepository.save(...)` 真插入验证 metadata 字段存进去了

### 修复后回归

- [ ] contract-test `M96.F02.I18` springboot 端转绿
- [ ] mvn test 全绿（特别是 TenantApiKeyServiceTest 新加的 verify）
- [ ] 本机 prod-build smoke：POST `/api-keys` → `GET /audit-events?action=api_key_created` 第 1 页就有新事件

### 风险

audit_events 表从不 truncate，contract-test 4 后端共用 DB，跑 50+ 次累计事件数远超 500 行。修本 bug 时**同时需要 contract-test 仓的 `beforeAll` 加 `TRUNCATE audit_events WHERE id NOT IN (V016 seed id 列表)`**（保留 V016 种子事件不动，只清跑出来的）。**这是跨仓 PR，不是 backend 仓单方面能解的事**，见 `saas-identity-platform-contract-test/PLAN.md` 新建 [DEBT] 工单。

## 迭代方向

- （待补）

