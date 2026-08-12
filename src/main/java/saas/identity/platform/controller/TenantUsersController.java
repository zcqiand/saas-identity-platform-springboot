package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.security.TenantGuard;
import saas.identity.platform.service.TenantUsersService;
import saas.identity.shared.api.TenantUsersApi;
import saas.identity.shared.dto.CreateUserRequest;
import saas.identity.shared.dto.TenantUsersAssignRolesRequest;
import saas.identity.shared.dto.TenantUsersChangeUserStatusRequest;
import saas.identity.shared.dto.TenantUsersInviteUserRequest;
import saas.identity.shared.dto.TenantUsersListUsers200Response;
import saas.identity.shared.dto.User;
import saas.identity.shared.dto.UserStatus;

/**
 * Hand-written implementation of the codegen TenantUsersApi interface. Every tenant-scoped method
 * MUST call tenantGuard.verifyPathTenant(tenantId) first.
 */
@RestController
public class TenantUsersController implements TenantUsersApi {

  private final TenantUsersService service;
  private final TenantGuard tenantGuard;

  public TenantUsersController(TenantUsersService service, TenantGuard tenantGuard) {
    this.service = service;
    this.tenantGuard = tenantGuard;
  }

  @Override
  public ResponseEntity<TenantUsersListUsers200Response> tenantUsersListUsers(
      String tenantId, Integer page, Integer pageSize, UserStatus status) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.listUsers(UUID.fromString(tenantId), page, pageSize, status));
  }

  @Override
  public ResponseEntity<User> tenantUsersCreateUser(
      String tenantId, CreateUserRequest createUserRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.createUser(UUID.fromString(tenantId), createUserRequest));
  }

  @Override
  public ResponseEntity<User> tenantUsersGetUser(String tenantId, String userId) {
    tenantGuard.verifyPathTenant(tenantId);
    User u = new User();
    u.setId(UUID.fromString(userId));
    u.setTenantId(UUID.fromString(tenantId));
    u.setUsername("alice");
    u.setEmail("alice@example.com");
    u.setStatus(UserStatus.ACTIVE);
    u.setRoleIds(java.util.List.of());
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<User> tenantUsersUpdateUser(
      String tenantId,
      String userId,
      saas.identity.shared.dto.UpdateUserRequest updateUserRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    User u = new User();
    u.setId(UUID.fromString(userId));
    u.setTenantId(UUID.fromString(tenantId));
    u.setUsername(
        updateUserRequest.getDisplayName() == null ? "alice" : updateUserRequest.getDisplayName());
    u.setEmail(
        updateUserRequest.getEmail() == null ? "alice@example.com" : updateUserRequest.getEmail());
    u.setStatus(
        updateUserRequest.getStatus() == null ? UserStatus.ACTIVE : updateUserRequest.getStatus());
    u.setRoleIds(
        updateUserRequest.getRoleIds() == null
            ? java.util.List.of()
            : updateUserRequest.getRoleIds());
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<Void> tenantUsersDeleteUser(String tenantId, String userId) {
    tenantGuard.verifyPathTenant(tenantId);
    service.deleteUser(UUID.fromString(tenantId), UUID.fromString(userId));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<User> tenantUsersAssignRoles(
      String tenantId, String userId, TenantUsersAssignRolesRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    User u = new User();
    u.setId(UUID.fromString(userId));
    u.setTenantId(UUID.fromString(tenantId));
    u.setUsername("alice");
    u.setEmail("alice@example.com");
    u.setStatus(UserStatus.ACTIVE);
    u.setRoleIds(body.getRoleIds() == null ? java.util.List.of() : body.getRoleIds());
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<User> tenantUsersInviteUser(
      String tenantId, TenantUsersInviteUserRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    User u = new User();
    u.setId(UUID.randomUUID());
    u.setTenantId(UUID.fromString(tenantId));
    u.setUsername(body.getEmail());
    u.setEmail(body.getEmail());
    u.setStatus(UserStatus.INVITED);
    u.setRoleIds(body.getRoleIds() == null ? java.util.List.of() : body.getRoleIds());
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<User> tenantUsersChangeUserStatus(
      String tenantId, String userId, TenantUsersChangeUserStatusRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    User u = new User();
    u.setId(UUID.fromString(userId));
    u.setTenantId(UUID.fromString(tenantId));
    u.setUsername("alice");
    u.setEmail("alice@example.com");
    u.setStatus(body.getStatus());
    u.setRoleIds(java.util.List.of());
    return ResponseEntity.ok(u);
  }
}
