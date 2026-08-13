package saas.identity.platform.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.RoleMenuGrantEntity;
import saas.identity.platform.repository.RoleMenuGrantRepository;
import saas.identity.platform.repository.RoleRepository;

/**
 * M09.F01 + M09.F02 — 角色 ↔ 菜单 授权。 v0.4.0：从 InMemoryStore 迁到 RoleMenuGrantRepository。
 *
 * <p>备注：当前 NSwag 生成的 DTO 没有 RoleMenuGrant（admin-app-menus / tenant-role-menus route 需在 shared 端补
 * OpenAPI 后重新 gen-shared）。本 service 先建好 Repository 引用， 控制器层 Phase 6 集成 DTO 时再补上方法签名。
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

  /** M09.F01.I01 — 当前返回 Map（Phase 6 接 DTO 时换为 RoleMenuGrant） */
  @Transactional(readOnly = true)
  public java.util.Map<String, Object> get(UUID roleId) {
    if (roleRepository.findById(roleId).isEmpty()) {
      throw new NoSuchElementException("role not found");
    }
    var row = grantRepository.findByRoleId(roleId);
    if (row.isEmpty()) {
      return java.util.Map.of(
          "roleId", roleId,
          "menuIds", List.of(),
          "updatedAt", OffsetDateTime.now().toString());
    }
    return toMap(row.get());
  }

  /** M09.F02.I02 — 整批替换 */
  @Transactional
  public java.util.Map<String, Object> set(UUID roleId, List<String> menuIds) {
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
    UUID tenantId = roleRepository.findById(roleId).orElseThrow().getTenantId();
    e.setTenantId(tenantId);
    e.setMenuIds(parsedMenuIds);
    e.setUpdatedAt(now);
    return toMap(grantRepository.save(e));
  }

  /** M09.F02.I03 — 清空 */
  @Transactional
  public void clear(UUID roleId) {
    grantRepository.findByRoleId(roleId).ifPresent(grantRepository::delete);
  }

  private java.util.Map<String, Object> toMap(RoleMenuGrantEntity e) {
    return java.util.Map.of(
        "roleId", e.getRoleId().toString(),
        "menuIds",
            e.getMenuIds() == null
                ? List.of()
                : e.getMenuIds().stream().map(UUID::toString).toList(),
        "updatedAt", e.getUpdatedAt().toString());
  }
}
