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

### 风险

合约测试 ADR-0015-amend 通过后，I10 会改用「格式断言」比较 4 后端时间戳格式
（`…Z` 毫秒）。**新断言下 `-292275055-05-16T23:00:00Z` 仍是合法格式（毫秒 + Z），
仍然不会红** —— 所以**这条 bug 在 ADR 通过后会被合约侧盖住，必须在此工单锁死修复日期**，
否则 Hibernate 行为变化时再暴露没人会发现。

## 迭代方向

- （待补）
