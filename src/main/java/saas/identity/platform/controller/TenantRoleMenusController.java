package saas.identity.platform.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.SysRole;
import saas.identity.platform.repository.SysRoleMenuRepository;
import saas.identity.platform.repository.SysRoleRepository;
import saas.identity.platform.security.TenantGuard;
import saas.identity.shared.api.TenantRoleMenusApi;
import saas.identity.shared.dto.RoleMenuGrant;
import saas.identity.shared.dto.SetSysRoleMenusRequest;

/**
 * M00.F04 角色菜单授权（I02-I04）。
 *
 * <p>2026-09-10 I20 方案 C：GET/PUT 从 List&lt;SysRoleMenu&gt; 行集切 RoleMenuGrant 聚合返回 ({roleId,
 * tenantId, menuIds[], updatedAt})；PUT 时 touch sys_role.updated_at 作为聚合 updatedAt
 * 来源（家族约定）。SysRoleMenu DTO 已随 shared openapi 移除，junction 表读写保持 repo 直查。
 */
@RestController
@Transactional
public class TenantRoleMenusController implements TenantRoleMenusApi {

  private final SysRoleRepository roles;
  private final SysRoleMenuRepository roleMenus;
  private final TenantGuard tenantGuard;

  @PersistenceContext private EntityManager em;

  public TenantRoleMenusController(
      SysRoleRepository roles, SysRoleMenuRepository roleMenus, TenantGuard tenantGuard) {
    this.roles = roles;
    this.roleMenus = roleMenus;
    this.tenantGuard = tenantGuard;
  }

  @Override
  public ResponseEntity<RoleMenuGrant> tenantRoleMenusListSysRoleMenus(
      String tenantId, String roleId, String clientId) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(toGrant(findRole(roleId)));
  }

  @Override
  public ResponseEntity<RoleMenuGrant> tenantRoleMenusSetSysRoleMenus(
      String tenantId, String roleId, SetSysRoleMenusRequest body, String clientId) {
    tenantGuard.verifyPathTenant(tenantId);
    SysRole role = findRole(roleId);
    UUID roleUuid = role.getId();
    roleMenus.deleteByRoleId(roleUuid);
    // 9/10 修：sys_role_menu 派生 deleteByRoleId + 同事务内 save 撞 unique PK 23505
    // （Hibernate PersistenceContext 把 delete 行与新 save 行当同 entries，
    // 没 flush 时插入即撞唯一约束）。显式 flush + clear 强制 SQL 顺序执行。
    em.flush();
    em.clear();
    if (body != null && body.getMenuIds() != null) {
      for (String menuId : body.getMenuIds()) {
        saas.identity.platform.entity.Generated.SysRoleMenu r =
            new saas.identity.platform.entity.Generated.SysRoleMenu();
        r.setRoleId(roleUuid);
        r.setMenuId(UUID.fromString(menuId));
        roleMenus.save(r);
      }
    }
    role.setUpdatedAt(OffsetDateTime.now()); // touch — 聚合 updatedAt 来源（家族约定）
    roles.save(role);
    return ResponseEntity.ok(toGrant(roles.findById(roleUuid).orElse(role)));
  }

  @Override
  public ResponseEntity<Void> tenantRoleMenusClearSysRoleMenus(
      String tenantId, String roleId, String clientId) {
    tenantGuard.verifyPathTenant(tenantId);
    UUID roleUuid = UUID.fromString(roleId);
    roleMenus.deleteByRoleId(roleUuid);
    return ResponseEntity.noContent().build();
  }

  private SysRole findRole(String roleId) {
    UUID roleUuid = UUID.fromString(roleId);
    return roles.findById(roleUuid).orElseThrow(() -> new NoSuchElementException("role " + roleId));
  }

  private RoleMenuGrant toGrant(SysRole role) {
    RoleMenuGrant g = new RoleMenuGrant();
    g.setRoleId(role.getId());
    g.setTenantId(role.getTenantId());
    List<String> menuIds =
        roleMenus.findByRoleId(role.getId()).stream()
            .map(r -> r.getMenuId().toString())
            .sorted()
            .toList();
    g.setMenuIds(menuIds);
    g.setUpdatedAt(role.getUpdatedAt());
    return g;
  }
}
