package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.TenantContext;
import saas.identity.platform.security.TenantGuard;
import saas.identity.shared.dto.CreateUserRequest;
import saas.identity.shared.dto.TenantUsersListUsers200Response;
import saas.identity.shared.dto.User;
import saas.identity.shared.dto.UserStatus;

/**
 * TenantUsersService 单测 + TenantGuard 测试。 单元测试用 Mockito mock UserRepository；DB 真实 CRUD 见
 * UserRepositoryDataJpaTest（M09.F03）。
 */
class TenantUsersServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final saas.identity.platform.repository.TenantMembershipRepository membershipRepository =
      mock(saas.identity.platform.repository.TenantMembershipRepository.class);
  private final AuditWriter auditWriter = mock(AuditWriter.class);
  private final TenantUsersService service =
      new TenantUsersService(userRepository, membershipRepository, auditWriter);

  @Test
  @Fn({"M01.F01.I01"})
  void listUsers_returnsPage() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    UserEntity u = new UserEntity();
    u.setId(uid);
    u.setTenantId(tid);
    u.setUsername("alice");
    u.setEmail("alice@example.com");
    u.setStatus(saas.identity.platform.enums.UserStatus.ACTIVE);
    u.setRoleIds(List.of());
    u.setCreatedAt(OffsetDateTime.now());
    u.setUpdatedAt(OffsetDateTime.now());
    Page<UserEntity> page = new PageImpl<>(List.of(u));
    when(userRepository.findByTenantId(any(UUID.class), any(Pageable.class))).thenReturn(page);

    TenantUsersListUsers200Response p = service.listUsers(tid, 0, 20, null);
    assertNotNull(p);
    assertEquals(1, p.getItems().size());
    assertEquals(tid, p.getItems().get(0).getTenantId());
    assertEquals("alice", p.getItems().get(0).getUsername());
  }

  @Test
  @Fn({"M01.F01.I02"})
  void createUser_returnsUser() {
    UUID tid = UUID.randomUUID();
    // save mock 模拟 DB @GeneratedValue：填 id（createUser 现在会读 getId() 写审计 metadata）
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(
            inv -> {
              UserEntity e = inv.getArgument(0);
              if (e.getId() == null) e.setId(UUID.randomUUID());
              return e;
            });
    CreateUserRequest body = new CreateUserRequest();
    body.setUsername("alice");
    body.setEmail("alice@example.com");
    body.setPassword("p");
    User u = service.createUser(tid, body);
    assertEquals("alice", u.getUsername());
    assertEquals(UserStatus.ACTIVE, u.getStatus());
    assertEquals(tid, u.getTenantId());
  }

  // 2026-09-02 contract-test M96 audit 覆盖对齐：createUser 成功必须写 user_created 审计事件
  // （msw/nextjs 已写，本仓此前缺失）。形状对齐 nextjs users/route.ts：metadata={userId}。
  // actorUserId：service 层无请求上下文，写 null（系统动作），与 msw 的 undefined 同语义。
  @Test
  @Fn({"M01.F01.I02"})
  void createUser_writesAuditEvent() {
    UUID tid = UUID.randomUUID();
    UUID generatedId = UUID.randomUUID();
    // @GeneratedValue 在 mock save 下不触发，手动填 id 模拟 DB 生成
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(
            inv -> {
              UserEntity e = inv.getArgument(0);
              e.setId(generatedId);
              return e;
            });
    CreateUserRequest body = new CreateUserRequest();
    body.setUsername("bob");
    body.setEmail("bob@example.com");
    body.setPassword("p");

    service.createUser(tid, body);

    verify(auditWriter)
        .write(
            eq(tid),
            eq(null),
            eq("user_created"),
            eq(generatedId),
            eq(java.util.Map.of("userId", generatedId.toString())));
  }

  @Test
  @Fn({"M01.F01.I05"})
  void deleteUser_throwsIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    when(userRepository.findByTenantIdAndId(tid, uid)).thenReturn(java.util.Optional.empty());
    // 2026-09-01 contract-test I43：资源不存在语义 = NoSuchElementException → 404
    // （原 IllegalArgumentException → 400，与家族 TenantApiKeyService/AdminTenantService 分叉）
    assertThrows(java.util.NoSuchElementException.class, () -> service.deleteUser(tid, uid));
  }

  // 2026-09-01 contract-test I39/I70：PATCH 不存在 user 必须 404（NSEE），不是 400（IAE）
  @Test
  @Fn({"M01.F01.I04"})
  void updateUser_throwsNoSuchIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    when(userRepository.findByTenantIdAndId(tid, uid)).thenReturn(java.util.Optional.empty());
    saas.identity.shared.dto.UpdateUserRequest body =
        new saas.identity.shared.dto.UpdateUserRequest();
    body.setDisplayName("noop");
    assertThrows(java.util.NoSuchElementException.class, () -> service.updateUser(tid, uid, body));
  }

  // 2026-09-01 contract-test I40 前置：assignRoles 不存在 user 同款 404 语义
  @Test
  @Fn({"M01.F02.I01"})
  void assignRoles_throwsNoSuchIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    when(userRepository.findByTenantIdAndId(tid, uid)).thenReturn(java.util.Optional.empty());
    assertThrows(
        java.util.NoSuchElementException.class, () -> service.assignRoles(tid, uid, List.of()));
  }

  // 2026-09-01 contract-test I42/I43 前置：getUser 不存在同款 404 语义
  @Test
  @Fn({"M01.F01.I03"})
  void getUser_throwsNoSuchIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    when(userRepository.findByTenantIdAndId(tid, uid)).thenReturn(java.util.Optional.empty());
    assertThrows(java.util.NoSuchElementException.class, () -> service.getUser(tid, uid));
  }

  @Test
  @Fn({"M00.F01.I03"})
  void tenantGuard_throwsOnMismatch() {
    TenantContext ctx = mock(TenantContext.class);
    when(ctx.currentTenantId()).thenReturn("tenant-A");
    TenantGuard guard = new TenantGuard(ctx);
    assertThrows(AccessDeniedException.class, () -> guard.verifyPathTenant("tenant-B"));
  }

  @Test
  @Fn({"M00.F01.I03"})
  void tenantGuard_acceptsMatch() {
    TenantContext ctx = mock(TenantContext.class);
    when(ctx.currentTenantId()).thenReturn("tenant-A");
    TenantGuard guard = new TenantGuard(ctx);
    guard.verifyPathTenant("tenant-A"); // should not throw
  }
}
