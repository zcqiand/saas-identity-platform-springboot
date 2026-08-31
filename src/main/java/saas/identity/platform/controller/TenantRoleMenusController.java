package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.security.TenantGuard;
import saas.identity.platform.service.TenantRoleMenuService;
import saas.identity.shared.api.TenantRoleMenusApi;
import saas.identity.shared.dto.RoleMenuGrant;
import saas.identity.shared.dto.SetRoleMenusRequest;

/**
 * M09.F01 + M09.F02 — 角色 ↔ 菜单 授权 controller。
 *
 * <p>2026-08-30：新增（NSwag 早生成 TenantRoleMenusApi，service 早建好 TenantRoleMenuService，一直缺
 * controller；contract-test M96.F02.I09 拉齐 4 后端时暴露）。
 */
@RestController
public class TenantRoleMenusController implements TenantRoleMenusApi {

  private final TenantRoleMenuService service;
  private final TenantGuard tenantGuard;

  public TenantRoleMenusController(TenantRoleMenuService service, TenantGuard tenantGuard) {
    this.service = service;
    this.tenantGuard = tenantGuard;
  }

  @Override
  public ResponseEntity<RoleMenuGrant> tenantRoleMenusListRoleMenus(
      String tenantId, String roleId) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.get(UUID.fromString(tenantId), UUID.fromString(roleId)));
  }

  @Override
  public ResponseEntity<RoleMenuGrant> tenantRoleMenusSetRoleMenus(
      String tenantId, String roleId, SetRoleMenusRequest setRoleMenusRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(
        service.set(
            UUID.fromString(tenantId),
            UUID.fromString(roleId),
            setRoleMenusRequest == null ? null : setRoleMenusRequest.getMenuIds()));
  }

  @Override
  public ResponseEntity<Void> tenantRoleMenusClearRoleMenus(String tenantId, String roleId) {
    tenantGuard.verifyPathTenant(tenantId);
    service.clear(UUID.fromString(roleId));
    return ResponseEntity.noContent().build();
  }
}
