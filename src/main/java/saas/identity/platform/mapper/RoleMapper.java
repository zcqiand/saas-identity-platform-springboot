package saas.identity.platform.mapper;

import java.util.List;
import java.util.UUID;
import saas.identity.platform.entity.RoleEntity;
import saas.identity.shared.dto.CreateRoleRequest;
import saas.identity.shared.dto.Role;
import saas.identity.shared.dto.UpdateRoleRequest;

public final class RoleMapper {

  private RoleMapper() {}

  public static Role toDto(RoleEntity e, List<String> permissionIds) {
    Role r = new Role();
    r.setId(e.getId());
    r.setTenantId(e.getTenantId());
    r.setCode(e.getCode());
    r.setName(e.getName());
    // 2026-08-30 contract-test I07/I08: 不返 description(msw/nextjs 不返, 字节对齐)
    r.setPermissionIds(permissionIds == null ? List.of() : permissionIds);
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  public static RoleEntity fromCreateRequest(UUID tenantId, CreateRoleRequest req) {
    RoleEntity e = new RoleEntity();
    // 不预置 id：id 非空被 Spring Data 判为 detached → merge → StaleObjectStateException
    e.setTenantId(tenantId);
    e.setCode(req.getCode());
    e.setName(req.getName());
    e.setDescription(req.getDescription());
    return e;
  }

  public static void applyUpdate(RoleEntity e, UpdateRoleRequest req) {
    if (req.getName() != null) e.setName(req.getName());
    if (req.getDescription() != null) e.setDescription(req.getDescription());
  }

  public static List<Role> toDtoList(List<RoleEntity> entities) {
    return entities.stream().map(e -> toDto(e, List.of())).toList();
  }
}
