package saas.identity.platform.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
 *
 * <p>2026-09-12 I20/I38 四方并发超时+死锁修复（40P01 deadlock + StaleObjectState + 11.6s 单发）： 旧实现「派生
 * deleteByRoleId（先 SELECT 加载 34 行再逐行 DELETE）+ 逐行 save + 回读」对远端 PG （~195ms RTT）= 60+ 往返 11.6s；并发 4
 * PUT 同一 role 时「全量删+全量插+touch sys_role」互相持 junction 行锁再等对方 sys_role 行锁 → 40P01 死锁 / 3×500。对齐家族
 * oracle（nextjs route.ts notInArray 差量 delete + onConflictDoNothing insert）：
 *
 * <ol>
 *   <li>先 {@code SELECT ... FOR UPDATE} 锁 sys_role 行 —— 同 role 的并发写在此串行化， 消灭「junction 行锁 → sys_role
 *       行锁」反向等待环；
 *   <li>bulk JPQL 差量 {@code DELETE ... AND menu_id NOT IN (...)}（单语句，不动未变化的行）；
 *   <li>JdbcTemplate batch 批量 {@code INSERT ... ON CONFLICT DO NOTHING}（1 往返，幂等）；
 *   <li>响应从请求集合直接构造，不再回读。
 * </ol>
 *
 * <p>整条 PUT 现在固定 4 次往返（~0.8s @195ms RTT），并发展开后最慢一个 ~3s &lt; 契约测试 5s 预算。
 */
@RestController
@Transactional
public class TenantRoleMenusController implements TenantRoleMenusApi {

  private final SysRoleRepository roles;
  private final SysRoleMenuRepository roleMenus;
  private final TenantGuard tenantGuard;
  private final JdbcTemplate jdbc;

  @PersistenceContext private EntityManager em;

  public TenantRoleMenusController(
      SysRoleRepository roles,
      SysRoleMenuRepository roleMenus,
      TenantGuard tenantGuard,
      JdbcTemplate jdbc) {
    this.roles = roles;
    this.roleMenus = roleMenus;
    this.tenantGuard = tenantGuard;
    this.jdbc = jdbc;
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
    Set<UUID> requested = parseMenuIds(body);
    // 1. 行锁串行化同 role 并发写（不存在 → 404，与原 findRole 语义一致）
    UUID roleUuid = UUID.fromString(roleId);
    SysRole role = em.find(SysRole.class, roleUuid, LockModeType.PESSIMISTIC_WRITE);
    if (role == null) {
      throw new NoSuchElementException("role " + roleId);
    }
    // 2. 差量 delete —— 只清不在新集合里的 grant
    if (requested.isEmpty()) {
      roleMenus.deleteAllForRole(roleUuid);
    } else {
      roleMenus.deleteByRoleIdAndMenuIdNotIn(roleUuid, requested);
    }
    // 3. 批量 insert（ON CONFLICT DO NOTHING → 幂等，重跑不撞 23505）
    if (!requested.isEmpty()) {
      List<UUID> ordered = List.copyOf(requested);
      jdbc.batchUpdate(
          "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
          ordered,
          100,
          (ps, menuId) -> {
            ps.setObject(1, roleUuid);
            ps.setObject(2, menuId);
          });
    }
    // 4. touch — 聚合 updatedAt 来源（家族约定）；managed 实体脏检查 commit 时 flush
    role.setUpdatedAt(OffsetDateTime.now());
    return ResponseEntity.ok(toGrantFromRequested(role, requested));
  }

  @Override
  public ResponseEntity<Void> tenantRoleMenusClearSysRoleMenus(
      String tenantId, String roleId, String clientId) {
    tenantGuard.verifyPathTenant(tenantId);
    // bulk 单语句 delete（旧派生 deleteByRoleId 逐行删是 I38 60s 超时的主因）
    roleMenus.deleteAllForRole(UUID.fromString(roleId));
    return ResponseEntity.noContent().build();
  }

  private Set<UUID> parseMenuIds(SetSysRoleMenusRequest body) {
    if (body == null || body.getMenuIds() == null) {
      return Set.of();
    }
    LinkedHashSet<UUID> out = new LinkedHashSet<>();
    for (String menuId : body.getMenuIds()) {
      out.add(UUID.fromString(menuId));
    }
    return out;
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

  /**
   * PUT 响应构造 —— 差量 delete + ON CONFLICT insert 之后 DB 终态恰为请求集合（已去重）， 直接从请求集合构造省 2
   * 次回读往返（~400ms @195ms RTT）。
   */
  private RoleMenuGrant toGrantFromRequested(SysRole role, Set<UUID> requested) {
    RoleMenuGrant g = new RoleMenuGrant();
    g.setRoleId(role.getId());
    g.setTenantId(role.getTenantId());
    g.setMenuIds(requested.stream().map(UUID::toString).sorted().toList());
    g.setUpdatedAt(role.getUpdatedAt());
    return g;
  }
}
