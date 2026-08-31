package saas.identity.platform.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.RoleMenuGrantEntity;
import saas.identity.platform.repository.RoleMenuGrantRepository;
import saas.identity.platform.repository.RoleRepository;
import saas.identity.shared.dto.RoleMenuGrant;

/**
 * M09.F01 + M09.F02 — 角色 ↔ 菜单 授权。 v0.4.0：从 InMemoryStore 迁到 RoleMenuGrantRepository。
 *
 * <p>2026-08-30：Phase 6 接 DTO — service 直接返回 RoleMenuGrant（不再 Map），配套新增 TenantRoleMenusController
 * 把端点暴露出去（contract-test M96.F02.I09 要求 GET /tenants/{t}/roles/{r}/menus 在 4 后端都返回 200 + 完整 4 字段）。
 */
@Service
public class TenantRoleMenuService {

  private final RoleMenuGrantRepository grantRepository;
  private final RoleRepository roleRepository;

  public TenantRoleMenuService(
      RoleMenuGrantRepository grantRepository, RoleRepository roleRepository) {
    this.grantRepository = grantRepository;
    this.roleRepository = roleRepository;
  }

  /** M09.F01.I01 — 查 role 的菜单授权（找不到行返回空 grant，roleId/tenantId 仍正确）。 */
  @Transactional(readOnly = true)
  public RoleMenuGrant get(UUID tenantId, UUID roleId) {
    if (roleRepository.findById(roleId).isEmpty()) {
      throw new NoSuchElementException("role not found");
    }
    var row = grantRepository.findByRoleId(roleId);
    if (row.isEmpty()) {
      return emptyGrant(tenantId, roleId);
    }
    return toDto(row.get());
  }

  /** M09.F02.I02 — 整批替换。 */
  @Transactional
  public RoleMenuGrant set(UUID tenantId, UUID roleId, List<String> menuIds) {
    if (roleRepository.findById(roleId).isEmpty()) {
      throw new NoSuchElementException("role not found");
    }
    RoleMenuGrantEntity e = grantRepository.findByRoleId(roleId).orElse(null);
    OffsetDateTime now = OffsetDateTime.now();
    List<UUID> parsedMenuIds =
        menuIds == null
            ? List.of()
            : menuIds.stream()
                .filter(
                    s -> {
                      try {
                        UUID.fromString(s);
                        return true;
                      } catch (IllegalArgumentException ex) {
                        return false;
                      }
                    })
                .map(UUID::fromString)
                .toList();
    if (e == null) {
      e = new RoleMenuGrantEntity();
      e.setRoleId(roleId);
    }
    e.setTenantId(tenantId);
    e.setMenuIds(parsedMenuIds);
    e.setUpdatedAt(now);
    var saved = grantRepository.save(e);
    return toDto(saved);
  }

  /** M09.F02.I03 — 清空。 */
  @Transactional
  public void clear(UUID roleId) {
    grantRepository.findByRoleId(roleId).ifPresent(grantRepository::delete);
  }

  private RoleMenuGrant toDto(RoleMenuGrantEntity e) {
    RoleMenuGrant dto = new RoleMenuGrant();
    dto.setRoleId(e.getRoleId());
    dto.setTenantId(e.getTenantId());
    dto.setMenuIds(
        e.getMenuIds() == null
            ? new ArrayList<>()
            : e.getMenuIds().stream().map(UUID::toString).toList());
    dto.setUpdatedAt(e.getUpdatedAt());
    return dto;
  }

  private RoleMenuGrant emptyGrant(UUID tenantId, UUID roleId) {
    RoleMenuGrant dto = new RoleMenuGrant();
    dto.setRoleId(roleId);
    dto.setTenantId(tenantId);
    dto.setMenuIds(new ArrayList<>());
    dto.setUpdatedAt(OffsetDateTime.now());
    return dto;
  }
}
