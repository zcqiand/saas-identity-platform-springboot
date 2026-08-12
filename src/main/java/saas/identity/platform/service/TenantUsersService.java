package saas.identity.platform.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import saas.identity.shared.dto.CreateUserRequest;
import saas.identity.shared.dto.TenantUsersListUsers200Response;
import saas.identity.shared.dto.User;
import saas.identity.shared.dto.UserStatus;

/**
 * SCAFFOLD GET:/api/tenants/{tenantId}/users SCAFFOLD POST:/api/tenants/{tenantId}/users SCAFFOLD
 * DELETE:/api/tenants/{tenantId}/users/{userId}
 *
 * <p>This service is HAND-WRITTEN business logic. The Controller interface (TenantUsersApi) is
 * codegen — don't add methods there.
 */
@Service
public class TenantUsersService {

  public TenantUsersListUsers200Response listUsers(
      UUID tenantId, Integer page, Integer pageSize, UserStatus status) {
    // M01.F01.I01 — list users in tenant
    // SCAFFOLD GET:/api/tenants/{tenantId}/users
    User u = new User();
    u.setId(UUID.randomUUID());
    u.setTenantId(tenantId);
    u.setUsername("alice");
    u.setEmail("alice@example.com");
    u.setStatus(UserStatus.ACTIVE);
    u.setRoleIds(List.of());
    TenantUsersListUsers200Response p =
        new TenantUsersListUsers200Response(
            List.of(u), page == null ? 0 : page, pageSize == null ? 20 : pageSize, 1L);
    return p;
  }

  public User createUser(UUID tenantId, CreateUserRequest body) {
    // M01.F01.I02 — create user in tenant
    // SCAFFOLD POST:/api/tenants/{tenantId}/users
    User u = new User();
    u.setId(UUID.randomUUID());
    u.setTenantId(tenantId);
    u.setUsername(body.getUsername());
    u.setEmail(body.getEmail());
    u.setStatus(UserStatus.INVITED);
    u.setRoleIds(body.getRoleIds() == null ? List.of() : body.getRoleIds());
    return u;
  }

  public void deleteUser(UUID tenantId, UUID userId) {
    // M01.F01.I05 — delete user in tenant
    // SCAFFOLD DELETE:/api/tenants/{tenantId}/users/{userId}
    // no-op for scaffold
  }
}
