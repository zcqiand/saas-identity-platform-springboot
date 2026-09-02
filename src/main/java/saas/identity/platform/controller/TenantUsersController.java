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
    return ResponseEntity.ok(service.getUser(UUID.fromString(tenantId), UUID.fromString(userId)));
  }

  @Override
  public ResponseEntity<User> tenantUsersUpdateUser(
      String tenantId,
      String userId,
      saas.identity.shared.dto.UpdateUserRequest updateUserRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    // 2026-09-01 contract-test I39：原内存 stub 写不落库，切 service（DB-backed）
    return ResponseEntity.ok(
        service.updateUser(UUID.fromString(tenantId), UUID.fromString(userId), updateUserRequest));
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
    // 2026-09-01 contract-test I40：原内存 stub，切 service（memberships authoritative 双写）
    java.util.List<UUID> roleIds =
        body.getRoleIds() == null
            ? java.util.List.of()
            : body.getRoleIds().stream().map(UUID::fromString).toList();
    return ResponseEntity.ok(
        service.assignRoles(UUID.fromString(tenantId), UUID.fromString(userId), roleIds));
  }

  @Override
  public ResponseEntity<User> tenantUsersInviteUser(
      String tenantId, TenantUsersInviteUserRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    // 2026-09-01 contract-test I42：原内存 stub 不落库（DELETE 随即 400），切 service
    java.util.List<UUID> roleIds =
        body.getRoleIds() == null
            ? null
            : body.getRoleIds().stream().map(UUID::fromString).toList();
    return ResponseEntity.ok(
        service.inviteUser(UUID.fromString(tenantId), body.getEmail(), roleIds));
  }

  @Override
  public ResponseEntity<User> tenantUsersChangeUserStatus(
      String tenantId, String userId, TenantUsersChangeUserStatusRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    // 2026-09-01 contract-test I41：原内存 stub，切 service（status 真往返）
    return ResponseEntity.ok(
        service.changeUserStatus(
            UUID.fromString(tenantId), UUID.fromString(userId), body.getStatus()));
  }
}
