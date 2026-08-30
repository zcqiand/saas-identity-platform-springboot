package saas.identity.platform.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
 *
 * <p>2026-08-30：list/get/update/setPermissions 都补 PermissionIds（contract-test
 * M96.F02.I07/I08 必填；join RolePermission → permissions.id）。
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

  /** 单 role 的 permissionIds（UUID string 列表）。 */
  private List<String> permissionIdsForRole(UUID roleId) {
    return rolePermissionRepository.findByRoleId(roleId).stream()
        .map(rp -> rp.getPermissionId().toString())
        .collect(Collectors.toList());
  }

  /** 批量：roleId → permissionIds（避免 list() 的 N+1）。 */
  private Map<UUID, List<String>> permissionIdsByRole(Collection<UUID> roleIds) {
    if (roleIds.isEmpty()) return Map.of();
    Map<UUID, List<String>> out = new HashMap<>();
    for (RolePermissionEntity rp : rolePermissionRepository.findByRoleIdIn(roleIds)) {
      out.computeIfAbsent(rp.getRoleId(), k -> new java.util.ArrayList<>())
          .add(rp.getPermissionId().toString());
    }
    return out;
  }

  @Transactional(readOnly = true)
  public Page<Role> list(UUID tenantId, int page, int pageSize) {
    var roles =
        roleRepository.findByTenantId(tenantId, PageRequest.of(page, pageSize)).getContent();
    var perms = permissionIdsByRole(roles.stream().map(RoleEntity::getId).toList());
    var dtos =
        roles.stream()
            .map(r -> RoleMapper.toDto(r, perms.getOrDefault(r.getId(), List.of())))
            .toList();
    long total = roleRepository.findByTenantId(tenantId, PageRequest.of(0, 1)).getTotalElements();
    return new PageImpl<>(dtos, PageRequest.of(page, pageSize), total);
  }

  @Transactional
  public Role create(UUID tenantId, CreateRoleRequest body) {
    RoleEntity e = RoleMapper.fromCreateRequest(tenantId, body);
    return RoleMapper.toDto(roleRepository.save(e), List.of());
  }

  @Transactional(readOnly = true)
  public Role get(UUID tenantId, UUID roleId) {
    var e =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new NoSuchElementException("role not found"));
    return RoleMapper.toDto(e, permissionIdsForRole(roleId));
  }

  @Transactional
  public Role update(UUID tenantId, UUID roleId, UpdateRoleRequest body) {
    RoleEntity e =
        roleRepository
            .findByTenantIdAndId(tenantId, roleId)
            .orElseThrow(() -> new NoSuchElementException("role not found"));
    RoleMapper.applyUpdate(e, body);
    var saved = roleRepository.save(e);
    return RoleMapper.toDto(saved, permissionIdsForRole(roleId));
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
    List<String> savedIds = new java.util.ArrayList<>();
    if (permissionIds != null) {
      for (String pidStr : permissionIds) {
        try {
          UUID pid = UUID.fromString(pidStr);
          RolePermissionEntity rp = new RolePermissionEntity();
          rp.setRoleId(roleId);
          rp.setPermissionId(pid);
          rolePermissionRepository.save(rp);
          savedIds.add(pid.toString());
        } catch (IllegalArgumentException ignored) {
          // skip invalid UUIDs
        }
      }
    }
    return RoleMapper.toDto(role, savedIds);
  }
}