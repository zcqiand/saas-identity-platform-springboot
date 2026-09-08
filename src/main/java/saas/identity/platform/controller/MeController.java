package saas.identity.platform.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
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
 * me/menus 装配链路（M04.F04 §角色菜单授权 + 当前用户渲染）：
 *   1. tenant_member WHERE user_id = me → member 列表
 *   2. tenant_member_role WHERE member_id IN (...) → role ids
 *   3. sys_role_menu WHERE role_id IN (...) → menu ids
 *   4. sys_menu WHERE id IN (...) → 全部菜单
 *   5. flat → tree（parentId 链），group by clientId
 *
 * skeleton：userId 从 query param 拿；后续接入 JWT 后改 @AuthenticationPrincipal Jwt。
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
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<List<saas.identity.shared.dto.TenantMember>> meListMyTenants(String clientId) {
    return ResponseEntity.ok(List.of());
  }

  @Override
  public ResponseEntity<SwitchTenantResponse> meSwitchTenant(String tenantId, String clientId) {
    SwitchTenantResponse r = new SwitchTenantResponse();
    r.setTenantId(UUID.fromString(tenantId));
    return ResponseEntity.ok(r);
  }

  @Override
  public ResponseEntity<Map<String, List<EffectiveMenuNode>>> meGetMyMenus(String clientId) {
    // skeleton：当前未接 JWT，从 query 拿 userId（开发期方便测试）
    // 接入 JWT 后：UUID userId = UUID.fromString(jwt.getSubject());
    return ResponseEntity.ok(Map.of());
  }

  /**
   * M04.F04.I08 — 内部用 me/menus 装配（未来接 JWT 后直接走这个）。
   * 当前未挂 controller（skeleton 用 meGetMyMenus 返回 Map.of()）。
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
    Map<String, List<SysMenu>> byClient = flat.stream()
        .collect(Collectors.groupingBy(SysMenu::getClientId));

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
      n.setType(m.getType() == null ? null : SysMenuType.fromValue(String.valueOf(m.getType().intValue())));
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
    roots.sort((a, b) -> Integer.compare(
        a.getSortOrder() == null ? 0 : a.getSortOrder(),
        b.getSortOrder() == null ? 0 : b.getSortOrder()));
    return roots;
  }
}
