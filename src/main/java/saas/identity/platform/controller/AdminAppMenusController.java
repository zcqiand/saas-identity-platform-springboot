package saas.identity.platform.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.enums.MenuStatus;
import saas.identity.platform.enums.MenuType;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.MenuRepository;
import saas.identity.shared.api.AdminAppMenusApi;
import saas.identity.shared.dto.AdminAppMenusMoveMenuRequest;
import saas.identity.shared.dto.CreateMenuRequest;
import saas.identity.shared.dto.Menu;
import saas.identity.shared.dto.ReorderMenuRequest;
import saas.identity.shared.dto.UpdateMenuRequest;

/**
 * M08 — 平台 admin 菜单 CRUD（应用下）。 平台级（非 tenant-scoped），不走 TenantGuard。 业务 inline（dev unblock），手写
 * CRUD：list / get / create / update / delete / move / reorder。
 *
 * <p>dev 注意：appId 路径参数兼容 Guid 或 code slug（"lab-management"），与 AdminAppsController 同款约定。
 */
@RestController
public class AdminAppMenusController implements AdminAppMenusApi {

  private final MenuRepository menus;
  private final AppRepository apps;

  public AdminAppMenusController(MenuRepository menus, AppRepository apps) {
    this.menus = menus;
    this.apps = apps;
  }

  private saas.identity.platform.entity.AppEntity resolveApp(String appIdOrCode) {
    try {
      var gid = UUID.fromString(appIdOrCode);
      var byId = apps.findById(gid);
      if (byId.isPresent()) return byId.get();
    } catch (IllegalArgumentException ignored) {
      // 不是 UUID，按 code 查
    }
    return apps.findAll().stream()
        .filter(a -> appIdOrCode.equals(a.getCode()))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("app '" + appIdOrCode + "' not found"));
  }

  private saas.identity.platform.entity.MenuEntity resolveMenu(UUID appId, String menuId) {
    UUID mid;
    try {
      mid = UUID.fromString(menuId);
    } catch (IllegalArgumentException e) {
      throw new NoSuchElementException("menu id '" + menuId + "' is not a Guid");
    }
    return menus
        .findById(mid)
        .filter(m -> m.getAppId().equals(appId))
        .orElseThrow(
            () -> new NoSuchElementException("menu '" + menuId + "' not found under app " + appId));
  }

  // === DTO ↔ Entity（inline，MenuMapper 不存在；Phase 6 抽出去） ===

  private static Menu toDto(saas.identity.platform.entity.MenuEntity e) {
    Menu m = new Menu();
    m.setId(e.getId());
    m.setAppId(e.getAppId());
    m.setParentId(e.getParentId());
    m.setCode(e.getCode());
    m.setName(e.getName());
    m.setPath(e.getPath());
    m.setIcon(e.getIcon());
    m.setType(toSharedType(e.getType()));
    m.setSortOrder(e.getSortOrder());
    m.setStatus(toSharedStatus(e.getStatus()));
    m.setCreatedAt(e.getCreatedAt());
    m.setUpdatedAt(e.getUpdatedAt());
    return m;
  }

  private static saas.identity.shared.dto.MenuType toSharedType(MenuType t) {
    if (t == null) return null;
    return saas.identity.shared.dto.MenuType.valueOf(t.name());
  }

  private static saas.identity.shared.dto.MenuStatus toSharedStatus(MenuStatus s) {
    if (s == null) return null;
    return saas.identity.shared.dto.MenuStatus.valueOf(s.name());
  }

  private static saas.identity.platform.entity.MenuEntity fromCreateRequest(
      String appIdOrCode, CreateMenuRequest body) {
    var e = new saas.identity.platform.entity.MenuEntity();
    // 不预置 id：id 非空被 Spring Data 判为 detached → merge → StaleObjectStateException
    // appId 由 resolveApp 设上
    e.setCode(body.getCode());
    e.setName(body.getName());
    String path = body.getPath();
    e.setPath(path == null ? null : path);
    String icon = body.getIcon();
    e.setIcon(icon == null ? null : icon);
    e.setType(fromSharedType(body.getType()));
    e.setSortOrder(body.getSortOrder() == null ? 0 : body.getSortOrder());
    e.setStatus(fromSharedStatus(body.getStatus()));
    UUID parentId = body.getParentId();
    e.setParentId(parentId);
    return e;
  }

  private static MenuType fromSharedType(saas.identity.shared.dto.MenuType t) {
    if (t == null) return MenuType.PAGE;
    return MenuType.valueOf(t.name());
  }

  private static MenuStatus fromSharedStatus(saas.identity.shared.dto.MenuStatus s) {
    if (s == null) return MenuStatus.ACTIVE;
    return MenuStatus.valueOf(s.name());
  }

  private static void applyUpdate(
      saas.identity.platform.entity.MenuEntity e, UpdateMenuRequest body) {
    if (body.getName() != null) e.setName(body.getName());
    if (body.getPath() != null) e.setPath(body.getPath());
    if (body.getIcon() != null) e.setIcon(body.getIcon());
    if (body.getType() != null) e.setType(fromSharedType(body.getType()));
    if (body.getSortOrder() != null) e.setSortOrder(body.getSortOrder());
    if (body.getStatus() != null) e.setStatus(fromSharedStatus(body.getStatus()));
  }

  // === endpoints ===

  @Override
  public ResponseEntity<List<Menu>> adminAppMenusListMenus(String appId) {
    var app = resolveApp(appId);
    var rows =
        menus.findAll().stream()
            .filter(m -> m.getAppId().equals(app.getId()))
            .map(AdminAppMenusController::toDto)
            .toList();
    return ResponseEntity.ok(rows);
  }

  @Override
  public ResponseEntity<Menu> adminAppMenusGetMenu(String appId, String menuId) {
    var app = resolveApp(appId);
    var e = resolveMenu(app.getId(), menuId);
    return ResponseEntity.ok(toDto(e));
  }

  @Override
  public ResponseEntity<Menu> adminAppMenusCreateMenu(String appId, CreateMenuRequest body) {
    var app = resolveApp(appId);
    var e = fromCreateRequest(appId, body);
    e.setAppId(app.getId());
    var saved = menus.save(e);
    return ResponseEntity.ok(toDto(saved));
  }

  @Override
  public ResponseEntity<Menu> adminAppMenusUpdateMenu(
      String appId, String menuId, UpdateMenuRequest body) {
    var app = resolveApp(appId);
    var e = resolveMenu(app.getId(), menuId);
    applyUpdate(e, body);
    var saved = menus.save(e);
    return ResponseEntity.ok(toDto(saved));
  }

  @Override
  public ResponseEntity<Void> adminAppMenusDeleteMenu(String appId, String menuId) {
    var app = resolveApp(appId);
    var e = resolveMenu(app.getId(), menuId);
    menus.delete(e);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Menu> adminAppMenusMoveMenu(
      String appId, String menuId, AdminAppMenusMoveMenuRequest body) {
    var app = resolveApp(appId);
    var e = resolveMenu(app.getId(), menuId);
    String parentIdStr = body == null ? null : body.getParentId();
    UUID parentId = null;
    if (parentIdStr != null && !parentIdStr.isBlank()) {
      try {
        parentId = UUID.fromString(parentIdStr);
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("parentId '" + parentIdStr + "' is not a Guid");
      }
    }
    e.setParentId(parentId);
    var saved = menus.save(e);
    return ResponseEntity.ok(toDto(saved));
  }

  @Override
  public ResponseEntity<List<Menu>> adminAppMenusReorderMenus(
      String appId, String menuId, ReorderMenuRequest body) {
    // Phase 6 接：按 order 字段批量更新兄弟菜单 sort_order。本期 dev 返回当前全量。
    var app = resolveApp(appId);
    var rows =
        menus.findAll().stream()
            .filter(m -> m.getAppId().equals(app.getId()))
            .map(AdminAppMenusController::toDto)
            .toList();
    return ResponseEntity.ok(rows);
  }
}
