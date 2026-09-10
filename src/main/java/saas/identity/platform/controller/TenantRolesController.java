package saas.identity.platform.controller;

import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.repository.SysRoleRepository;
import saas.identity.platform.security.TenantGuard;
import saas.identity.shared.api.TenantRolesApi;
import saas.identity.shared.dto.CreateSysRoleRequest;
import saas.identity.shared.dto.TenantRolesListSysRoles200Response;
import saas.identity.shared.dto.UpdateSysRoleRequest;

/** M00.F03 租户角色 CRUD。skeleton。 */
@RestController
@Transactional
public class TenantRolesController implements TenantRolesApi {

  private final SysRoleRepository roles;
  private final TenantGuard tenantGuard;

  public TenantRolesController(SysRoleRepository roles, TenantGuard tenantGuard) {
    this.roles = roles;
    this.tenantGuard = tenantGuard;
  }

  @Override
  public ResponseEntity<TenantRolesListSysRoles200Response> tenantRolesListSysRoles(
      String tenantId, String clientId, Integer page, Integer pageSize) {
    tenantGuard.verifyPathTenant(tenantId);
    int p = page == null ? 0 : page;
    int ps = pageSize == null ? 20 : pageSize;
    var pg = roles.findAll(PageRequest.of(p, ps));
    TenantRolesListSysRoles200Response resp = new TenantRolesListSysRoles200Response();
    resp.setItems(pg.getContent().stream().map(this::toDto).toList());
    resp.setTotal(pg.getTotalElements());
    resp.setPage(p);
    resp.setPageSize(ps);
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysRole> tenantRolesCreateSysRole(
      String tenantId, CreateSysRoleRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
    saas.identity.platform.entity.Generated.SysRole e =
        new saas.identity.platform.entity.Generated.SysRole();
    e.setTenantId(UUID.fromString(tenantId));
    e.setClientId(body.getClientId());
    e.setRoleCode(body.getRoleCode());
    e.setRoleName(body.getRoleName());
    e.setDescription(body.getDescription());
    e.setIsPreset(body.getIsPreset() != null && body.getIsPreset());
    e.setStatus((short) 1);
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    return ResponseEntity.ok(toDto(roles.save(e)));
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysRole> tenantRolesGetSysRole(
      String tenantId, String roleId) {
    tenantGuard.verifyPathTenant(tenantId);
    UUID roleUuid = UUID.fromString(roleId);
    saas.identity.platform.entity.Generated.SysRole e =
        roles.findById(roleUuid).orElseThrow(() -> new NoSuchElementException("role " + roleId));
    return ResponseEntity.ok(toDto(e));
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysRole> tenantRolesUpdateSysRole(
      String tenantId, String roleId, UpdateSysRoleRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    UUID roleUuid = UUID.fromString(roleId);
    saas.identity.platform.entity.Generated.SysRole e =
        roles.findById(roleUuid).orElseThrow(() -> new NoSuchElementException("role " + roleId));
    if (body.getRoleName() != null) e.setRoleName(body.getRoleName());
    if (body.getDescription() != null) e.setDescription(body.getDescription());
    e.setUpdatedAt(java.time.OffsetDateTime.now());
    return ResponseEntity.ok(toDto(roles.save(e)));
  }

  @Override
  public ResponseEntity<Void> tenantRolesDeleteSysRole(String tenantId, String roleId) {
    tenantGuard.verifyPathTenant(tenantId);
    UUID roleUuid = UUID.fromString(roleId);
    roles.deleteById(roleUuid);
    return ResponseEntity.noContent().build();
  }

  private saas.identity.shared.dto.SysRole toDto(
      saas.identity.platform.entity.Generated.SysRole e) {
    saas.identity.shared.dto.SysRole d = new saas.identity.shared.dto.SysRole();
    d.setId(e.getId());
    d.setTenantId(e.getTenantId());
    d.setClientId(e.getClientId());
    d.setRoleCode(e.getRoleCode());
    d.setRoleName(e.getRoleName());
    d.setDescription(e.getDescription());
    d.setIsPreset(e.getIsPreset());
    d.setStatus(e.getStatus() == null ? null : e.getStatus().intValue());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }
}
