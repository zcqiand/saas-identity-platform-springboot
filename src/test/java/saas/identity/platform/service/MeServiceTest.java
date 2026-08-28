package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import saas.identity.platform.entity.TenantMembershipEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.enums.MembershipStatus;
import saas.identity.platform.enums.UserStatus;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.MenuRepository;
import saas.identity.platform.repository.RoleMenuGrantRepository;
import saas.identity.platform.repository.TenantMembershipRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.CurrentUser;
import saas.identity.shared.dto.SwitchTenantResponse;

/** MeService 单测（M00.F02 当前用户身份）。 */
class MeServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final TenantMembershipRepository membershipRepository =
      mock(TenantMembershipRepository.class);
  // 与 SecurityConfig.jwtDecoder 同 key：switchTenant 签出的 token 必须能被本仓 decoder 验过
  private final JwtIssuer jwt =
      new JwtIssuer("unit-test-signing-key-0123456789abcdef0123", "ut-issuer", "ut-aud", 3600);
  private final MeService service =
      new MeService(
          userRepository,
          membershipRepository,
          mock(RoleMenuGrantRepository.class),
          mock(MenuRepository.class),
          mock(AppRepository.class),
          jwt);

  private UserEntity user(UUID userId, UUID tenantId) {
    UserEntity u = new UserEntity();
    u.setId(userId);
    u.setTenantId(tenantId);
    u.setUsername("alice");
    u.setEmail("alice@example.com");
    u.setDisplayName("Alice");
    u.setStatus(UserStatus.ACTIVE);
    return u;
  }

  private TenantMembershipEntity membership(UUID userId, UUID tenantId, MembershipStatus status) {
    TenantMembershipEntity m = new TenantMembershipEntity();
    m.setId(UUID.randomUUID());
    m.setUserId(userId);
    m.setTenantId(tenantId);
    m.setStatus(status);
    m.setRoleIds(List.of());
    m.setJoinedAt(OffsetDateTime.now());
    return m;
  }

  @Test
  @Fn({"M00.F02.I01"})
  void whoami_returnsCurrentUser() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, tenantId)));
    when(membershipRepository.findByUserId(userId))
        .thenReturn(List.of(membership(userId, tenantId, MembershipStatus.ACTIVE)));

    CurrentUser cu = service.whoami(userId);
    assertEquals(userId, cu.getId());
    assertEquals("alice@example.com", cu.getEmail());
    assertEquals(1, cu.getMemberships().size());
    assertEquals(tenantId, cu.getCurrentTenantId());
  }

  @Test
  @Fn({"M00.F02.I01"})
  void whoami_throwsIfUserMissing() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.whoami(userId));
  }

  @Test
  @Fn({"M00.F02.I02"})
  void listMyTenants_excludesRemoved() {
    UUID userId = UUID.randomUUID();
    UUID t1 = UUID.randomUUID();
    UUID t2 = UUID.randomUUID();
    when(membershipRepository.findByUserId(userId))
        .thenReturn(
            List.of(
                membership(userId, t1, MembershipStatus.ACTIVE),
                membership(userId, t2, MembershipStatus.REMOVED)));
    var list = service.listMyTenants(userId);
    assertEquals(1, list.size());
    assertEquals(t1, list.get(0).getTenantId());
  }

  @Test
  @Fn({"M00.F02.I03"})
  void switchTenant_returnsToken() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    when(membershipRepository.findByUserIdAndTenantId(userId, tenantId))
        .thenReturn(Optional.of(membership(userId, tenantId, MembershipStatus.ACTIVE)));
    SwitchTenantResponse resp = service.switchTenant(userId, tenantId);
    // 回归 2026-08-28 线上 401：switchTenant 必须发 HS256 真签 token（同 login）
    String token = resp.getAccessToken();
    assertNotNull(token);
    assertEquals(3, token.split("\\.").length, "HS256 JWT 应为三段");
    assertFalse(token.endsWith("dev-placeholder"), "不得再发 dev-placeholder 假签");
    assertEquals(tenantId, resp.getTenantId());
  }

  @Test
  @Fn({"M00.F02.I03"})
  void switchTenant_throwsIfNotMember() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    when(membershipRepository.findByUserIdAndTenantId(userId, tenantId))
        .thenReturn(Optional.empty());
    assertThrows(SecurityException.class, () -> service.switchTenant(userId, tenantId));
  }
}
