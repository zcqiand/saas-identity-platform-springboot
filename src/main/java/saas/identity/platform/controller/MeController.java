package saas.identity.platform.controller;

// @impl M04.F04.I08 — book anchor (xr-know-007)

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
  private final saas.identity.platform.repository.TenantRepository tenants;
  private final saas.identity.platform.repository.SysUserRepository users;
  private final MemberViewAssembler assembler;
  private final saas.identity.platform.security.JwtIssuer jwt;

  public MeController(
      TenantMemberRepository members,
      TenantMemberRoleRepository memberRoles,
      SysRoleMenuRepository roleMenus,
      SysMenuRepository menus,
      saas.identity.platform.repository.TenantRepository tenants,
      saas.identity.platform.repository.SysUserRepository users,
      MemberViewAssembler assembler,
      saas.identity.platform.security.JwtIssuer jwt) {
    this.members = members;
    this.memberRoles = memberRoles;
    this.roleMenus = roleMenus;
    this.menus = menus;
    this.tenants = tenants;
    this.users = users;
    this.assembler = assembler;
    this.jwt = jwt;
  }

  /**
   * ADR-0032：/me 返回扁平 CurrentUser（id/email/memberships[]/currentTenantId?），对齐 msw
   * oracle。memberships = tenant_member(user 匹配) → TenantMembership；currentTenantId 优先取 JWT
   * tenant_id claim，缺省落首个 membership 的 tenant。
   */
  @Override
  public ResponseEntity<CurrentUser> meWhoami() {
    UUID userId = currentUserId();
    if (userId == null) {
      // 项 5.36：防御分支保留（Security 前置挡匿名的不可达性未实证），但序列化形状
      // 必须守契约 —— memberships 是 required，null 会违反 CurrentUser 契约形状。
      return ResponseEntity.ok(new CurrentUser().memberships(List.of()));
    }
    CurrentUser u = new CurrentUser();
    u.setId(userId);
    users.findById(userId).ifPresent(usr -> u.setEmail(usr.getEmail()));
    List<saas.identity.shared.dto.TenantMembership> memberships = membershipsOf(userId);
    u.setMemberships(memberships);
    u.setCurrentTenantId(currentTenantId(userId, memberships));
    return ResponseEntity.ok(u);
  }

  /** ADR-0032：/me/tenants 返回 TenantMembership[]（不再是 List.of() / TenantMember）。 */
  @Override
  public ResponseEntity<List<saas.identity.shared.dto.TenantMembership>> meListMyTenants(
      String clientId) {
    UUID userId = currentUserId();
    if (userId == null) {
      return ResponseEntity.ok(List.of());
    }
    return ResponseEntity.ok(membershipsOf(userId));
  }

  /**
   * M01.F03 切换当前租户 —— 签真 HS256 token（对齐 aspnetcore MeController.Switch）。
   *
   * <p>链路：tenant 存在性（不存在 404）→ tenant_member 该 user 有 active 行（无 → 404）→
   * issueAccessToken(sub=user_id, tenant_id) 填 SwitchTenantResponse。refreshToken 用 {@link
   * saas.identity.platform.security.JwtIssuer#generateRefreshToken}（对齐 aspnetcore： switch 不持久化
   * refresh 行，rotate 语义归 /auth/refresh）。
   */
  @Override
  public ResponseEntity<SwitchTenantResponse> meSwitchTenant(String tenantId, String clientId) {
    UUID userId = currentUserId();
    if (userId == null) {
      throw new saas.identity.platform.security.InvalidCredentialsException(
          "Bearer sub required for tenant switch");
    }
    UUID tenantUuid = UUID.fromString(tenantId);
    tenants
        .findById(tenantUuid)
        .orElseThrow(() -> new NoSuchElementException("tenant " + tenantId));
    // S5 修复（2026-09-12 四方一致）：switch 门槛从「仅 active(1)」放宽为「非 disabled(0)」
    // ——对齐 msw oracle（status !== "removed"）与 aspnetcore（Status != 0）多数派口径；
    // invited/suspended 成员可切换，被移除（0）不可。
    boolean activeMember =
        members.findByUserId(userId).stream()
            .anyMatch(
                m ->
                    tenantUuid.equals(m.getTenantId())
                        && m.getStatus() != null
                        && m.getStatus() != 0);
    if (!activeMember) {
      throw new NoSuchElementException(
          "user " + userId + " is not an active member of tenant " + tenantId);
    }
    SwitchTenantResponse r = new SwitchTenantResponse();
    r.setAccessToken(jwt.issueAccessToken(userId, tenantUuid));
    r.setRefreshToken(saas.identity.platform.security.JwtIssuer.generateRefreshToken(userId));
    r.setExpiresAt(java.time.OffsetDateTime.now().plusSeconds(jwt.getTtlSeconds()));
    r.setTenantId(tenantUuid);
    // ADR-0032：SwitchTenantResponse 删 clientId 键（契约收敛，切租户后前端经 /me 拿上下文）。
    return ResponseEntity.ok(r);
  }

  /** 当前用户的全部 membership（ADR-0032 扁平形态，含 roleIds 真 join / joinedAt）。 */
  private List<saas.identity.shared.dto.TenantMembership> membershipsOf(UUID userId) {
    return members.findByUserId(userId).stream().map(assembler::toMembership).toList();
  }

  /** currentTenantId：JWT tenant_id claim 优先；无 claim（测试 principal）落首个 membership 的 tenant。 */
  private UUID currentTenantId(
      UUID userId, List<saas.identity.shared.dto.TenantMembership> memberships) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt token) {
      Object claim = token.getClaims().get("tenant_id");
      if (claim instanceof String s) {
        try {
          return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
          // fall through to membership default
        }
      } else if (claim instanceof UUID uuid) {
        return uuid;
      }
    }
    return memberships.isEmpty() ? null : memberships.get(0).getTenantId();
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

    // 1. flat → DTO（根节点 parentId 零值 sentinel → null，对齐 msw/nextjs/aspnetcore 实测：
    // EffectiveMenuNode.parentId 序列化为 null 而非 00000000-... 零值 UUID）
    for (SysMenu m : flat) {
      EffectiveMenuNode n = new EffectiveMenuNode();
      n.setId(m.getId());
      n.setClientId(m.getClientId());
      n.setParentId(isZeroUuid(m.getParentId()) ? null : m.getParentId());
      n.setTitle(m.getTitle());
      n.setType(TypeMapper.fromShort(m.getType()));
      n.setPath(m.getPath());
      n.setComponent(m.getComponent());
      n.setPerms(m.getPerms());
      n.setIcon(m.getIcon());
      n.setSortOrder(m.getSortOrder());
      n.setChildren(new ArrayList<>());
      byId.put(m.getId(), n);
    }

    // 2. tree 装配：parentId 为 null（含已归零的根 sentinel）的为 root
    for (EffectiveMenuNode n : byId.values()) {
      if (n.getParentId() == null) {
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

  /** sys_menu 根节点的 parent_id sentinel（DB NOT NULL 列存的零值 UUID）。 */
  private static boolean isZeroUuid(UUID id) {
    return id != null && "00000000-0000-0000-0000-000000000000".equals(id.toString());
  }
}
