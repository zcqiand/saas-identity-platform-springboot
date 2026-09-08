package saas.identity.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.entity.MenuEntity;
import saas.identity.platform.enums.AppStatus;
import saas.identity.platform.enums.MenuStatus;
import saas.identity.platform.enums.MenuType;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.MenuRepository;
import saas.identity.shared.dto.AdminAppMenusMoveMenuRequest;
import saas.identity.shared.dto.CreateMenuRequest;
import saas.identity.shared.dto.UpdateMenuRequest;

/**
 * M08 — 平台 admin 菜单 CRUD（应用下，NSwag codegen from shared tsp routes/admin-app-menus.tsp）。 业务 inline 在
 * controller（dev unblock；后续 Phase 抽 AdminAppMenusService）。这里直接 mock MenuRepository + AppRepository
 * 测 controller 行为。
 */
class AdminAppMenusControllerTest {

  private final MenuRepository menuRepository = mock(MenuRepository.class);
  private final AppRepository appRepository = mock(AppRepository.class);
  private final AdminAppMenusController controller =
      new AdminAppMenusController(menuRepository, appRepository);

  private AppEntity appEntity(UUID id, String code) {
    AppEntity e = new AppEntity();
    e.setId(id);
    e.setCode(code);
    e.setName(code);
    e.setStatus(AppStatus.ACTIVE);
    return e;
  }

  private MenuEntity menuEntity(UUID appId, UUID id, String code) {
    MenuEntity e = new MenuEntity();
    e.setId(id);
    e.setAppId(appId);
    e.setCode(code);
    e.setName(code);
    e.setType(MenuType.PAGE);
    e.setStatus(MenuStatus.ACTIVE);
    e.setSortOrder(0);
    e.setCreatedAt(OffsetDateTime.now());
    e.setUpdatedAt(OffsetDateTime.now());
    return e;
  }

  // ===== M08.F01.I01 — 列表 =====

  @Test
  @Fn({"M08.F01.I01"})
  void listMenus_filtersByAppId() {
    UUID appId = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    UUID otherAppId = UUID.randomUUID();
    UUID mid1 = UUID.randomUUID();
    UUID mid2 = UUID.randomUUID();
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findAll())
        .thenReturn(
            List.of(
                menuEntity(appId, mid1, "users"),
                menuEntity(otherAppId, UUID.randomUUID(), "other"),
                menuEntity(appId, mid2, "audit")));

    var resp = controller.adminAppMenusListMenus(appId.toString());

    assertEquals(200, resp.getStatusCode().value());
    assertEquals(2, resp.getBody().size());
    assertEquals(mid1, resp.getBody().get(0).getId());
    assertEquals(mid2, resp.getBody().get(1).getId());
  }

  // ===== M08.F01.I02 — 创建 =====

  @Test
  @Fn({"M08.F01.I02"})
  void createMenu_savesWithResolvedAppId() {
    UUID appId = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    // 模拟 @GeneratedValue(GenerationType.UUID)：save 后赋值 id
    when(menuRepository.save(any(MenuEntity.class)))
        .thenAnswer(
            inv -> {
              MenuEntity e = inv.getArgument(0);
              e.setId(UUID.randomUUID());
              return e;
            });

    CreateMenuRequest body =
        new CreateMenuRequest()
            .code("users")
            .name("Users")
            .type(saas.identity.shared.dto.MenuType.PAGE);
    var resp = controller.adminAppMenusCreateMenu(appId.toString(), body);

    assertEquals(200, resp.getStatusCode().value());
    assertNotNull(resp.getBody().getId());
    assertEquals(appId, resp.getBody().getAppId());
    assertEquals("users", resp.getBody().getCode());
    assertEquals(saas.identity.shared.dto.MenuStatus.ACTIVE, resp.getBody().getStatus());
  }

  // ===== M08.F01.I03 — 详情 =====

  @Test
  @Fn({"M08.F01.I03"})
  void getMenu_returnsFound() {
    UUID appId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findById(mid)).thenReturn(Optional.of(menuEntity(appId, mid, "users")));

    var resp = controller.adminAppMenusGetMenu(appId.toString(), mid.toString());

    assertEquals(200, resp.getStatusCode().value());
    assertEquals(mid, resp.getBody().getId());
  }

  @Test
  @Fn({"M08.F01.I03"})
  void getMenu_wrongApp_throwsNotFound() {
    UUID appId = UUID.randomUUID();
    UUID otherAppId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findById(mid))
        .thenReturn(Optional.of(menuEntity(otherAppId, mid, "users")));

    assertThrows(
        NoSuchElementException.class,
        () -> controller.adminAppMenusGetMenu(appId.toString(), mid.toString()));
  }

  // ===== M08.F01.I04 — 更新 =====

  @Test
  @Fn({"M08.F01.I04"})
  void updateMenu_appliesPatch() {
    UUID appId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    MenuEntity existing = menuEntity(appId, mid, "users");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findById(mid)).thenReturn(Optional.of(existing));
    when(menuRepository.save(any(MenuEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    UpdateMenuRequest body = new UpdateMenuRequest().name("Users (renamed)");
    var resp = controller.adminAppMenusUpdateMenu(appId.toString(), mid.toString(), body);

    assertEquals(200, resp.getStatusCode().value());
    assertEquals("Users (renamed)", resp.getBody().getName());
    verify(menuRepository).save(any(MenuEntity.class));
  }

  // ===== M08.F01.I05 — 删除 =====

  @Test
  @Fn({"M08.F01.I05"})
  void deleteMenu_returns204() {
    UUID appId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    MenuEntity existing = menuEntity(appId, mid, "users");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findById(mid)).thenReturn(Optional.of(existing));

    var resp = controller.adminAppMenusDeleteMenu(appId.toString(), mid.toString());

    assertEquals(204, resp.getStatusCode().value());
    verify(menuRepository).delete(existing);
  }

  @Test
  @Fn({"M08.F01.I05"})
  void deleteMenu_notFound_throws() {
    UUID appId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findById(mid)).thenReturn(Optional.empty());

    assertThrows(
        NoSuchElementException.class,
        () -> controller.adminAppMenusDeleteMenu(appId.toString(), mid.toString()));
    verify(menuRepository, never()).delete(any(MenuEntity.class));
  }

  // ===== M08.F02.I06 — 同级排序 =====

  @Test
  @Fn({"M08.F02.I06"})
  void reorderMenus_returnsUpdatedList() {
    UUID appId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findAll()).thenReturn(List.of(menuEntity(appId, mid, "users")));

    var resp =
        controller.adminAppMenusReorderMenus(
            appId.toString(), mid.toString(), new saas.identity.shared.dto.ReorderMenuRequest());

    assertEquals(200, resp.getStatusCode().value());
    assertEquals(1, resp.getBody().size());
  }

  // ===== M08.F02.I07 — 切换父级 =====

  @Test
  @Fn({"M08.F02.I07"})
  void moveMenu_updatesParentId() {
    UUID appId = UUID.randomUUID();
    UUID mid = UUID.randomUUID();
    UUID newParent = UUID.randomUUID();
    AppEntity app = appEntity(appId, "lab-mgmt");
    MenuEntity existing = menuEntity(appId, mid, "users");
    when(appRepository.findById(appId)).thenReturn(Optional.of(app));
    when(menuRepository.findById(mid)).thenReturn(Optional.of(existing));
    when(menuRepository.save(any(MenuEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    var body = new AdminAppMenusMoveMenuRequest().parentId(newParent.toString());
    var resp = controller.adminAppMenusMoveMenu(appId.toString(), mid.toString(), body);

    assertEquals(200, resp.getStatusCode().value());
    assertEquals(newParent, resp.getBody().getParentId());
  }
}
