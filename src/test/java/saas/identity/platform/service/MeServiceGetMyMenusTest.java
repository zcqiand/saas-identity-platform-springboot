package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.MenuRepository;
import saas.identity.platform.repository.RoleMenuGrantRepository;
import saas.identity.platform.repository.TenantMembershipRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.EffectiveMenuNode;

/**
 * M09.F03.I04 — 当前用户有效菜单（按 app 分组）。
 *
 * <p>2026-08-28 prod 503 事故修复：/me/menus 此前是 stub（Map.of()）。真实现 = membership.roleIds →
 * role_menu_grants.menuIds → menus 树 + 父链容器保留。 roleIds/menuIds 都是 @Transient 数组列，走 repository
 * native query 取，不经实体。
 */
class MeServiceGetMyMenusTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final TenantMembershipRepository membershipRepository =
      mock(TenantMembershipRepository.class);
  private final RoleMenuGrantRepository grantRepository = mock(RoleMenuGrantRepository.class);
  private final MenuRepository menuRepository = mock(MenuRepository.class);
  private final AppRepository appRepository = mock(AppRepository.class);
  private final JwtIssuer jwt =
      new JwtIssuer("unit-test-signing-key-0123456789abcdef0123", "ut-issuer", "ut-aud", 3600L);

  private MeService service() {
    return new MeService(
        userRepository,
        membershipRepository,
        grantRepository,
        menuRepository,
        appRepository,
        jwt,
        null);
  }

  private AppEntity app(UUID appId, String code) {
    AppEntity a = new AppEntity();
    a.setId(appId);
    a.setCode(code);
    a.setName(code);
    a.setClientId(code);
    return a;
  }

  @Test
  @Fn({"M09.F03.I04"})
  void getMyMenus_returnsTreeGroupedByAppCode() {
    UUID userId = UUID.randomUUID();
    UUID appId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID roleId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID pageId = UUID.randomUUID();

    when(membershipRepository.findRoleIdsByUserId(userId)).thenReturn(List.of(roleId));
    when(grantRepository.findMenuIdsByRoleIds(List.of(roleId))).thenReturn(List.of(pageId));
    when(menuRepository.findAllById(any()))
        .thenReturn(
            List.of(
                group(groupId, appId, null, "m-overview", 1),
                page(pageId, appId, groupId, "m-dashboard", 1)));
    when(appRepository.findById(appId)).thenReturn(Optional.of(app(appId, "lab-management")));

    Map<String, List<EffectiveMenuNode>> result = service().getMyMenus(userId);

    assertEquals(1, result.size());
    List<EffectiveMenuNode> labTree = result.get("lab-management");
    assertEquals(1, labTree.size());
    assertEquals("m-overview", labTree.get(0).getCode());
    assertEquals(1, labTree.get(0).getChildren().size());
    assertEquals("m-dashboard", labTree.get(0).getChildren().get(0).getCode());
  }

  @Test
  @Fn({"M09.F03.I04"})
  void getMyMenus_noRoles_returnsEmptyMap() {
    UUID userId = UUID.randomUUID();
    when(membershipRepository.findRoleIdsByUserId(userId)).thenReturn(List.of());
    Map<String, List<EffectiveMenuNode>> result = service().getMyMenus(userId);
    assertTrue(result.isEmpty());
  }

  @Test
  @Fn({"M09.F03.I04"})
  void getMyMenus_groupNotGranted_keptAsContainerIfChildGranted() {
    // msw mock 语义镜像: group 节点不在 grant 中也保留作容器（父链补全）
    UUID userId = UUID.randomUUID();
    UUID appId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID roleId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    UUID pageId = UUID.randomUUID();

    when(membershipRepository.findRoleIdsByUserId(userId)).thenReturn(List.of(roleId));
    // grant 只有 page，group 不在 grant 里
    when(grantRepository.findMenuIdsByRoleIds(List.of(roleId))).thenReturn(List.of(pageId));
    when(menuRepository.findAllById(any()))
        .thenReturn(
            List.of(
                group(groupId, appId, null, "m-basedata", 2),
                page(pageId, appId, groupId, "m-models", 1)));
    when(appRepository.findById(appId)).thenReturn(Optional.of(app(appId, "lab-management")));

    Map<String, List<EffectiveMenuNode>> result = service().getMyMenus(userId);
    List<EffectiveMenuNode> tree = result.get("lab-management");
    assertEquals(1, tree.size());
    assertEquals("m-basedata", tree.get(0).getCode());
    assertEquals("m-models", tree.get(0).getChildren().get(0).getCode());
  }

  // === fixtures ===

  private saas.identity.platform.entity.MenuEntity group(
      UUID id, UUID appId, UUID parentId, String code, int sort) {
    return node(
        id, appId, parentId, code, code, null, saas.identity.platform.enums.MenuType.GROUP, sort);
  }

  private saas.identity.platform.entity.MenuEntity page(
      UUID id, UUID appId, UUID parentId, String code, int sort) {
    return node(
        id,
        appId,
        parentId,
        code,
        code,
        "some/path",
        saas.identity.platform.enums.MenuType.PAGE,
        sort);
  }

  private saas.identity.platform.entity.MenuEntity node(
      UUID id,
      UUID appId,
      UUID parentId,
      String code,
      String name,
      String path,
      saas.identity.platform.enums.MenuType type,
      int sort) {
    saas.identity.platform.entity.MenuEntity e = new saas.identity.platform.entity.MenuEntity();
    e.setId(id);
    e.setAppId(appId);
    e.setParentId(parentId);
    e.setCode(code);
    e.setName(name);
    e.setPath(path);
    e.setType(type);
    e.setSortOrder(sort);
    e.setStatus(saas.identity.platform.enums.MenuStatus.ACTIVE);
    return e;
  }
}
