package saas.identity.platform.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.SysMenu;
import saas.identity.platform.entity.Generated.SysRoleMenu;
import saas.identity.platform.entity.Generated.TenantMember;
import saas.identity.platform.entity.Generated.TenantMemberRole;
import saas.identity.platform.repository.SysMenuRepository;
import saas.identity.platform.repository.SysRoleMenuRepository;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.platform.repository.TenantMemberRoleRepository;
import saas.identity.shared.api.MeApi;
import saas.identity.shared.dto.CurrentUser;
import saas.identity.shared.dto.EffectiveMenuNode;
import saas.identity.shared.dto.SwitchTenantResponse;
import saas.identity.shared.dto.SysMenuType;

/**
 * M01.F01/F03 当前用户视图 + M04.F04.I08 me/menus 装配。
 *
 * <p>me/menus 装配链路（M04.F04 §角色菜单授权 + 当前用户渲染）： 1. tenant_member WHERE user_id = me → member 列表 2.
 * tenant_member_role WHERE member_id IN (...) → role ids 3. sys_role_menu WHERE role_id IN (...) →
 * menu ids 4. sys_menu WHERE id IN (...) → 全部菜单 5. flat → tree（parentId 链），group by clientId
 *
 * <p>skeleton：userId 从 query param 拿；后续接入 JWT 后改 @AuthenticationPrincipal Jwt。
 */
@RestController
public class MeController implements MeApi {

  private final TenantMemberRepository members;
  private final TenantMemberRoleRepository memberRoles;
  private final SysRoleMenuRepository roleMenus;
  private final SysMenuRepository menus;

  public MeController(
      TenantMemberRepository members,
      TenantMemberRoleRepository memberRoles,
      SysRoleMenuRepository roleMenus,
      SysMenuRepository menus) {
    this.members = members;
    this.memberRoles = memberRoles;
    this.roleMenus = roleMenus;
    this.menus = menus;
  }

  @Override
  public ResponseEntity<CurrentUser> meWhoami() {
    CurrentUser u = new CurrentUser();
    UUID userId = currentUserId();
    if (userId != null) {
      u.setCurrentTenantId(userId); // 占位：tenantId 后续接 meListMyTenants 拿真值
    }
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<List<saas.identity.shared.dto.TenantMember>> meListMyTenants(
      String clientId) {
    return ResponseEntity.ok(List.of());
  }

  @Override
  public ResponseEntity<SwitchTenantResponse> meSwitchTenant(String tenantId, String clientId) {
    SwitchTenantResponse r = new SwitchTenantResponse();
    r.setTenantId(UUID.fromString(tenantId));
    return ResponseEntity.ok(r);
  }

  /**
   * M04.F04.I08 — 当前用户有效菜单装配。
   *
   * <p>userId 提取路径（M04.F04 §2.3 + ADR-0020 路线 A）：
   *
   * <ul>
   *   <li>生产：JwtAuthenticationToken.getToken().getSubject() === userId（UUID string）
   *   <li>测试：@WithMockUser 时 principal 是 User，getName() 返回 username（仍可作为 lookup key）
   *   <li>无认证：返回 Map.of()
   * </ul>
   */
  @Override
  public ResponseEntity<Map<String, List<EffectiveMenuNode>>> meGetMyMenus(String clientId) {
    UUID userId = currentUserId();
    if (userId == null) {
      return ResponseEntity.ok(Map.of());
    }
    return ResponseEntity.ok(assembleMenus(userId));
  }

  /**
   * 从 SecurityContextHolder 提取当前用户 ID。
   *
   * <p>支持三种 principal：
   *
   * <ol>
   *   <li>Jwt（生产 + smoke test）：subject = userId
   *   <li>User / UsernamePasswordAuthenticationToken（@WithMockUser 测试）：name = username
   *   <li>无认证（permitAll 路径意外走到）：null
   * </ol>
   *
   * 返回 null 表示无认证上下文，由调用方决定回退策略。
   */
  private UUID currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      return null;
    }
    Object principal = auth.getPrincipal();
    // 生产：Jwt principal
    if (principal instanceof Jwt jwt) {
      try {
        return UUID.fromString(jwt.getSubject());
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
    // 测试：User principal — name 可能是 username 或 "user"
    String name = auth.getName();
    if (name == null) {
      return null;
    }
    try {
      return UUID.fromString(name);
    } catch (IllegalArgumentException e) {
      // 测试场景：username 不是 UUID 格式；assembleMenus 找不到 member 时返回 Map.of()
      return null;
    }
  }

  /**
   * M04.F04.I08 — 内部用 me/menus 装配（未来接 JWT 后直接走这个）。 当前未挂 controller（skeleton 用 meGetMyMenus 返回
   * Map.of()）。
   */
  public Map<String, List<EffectiveMenuNode>> assembleMenus(UUID userId) {
    // 1. tenant_member
    List<TenantMember> myMemberships = members.findByUserId(userId);
    if (myMemberships.isEmpty()) {
      return Map.of();
    }

    // 2. member → roles
    List<UUID> memberIds = myMemberships.stream().map(TenantMember::getId).toList();
    List<TenantMemberRole> bindings = memberRoles.findByMemberIds(memberIds);
    if (bindings.isEmpty()) {
      return Map.of();
    }
    List<UUID> roleIds = bindings.stream().map(TenantMemberRole::getRoleId).toList();

    // 3. role → menus
    List<SysRoleMenu> grants = roleMenus.findByRoleIds(roleIds);
    if (grants.isEmpty()) {
      return Map.of();
    }
    List<UUID> menuIds = grants.stream().map(SysRoleMenu::getMenuId).toList();

    // 4. menus → flat list
    List<SysMenu> flat = menus.findAllById(menuIds);

    // 5. group by clientId, build tree per client
    Map<String, List<SysMenu>> byClient =
        flat.stream().collect(Collectors.groupingBy(SysMenu::getClientId));

    Map<String, List<EffectiveMenuNode>> result = new HashMap<>();
    for (var entry : byClient.entrySet()) {
      result.put(entry.getKey(), buildTree(entry.getValue()));
    }
    return result;
  }

  private List<EffectiveMenuNode> buildTree(List<SysMenu> flat) {
    Map<UUID, EffectiveMenuNode> byId = new HashMap<>();
    List<EffectiveMenuNode> roots = new ArrayList<>();

    // 1. flat → DTO
    for (SysMenu m : flat) {
      EffectiveMenuNode n = new EffectiveMenuNode();
      n.setId(m.getId());
      n.setClientId(m.getClientId());
      n.setParentId(m.getParentId());
      n.setTitle(m.getTitle());
      n.setType(
          m.getType() == null
              ? null
              : SysMenuType.fromValue(String.valueOf(m.getType().intValue())));
      n.setPath(m.getPath());
      n.setComponent(m.getComponent());
      n.setPerms(m.getPerms());
      n.setIcon(m.getIcon());
      n.setSortOrder(m.getSortOrder());
      n.setChildren(new ArrayList<>());
      byId.put(m.getId(), n);
    }

    // 2. tree 装配：parentId == ROOT_MENU_ID 或 null 的为 root
    for (EffectiveMenuNode n : byId.values()) {
      if (n.getParentId() == null
          || "00000000-0000-0000-0000-000000000000".equals(n.getParentId().toString())) {
        roots.add(n);
      } else {
        EffectiveMenuNode parent = byId.get(n.getParentId());
        if (parent != null) {
          parent.getChildren().add(n);
        } else {
          // 孤儿（parent 不在 grants 内）→ 当 root
          roots.add(n);
        }
      }
    }

    // 3. sort by sortOrder
    roots.sort(
        (a, b) ->
            Integer.compare(
                a.getSortOrder() == null ? 0 : a.getSortOrder(),
                b.getSortOrder() == null ? 0 : b.getSortOrder()));
    return roots;
  }
}
