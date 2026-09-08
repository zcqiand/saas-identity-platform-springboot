package saas.identity.platform.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.SysMenu;
import saas.identity.platform.repository.SysMenuRepository;
import saas.identity.shared.api.ClientMenusApi;
import saas.identity.shared.dto.ClientMenusMoveSysMenuRequest;
import saas.identity.shared.dto.CreateSysMenuRequest;
import saas.identity.shared.dto.ReorderSysMenuRequest;
import saas.identity.shared.dto.SysMenuType;
import saas.identity.shared.dto.UpdateSysMenuRequest;

/** M04.F04 菜单 CRUD + 结构（I01-I07）。skeleton。 */
@RestController
public class ClientMenusController implements ClientMenusApi {

  private final SysMenuRepository menus;

  public ClientMenusController(SysMenuRepository menus) {
    this.menus = menus;
  }

  @Override
  public ResponseEntity<List<saas.identity.shared.dto.SysMenu>> clientMenusListSysMenus(String clientId) {
    List<saas.identity.shared.dto.SysMenu> dtos = menus.findByClientId(clientId).stream().map(this::toDto).toList();
    return ResponseEntity.ok(dtos);
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysMenu> clientMenusCreateSysMenu(String clientId, CreateSysMenuRequest body) {
    SysMenu e = new SysMenu();
    e.setId(UUID.randomUUID());
    e.setClientId(clientId);
    e.setParentId(body.getParentId() == null ? new UUID(0, 0) : body.getParentId());
    e.setTitle(body.getTitle());
    e.setType(body.getType() == null ? (short) 1 : typeToShort(body.getType()));
    e.setPath(body.getPath());
    e.setComponent(body.getComponent());
    e.setPerms(body.getPerms());
    e.setIcon(body.getIcon());
    e.setSortOrder(body.getSortOrder() == null ? 0 : body.getSortOrder());
    e.setStatus((short) 1);
    return ResponseEntity.ok(toDto(menus.save(e)));
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysMenu> clientMenusGetSysMenu(String clientId, String menuId) {
    UUID menuUuid = UUID.fromString(menuId);
    SysMenu e = menus.findById(menuUuid).orElseThrow(() -> new NoSuchElementException("menu " + menuId));
    return ResponseEntity.ok(toDto(e));
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysMenu> clientMenusUpdateSysMenu(String clientId, String menuId, UpdateSysMenuRequest body) {
    UUID menuUuid = UUID.fromString(menuId);
    SysMenu e = menus.findById(menuUuid).orElseThrow(() -> new NoSuchElementException("menu " + menuId));
    if (body.getTitle() != null) e.setTitle(body.getTitle());
    if (body.getPath() != null) e.setPath(body.getPath());
    if (body.getComponent() != null) e.setComponent(body.getComponent());
    if (body.getPerms() != null) e.setPerms(body.getPerms());
    if (body.getIcon() != null) e.setIcon(body.getIcon());
    if (body.getSortOrder() != null) e.setSortOrder(body.getSortOrder());
    return ResponseEntity.ok(toDto(menus.save(e)));
  }

  @Override
  public ResponseEntity<Void> clientMenusDeleteSysMenu(String clientId, String menuId) {
    UUID menuUuid = UUID.fromString(menuId);
    menus.deleteById(menuUuid);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<saas.identity.shared.dto.SysMenu>> clientMenusReorderSysMenus(String clientId, String menuId, ReorderSysMenuRequest body) {
    UUID menuUuid = UUID.fromString(menuId);
    SysMenu e = menus.findById(menuUuid).orElseThrow(() -> new NoSuchElementException("menu " + menuId));
    if (body.getOrderedMenuIds() != null) {
      int idx = body.getOrderedMenuIds().indexOf(menuId);
      if (idx >= 0) e.setSortOrder(idx);
    }
    menus.save(e);
    return ResponseEntity.ok(menus.findByClientId(clientId).stream().map(this::toDto).toList());
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.SysMenu> clientMenusMoveSysMenu(String clientId, String menuId, ClientMenusMoveSysMenuRequest body) {
    UUID menuUuid = UUID.fromString(menuId);
    SysMenu e = menus.findById(menuUuid).orElseThrow(() -> new NoSuchElementException("menu " + menuId));
    if (body.getParentId() != null) e.setParentId(UUID.fromString(body.getParentId()));
    return ResponseEntity.ok(toDto(menus.save(e)));
  }

  private short typeToShort(SysMenuType t) {
    return switch (t) {
      case DIRECTORY -> (short) 1;
      case MENU -> (short) 2;
      case BUTTON -> (short) 3;
    };
  }

  private SysMenuType shortToType(Short s) {
    if (s == null) return null;
    int v = s.intValue();
    if (v == 1) return SysMenuType.DIRECTORY;
    if (v == 2) return SysMenuType.MENU;
    if (v == 3) return SysMenuType.BUTTON;
    return SysMenuType.MENU;
  }

  private saas.identity.shared.dto.SysMenu toDto(SysMenu e) {
    saas.identity.shared.dto.SysMenu d = new saas.identity.shared.dto.SysMenu();
    d.setId(e.getId());
    d.setClientId(e.getClientId());
    d.setParentId(e.getParentId());
    d.setTitle(e.getTitle());
    d.setType(e.getType() == null ? null : shortToType(e.getType()));
    d.setPath(e.getPath());
    d.setComponent(e.getComponent());
    d.setPerms(e.getPerms());
    d.setIcon(e.getIcon());
    d.setSortOrder(e.getSortOrder());
    d.setStatus(e.getStatus() == null ? null : e.getStatus().intValue());
    d.setCreatedAt(e.getCreatedAt());
    return d;
  }
}
