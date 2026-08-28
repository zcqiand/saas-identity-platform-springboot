package saas.identity.platform.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.entity.MenuEntity;
import saas.identity.platform.entity.TenantMembershipEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.MenuRepository;
import saas.identity.platform.repository.RoleMenuGrantRepository;
import saas.identity.platform.repository.TenantMembershipRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.CurrentUser;
import saas.identity.shared.dto.EffectiveMenuNode;
import saas.identity.shared.dto.MembershipStatus;
import saas.identity.shared.dto.SwitchTenantResponse;
import saas.identity.shared.dto.TenantMembership;

/**
 * M00.F02 — 当前用户身份（whoami / 跨租户切换 / 我的租户）。 v0.4.0：从 InMemoryStore 迁到真实 DB。 switchTenant 走 JwtIssuer
 * HS256 真签（2026-08-28 与 AuthService 同步迁移；对称 saas-aspnetcore MeController v0.2.0）。
 *
 * <p>M09.F03.I02/I03/I04 — getMyMenus 真实现：从 membership.roleIds 收集 → role_menu_grants.menuIds →
 * menus 表 → 父链补全 → 按 app.code 分组输出 Map<appCode, List<EffectiveMenuNode>>。
 */
@Service
public class MeService {

  private final UserRepository userRepository;
  private final TenantMembershipRepository membershipRepository;
  private final RoleMenuGrantRepository grantRepository;
  private final MenuRepository menuRepository;
  private final AppRepository appRepository;
  private final JwtIssuer jwtIssuer;
  private final JdbcTemplate jdbc;

  public MeService(
      UserRepository userRepository,
      TenantMembershipRepository membershipRepository,
      RoleMenuGrantRepository grantRepository,
      MenuRepository menuRepository,
      AppRepository appRepository,
      JwtIssuer jwtIssuer,
      JdbcTemplate jdbc) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.grantRepository = grantRepository;
    this.menuRepository = menuRepository;
    this.appRepository = appRepository;
    this.jwtIssuer = jwtIssuer;
    this.jdbc = jdbc;
  }

  @Transactional(readOnly = true)
  public CurrentUser whoami(UUID userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found"));
    List<TenantMembershipEntity> memberships = membershipRepository.findByUserId(userId);
    List<TenantMembership> dtos =
        memberships.stream()
            .filter(m -> m.getStatus() != saas.identity.platform.enums.MembershipStatus.REMOVED)
            .map(this::toMembershipDto)
            .toList();
    UUID currentTenantId =
        memberships.stream()
            .filter(m -> m.getStatus() != saas.identity.platform.enums.MembershipStatus.REMOVED)
            .map(TenantMembershipEntity::getTenantId)
            .findFirst()
            .orElse(user.getTenantId());
    return new CurrentUser()
        .id(user.getId())
        .email(user.getEmail())
        .displayName(user.getDisplayName())
        .memberships(dtos)
        .currentTenantId(currentTenantId);
  }

  @Transactional(readOnly = true)
  public List<TenantMembership> listMyTenants(UUID userId) {
    return membershipRepository.findByUserId(userId).stream()
        .filter(m -> m.getStatus() != saas.identity.platform.enums.MembershipStatus.REMOVED)
        .map(this::toMembershipDto)
        .toList();
  }

  @Transactional
  public SwitchTenantResponse switchTenant(UUID userId, UUID tenantId) {
    TenantMembershipEntity m =
        membershipRepository
            .findByUserIdAndTenantId(userId, tenantId)
            .orElseThrow(() -> new SecurityException("not a member of this tenant"));
    if (m.getStatus() == saas.identity.platform.enums.MembershipStatus.REMOVED) {
      throw new SecurityException("not a member of this tenant");
    }
    return new SwitchTenantResponse()
        .accessToken(jwtIssuer.issueAccessToken(userId, tenantId))
        .refreshToken(JwtIssuer.generateRefreshToken(userId))
        .expiresAt(java.time.OffsetDateTime.now().plusHours(1))
        .tenantId(tenantId);
  }

  /**
   * M09.F03.I02/I03/I04 — 当前用户有效菜单（按 app.code 分组）。
   *
   * <p>链路：userId → membership.roleIds (DISTINCT unnest) → role_menu_grants.menuIds (DISTINCT
   * unnest) → menus 表 + 父链补全 → 按 appId 分桶 → 映射 app.code → 输出 Map<appCode, List<EffectiveMenuNode>>。
   */
  @Transactional(readOnly = true)
  public Map<String, List<EffectiveMenuNode>> getMyMenus(UUID userId) {
    // 用 JdbcTemplate 直连（OauthService 同款 — unnest() 经 Spring Data @Query 映射 List<UUID>
    // 线上 500，复用 OauthService 已验证模式）。jdbc 为 null 时回退 Spring Data 仓库（单测场景）。
    List<UUID> roleIds;
    List<UUID> grantedMenuIds;
    if (jdbc != null) {
      roleIds =
          jdbc.queryForList(
              "SELECT DISTINCT unnest(role_ids) FROM tenant_memberships WHERE user_id = ?",
              UUID.class,
              userId);
      if (roleIds.isEmpty()) {
        return Map.of();
      }
      grantedMenuIds =
          jdbc.query(
              "SELECT DISTINCT unnest(menu_ids) FROM role_menu_grants WHERE role_id = ANY(?)",
              ps -> ps.setArray(1, ps.getConnection().createArrayOf("uuid", roleIds.toArray())),
              (rs, rowNum) -> (UUID) rs.getObject(1));
    } else {
      roleIds = membershipRepository.findRoleIdsByUserId(userId);
      if (roleIds.isEmpty()) {
        return Map.of();
      }
      grantedMenuIds = grantRepository.findMenuIdsByRoleIds(roleIds);
    }
    if (grantedMenuIds.isEmpty()) {
      return Map.of();
    }
    // 父链补全：granted 的每个 menu 的祖先链一并加载（深度受 menu 树硬约束）
    Set<UUID> allMenuIds = new LinkedHashSet<>(grantedMenuIds);
    List<MenuEntity> granted = menuRepository.findAllById(new ArrayList<>(allMenuIds));
    boolean changed = true;
    while (changed) {
      Set<UUID> missing = new HashSet<>();
      for (MenuEntity m : granted) {
        UUID parent = m.getParentId();
        if (parent != null && !allMenuIds.contains(parent)) {
          missing.add(parent);
        }
      }
      if (missing.isEmpty()) break;
      allMenuIds.addAll(missing);
      granted = menuRepository.findAllById(new ArrayList<>(allMenuIds));
      changed = true;
    }
    // 按 appId 分桶
    Map<UUID, List<MenuEntity>> byApp = new HashMap<>();
    for (MenuEntity m : granted) {
      byApp.computeIfAbsent(m.getAppId(), k -> new ArrayList<>()).add(m);
    }
    // 装配树（按菜单节点父子）
    Map<UUID, EffectiveMenuNode> nodeIndex = new HashMap<>();
    for (MenuEntity m : granted) {
      nodeIndex.put(m.getId(), toDto(m));
    }
    for (MenuEntity m : granted) {
      EffectiveMenuNode self = nodeIndex.get(m.getId());
      UUID parentId = m.getParentId();
      if (parentId != null && nodeIndex.containsKey(parentId)) {
        nodeIndex.get(parentId).addChildrenItem(self);
      }
    }
    // 按 app 输出（key = app.code）
    Map<String, List<EffectiveMenuNode>> result = new HashMap<>();
    for (Map.Entry<UUID, List<MenuEntity>> e : byApp.entrySet()) {
      UUID appId = e.getKey();
      String code = appRepository.findById(appId).map(AppEntity::getCode).orElse(appId.toString());
      Set<UUID> appMenuIds = new HashSet<>();
      for (MenuEntity m : e.getValue()) appMenuIds.add(m.getId());
      List<EffectiveMenuNode> appRoots = new ArrayList<>();
      for (MenuEntity m : e.getValue()) {
        UUID parentId = m.getParentId();
        if (parentId == null || !appMenuIds.contains(parentId)) {
          appRoots.add(nodeIndex.get(m.getId()));
        }
      }
      appRoots.sort(byCode());
      appRoots.forEach(r -> r.getChildren().sort(byCode()));
      result.put(code, appRoots);
    }
    return result;
  }

  private static Comparator<EffectiveMenuNode> byCode() {
    return Comparator.comparing(
            (EffectiveMenuNode n) -> n.getSortOrder(),
            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
        .thenComparing(
            (EffectiveMenuNode n) -> n.getCode(),
            java.util.Comparator.nullsLast(String::compareTo));
  }

  private static EffectiveMenuNode toDto(MenuEntity m) {
    EffectiveMenuNode n = new EffectiveMenuNode();
    n.setId(m.getId());
    n.setAppId(m.getAppId());
    n.setParentId(m.getParentId());
    n.setCode(m.getCode());
    n.setName(m.getName());
    n.setPath(m.getPath());
    n.setIcon(m.getIcon());
    n.setType(toDtoType(m.getType()));
    n.setSortOrder(m.getSortOrder());
    n.setChildren(new ArrayList<>());
    return n;
  }

  private static saas.identity.shared.dto.MenuType toDtoType(
      saas.identity.platform.enums.MenuType t) {
    if (t == null) return null;
    return saas.identity.shared.dto.MenuType.fromValue(t.toDbValue());
  }

  private TenantMembership toMembershipDto(TenantMembershipEntity m) {
    return new TenantMembership()
        .id(m.getId())
        .userId(m.getUserId())
        .tenantId(m.getTenantId())
        .roleIds(
            m.getRoleIds() == null
                ? List.of()
                : m.getRoleIds().stream().map(UUID::toString).toList())
        .status(toDtoMembership(m.getStatus()))
        .joinedAt(m.getJoinedAt());
  }

  private saas.identity.shared.dto.MembershipStatus toDtoMembership(
      saas.identity.platform.enums.MembershipStatus s) {
    if (s == null) return MembershipStatus.ACTIVE;
    return MembershipStatus.valueOf(s.name());
  }
}
