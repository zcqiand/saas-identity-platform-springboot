package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.security.TenantGuard;
import saas.identity.platform.service.TenantRoleService;
import saas.identity.shared.api.TenantRolesApi;
import saas.identity.shared.dto.CreateRoleRequest;
import saas.identity.shared.dto.Role;
import saas.identity.shared.dto.TenantRolesListRoles200Response;
import saas.identity.shared.dto.TenantRolesSetPermissionsRequest;
import saas.identity.shared.dto.UpdateRoleRequest;

/**
 * M02.F01 + M02.F02 — 角色 CRUD + 权限矩阵。 tenant-scoped：每个方法首调 tenantGuard.verifyPathTenant。 业务在 {@link
 * TenantRoleService}。
 */
@RestController
public class TenantRolesController implements TenantRolesApi {

  private static final int PAGE_DEFAULT = 0;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final TenantRoleService service;
  private final TenantGuard tenantGuard;

  public TenantRolesController(TenantRoleService service, TenantGuard tenantGuard) {
    this.service = service;
    this.tenantGuard = tenantGuard;
  }

  private int normPage(Integer page) {
    return page == null ? PAGE_DEFAULT : Math.max(0, page);
  }

  private int normPageSize(Integer pageSize) {
    return pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
  }

  @Override
  public ResponseEntity<TenantRolesListRoles200Response> tenantRolesListRoles(
      String tenantId, Integer page, Integer pageSize) {
    tenantGuard.verifyPathTenant(tenantId);
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    var result = service.list(UUID.fromString(tenantId), p, ps);
    var body =
        new TenantRolesListRoles200Response()
            .items(result.getContent())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<Role> tenantRolesCreateRole(
      String tenantId, CreateRoleRequest createRoleRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.create(UUID.fromString(tenantId), createRoleRequest));
  }

  @Override
  public ResponseEntity<Role> tenantRolesGetRole(String tenantId, String roleId) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.get(UUID.fromString(tenantId), UUID.fromString(roleId)));
  }

  @Override
  public ResponseEntity<Role> tenantRolesUpdateRole(
      String tenantId, String roleId, UpdateRoleRequest updateRoleRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(
        service.update(UUID.fromString(tenantId), UUID.fromString(roleId), updateRoleRequest));
  }

  @Override
  public ResponseEntity<Void> tenantRolesDeleteRole(String tenantId, String roleId) {
    tenantGuard.verifyPathTenant(tenantId);
    service.delete(UUID.fromString(tenantId), UUID.fromString(roleId));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Role> tenantRolesSetPermissions(
      String tenantId,
      String roleId,
      TenantRolesSetPermissionsRequest tenantRolesSetPermissionsRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    var ids =
        tenantRolesSetPermissionsRequest.getPermissionIds() == null
            ? java.util.List.<String>of()
            : tenantRolesSetPermissionsRequest.getPermissionIds();
    return ResponseEntity.ok(
        service.setPermissions(UUID.fromString(tenantId), UUID.fromString(roleId), ids));
  }
}
