package saas.identity.platform.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.SysRoleMenu;
import saas.identity.platform.repository.SysRoleMenuRepository;
import saas.identity.shared.api.TenantRoleMenusApi;
import saas.identity.shared.dto.SetSysRoleMenusRequest;

/** M00.F04 角色菜单授权（I02-I04）。skeleton。 */
@RestController
public class TenantRoleMenusController implements TenantRoleMenusApi {

  private final SysRoleMenuRepository roleMenus;

  public TenantRoleMenusController(SysRoleMenuRepository roleMenus) {
    this.roleMenus = roleMenus;
  }

  @Override
  public ResponseEntity<List<saas.identity.shared.dto.SysRoleMenu>> tenantRoleMenusListSysRoleMenus(
      String tenantId, String roleId, String clientId) {
    UUID roleUuid = UUID.fromString(roleId);
    List<SysRoleMenu> rows = roleMenus.findByRoleId(roleUuid);
    return ResponseEntity.ok(rows.stream().map(this::toDto).toList());
  }

  @Override
  public ResponseEntity<List<saas.identity.shared.dto.SysRoleMenu>> tenantRoleMenusSetSysRoleMenus(
      String tenantId, String roleId, String clientId, SetSysRoleMenusRequest body) {
    UUID roleUuid = UUID.fromString(roleId);
    roleMenus.deleteByRoleId(roleUuid);
    List<saas.identity.shared.dto.SysRoleMenu> saved = List.of();
    if (body != null && body.getMenuIds() != null) {
      for (String menuId : body.getMenuIds()) {
        SysRoleMenu r = new SysRoleMenu();
        r.setRoleId(roleUuid);
        r.setMenuId(UUID.fromString(menuId));
        roleMenus.save(r);
      }
      saved = roleMenus.findByRoleId(roleUuid).stream().map(this::toDto).toList();
    }
    return ResponseEntity.ok(saved);
  }

  @Override
  public ResponseEntity<Void> tenantRoleMenusClearSysRoleMenus(
      String tenantId, String roleId, String clientId) {
    UUID roleUuid = UUID.fromString(roleId);
    roleMenus.deleteByRoleId(roleUuid);
    return ResponseEntity.noContent().build();
  }

  private saas.identity.shared.dto.SysRoleMenu toDto(SysRoleMenu e) {
    saas.identity.shared.dto.SysRoleMenu d = new saas.identity.shared.dto.SysRoleMenu();
    d.setRoleId(e.getRoleId());
    d.setMenuId(e.getMenuId());
    return d;
  }
}
