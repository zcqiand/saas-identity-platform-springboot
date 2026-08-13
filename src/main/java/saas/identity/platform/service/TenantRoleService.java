package saas.identity.platform.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.RoleEntity;
import saas.identity.platform.entity.RolePermissionEntity;
import saas.identity.platform.mapper.RoleMapper;
import saas.identity.platform.repository.RolePermissionRepository;
import saas.identity.platform.repository.RoleRepository;
import saas.identity.shared.dto.CreateRoleRequest;
import saas.identity.shared.dto.Role;
import saas.identity.shared.dto.UpdateRoleRequest;

/**
 * M02.F01 + M02.F02 — 角色 CRUD + 权限矩阵。 v0.4.0：从 InMemoryStore 迁到 RoleRepository /
 * RolePermissionRepository。
 */
@Service
public class TenantRoleService {

  private final RoleRepository roleRepository;
  private final RolePermissionRepository rolePermissionRepository;

  public TenantRoleService(
      RoleRepository roleRepository, RolePermissionRepository rolePermissionRepository) {
    this.roleRepository = roleRepository;
    this.rolePermissionRepository = rolePermissionRepository;
  }

  @Transactional(readOnly = true)
  public Page<Role> list(UUID tenantId, int page, int pageSize) {
    return roleRepository
        .findByTenantId(tenantId, PageRequest.of(page, pageSize))
        .map(RoleMapper::toDto);
  }

  @Transactional
  public Role create(UUID tenantId, CreateRoleRequest body) {
    RoleEntity e = RoleMapper.fromCreateRequest(tenantId, body);
    return RoleMapper.toDto(roleRepository.save(e));
  }

  @Transactional(readOnly = true)
  public Role get(UUID tenantId, UUID roleId) {
    return RoleMapper.toDto(
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new NoSuchElementException("role not found")));
  }

  @Transactional
  public Role update(UUID tenantId, UUID roleId, UpdateRoleRequest body) {
    RoleEntity e =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new NoSuchElementException("role not found"));
    RoleMapper.applyUpdate(e, body);
    return RoleMapper.toDto(roleRepository.save(e));
  }

  @Transactional
  public void delete(UUID tenantId, UUID roleId) {
    roleRepository.findByTenantIdAndId(tenantId, roleId).ifPresent(roleRepository::delete);
  }

  @Transactional
  public Role setPermissions(UUID tenantId, UUID roleId, List<String> permissionIds) {
    RoleEntity role =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new NoSuchElementException("role not found"));
    // 整批替换 M:N
    rolePermissionRepository.deleteByRoleId(roleId);
    if (permissionIds != null) {
      for (String pidStr : permissionIds) {
        try {
          UUID pid = UUID.fromString(pidStr);
          RolePermissionEntity rp = new RolePermissionEntity();
          rp.setRoleId(roleId);
          rp.setPermissionId(pid);
          rolePermissionRepository.save(rp);
        } catch (IllegalArgumentException ignored) {
          // skip invalid UUIDs
        }
      }
    }
    return RoleMapper.toDto(role);
  }
}
