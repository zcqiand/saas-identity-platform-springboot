package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import saas.identity.platform.entity.RoleEntity;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.RolePermissionRepository;
import saas.identity.platform.repository.RoleRepository;
import saas.identity.shared.dto.CreateRoleRequest;
import saas.identity.shared.dto.Role;
import saas.identity.shared.dto.UpdateRoleRequest;

/** TenantRoleService 单测（M02.F01 角色 CRUD + M02.F02 权限矩阵）。 */
class TenantRoleServiceTest {

  private final RoleRepository roleRepository = mock(RoleRepository.class);
  private final RolePermissionRepository rolePermissionRepository =
      mock(RolePermissionRepository.class);
  private final TenantRoleService service =
      new TenantRoleService(roleRepository, rolePermissionRepository);

  private RoleEntity entity(UUID tenantId, UUID roleId) {
    RoleEntity e = new RoleEntity();
    e.setId(roleId);
    e.setTenantId(tenantId);
    e.setCode("ADMIN");
    e.setName("Admin");
    return e;
  }

  @Test
  @Fn({"M02.F01.I01"})
  void list_returnsPage() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    Page<RoleEntity> page = new PageImpl<>(List.of(entity(tid, rid)));
    when(roleRepository.findByTenantId(eq(tid), any(Pageable.class))).thenReturn(page);

    Page<Role> result = service.list(tid, 0, 20);
    assertEquals(1, result.getContent().size());
    assertEquals("ADMIN", result.getContent().get(0).getCode());
  }

  @Test
  @Fn({"M02.F01.I02"})
  void create_returnsRole() {
    UUID tid = UUID.randomUUID();
    when(roleRepository.save(any(RoleEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    CreateRoleRequest body = new CreateRoleRequest().code("ADMIN").name("Admin");
    Role r = service.create(tid, body);
    assertEquals("ADMIN", r.getCode());
    assertEquals(tid, r.getTenantId());
  }

  @Test
  @Fn({"M02.F01.I03"})
  void get_returnsRole() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.of(entity(tid, rid)));
    Role r = service.get(tid, rid);
    assertEquals(rid, r.getId());
  }

  @Test
  @Fn({"M02.F01.I03"})
  void get_throwsIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.empty());
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(tid, rid));
  }

  @Test
  @Fn({"M02.F01.I04"})
  void update_appliesAndSaves() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.of(entity(tid, rid)));
    when(roleRepository.save(any(RoleEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    UpdateRoleRequest body = new UpdateRoleRequest().name("Admin II");
    Role r = service.update(tid, rid, body);
    assertEquals("Admin II", r.getName());
  }

  @Test
  @Fn({"M02.F01.I05"})
  void delete_removesWhenPresent() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    RoleEntity e = entity(tid, rid);
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.of(e));
    service.delete(tid, rid);
    verify(roleRepository).delete(e);
  }

  @Test
  @Fn({"M02.F01.I05"})
  void delete_noopWhenMissing() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.empty());
    service.delete(tid, rid);
    verify(roleRepository, never()).delete(any(RoleEntity.class));
  }

  @Test
  @Fn({"M02.F02.I01"})
  void setPermissions_replacesBatch() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    UUID pid = UUID.randomUUID();
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.of(entity(tid, rid)));
    Role r = service.setPermissions(tid, rid, List.of(pid.toString()));
    assertEquals(rid, r.getId());
    verify(rolePermissionRepository).deleteByRoleId(rid);
    verify(rolePermissionRepository).save(any());
  }

  @Test
  @Fn({"M02.F02.I01"})
  void setPermissions_skipsInvalidUuid() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    when(roleRepository.findByTenantIdAndId(tid, rid)).thenReturn(Optional.of(entity(tid, rid)));
    service.setPermissions(tid, rid, List.of("not-a-uuid"));
    verify(rolePermissionRepository).deleteByRoleId(rid);
    verify(rolePermissionRepository, never()).save(any());
  }
}
