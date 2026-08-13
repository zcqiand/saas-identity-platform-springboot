package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
  private final TenantUsersService service = new TenantUsersService(userRepository);

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
    when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    CreateUserRequest body = new CreateUserRequest();
    body.setUsername("alice");
    body.setEmail("alice@example.com");
    body.setPassword("p");
    User u = service.createUser(tid, body);
    assertEquals("alice", u.getUsername());
    assertEquals(UserStatus.INVITED, u.getStatus());
    assertEquals(tid, u.getTenantId());
  }

  @Test
  @Fn({"M01.F01.I05"})
  void deleteUser_throwsIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    when(userRepository.findByTenantIdAndId(tid, uid)).thenReturn(java.util.Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> service.deleteUser(tid, uid));
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
