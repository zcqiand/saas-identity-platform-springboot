package saas.identity.platform.mapper;

import java.util.List;
import java.util.UUID;
import saas.identity.platform.entity.RoleEntity;
import saas.identity.shared.dto.CreateRoleRequest;
import saas.identity.shared.dto.Role;
import saas.identity.shared.dto.UpdateRoleRequest;

public final class RoleMapper {

  private RoleMapper() {}

  public static Role toDto(RoleEntity e) {
    Role r = new Role();
    r.setId(e.getId());
    r.setTenantId(e.getTenantId());
    r.setCode(e.getCode());
    r.setName(e.getName());
    r.setDescription(e.getDescription());
    r.setCreatedAt(e.getCreatedAt());
    r.setUpdatedAt(e.getUpdatedAt());
    return r;
  }

  public static RoleEntity fromCreateRequest(UUID tenantId, CreateRoleRequest req) {
    RoleEntity e = new RoleEntity();
    e.setId(UUID.randomUUID());
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
    return entities.stream().map(RoleMapper::toDto).toList();
  }
}
