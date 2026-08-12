package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.security.TenantContext;
import saas.identity.platform.security.TenantGuard;
import saas.identity.shared.dto.CreateUserRequest;
import saas.identity.shared.dto.TenantUsersListUsers200Response;
import saas.identity.shared.dto.User;
import saas.identity.shared.dto.UserStatus;

/**
 * TenantUsersService + TenantGuard tests. Each test uses @Fn to claim a function ID for L5
 * alignment.
 */
class TenantUsersServiceTest {

  private final TenantUsersService service = new TenantUsersService();

  @Test
  @Fn({"M01.F01.I01"})
  void listUsers_returnsPage() {
    UUID tid = UUID.randomUUID();
    TenantUsersListUsers200Response p = service.listUsers(tid, 0, 20, null);
    assertNotNull(p);
    assertEquals(1, p.getItems().size());
    assertEquals(tid, p.getItems().get(0).getTenantId());
  }

  @Test
  @Fn({"M01.F01.I02"})
  void createUser_returnsUser() {
    UUID tid = UUID.randomUUID();
    CreateUserRequest body = new CreateUserRequest();
    body.setUsername("alice");
    body.setEmail("alice@example.com");
    body.setPassword("p");
    User u = service.createUser(tid, body);
    assertEquals("alice", u.getUsername());
    assertEquals(UserStatus.INVITED, u.getStatus());
  }

  @Test
  @Fn({"M01.F01.I05"})
  void deleteUser_isNoOp() {
    UUID tid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    // Should not throw
    service.deleteUser(tid, uid);
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
