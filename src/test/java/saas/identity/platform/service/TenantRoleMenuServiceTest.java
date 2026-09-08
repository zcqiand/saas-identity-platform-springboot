package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import saas.identity.platform.entity.RoleEntity;
import saas.identity.platform.entity.RoleMenuGrantEntity;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.RoleMenuGrantRepository;
import saas.identity.platform.repository.RoleRepository;
import saas.identity.shared.dto.RoleMenuGrant;

/**
 * M09.F01 + M09.F02 — 角色 ↔ 菜单 授权。
 *
 * <p>NSwag codegen from shared tsp routes/tenant-role-menus.tsp（dev unblock；admin/UI 入口在
 * contract-test 仓的 M96.F02.I20 走四方比对）。这里直接 mock 两个 repository 测 service 行为。
 */
class TenantRoleMenuServiceTest {

  private final RoleMenuGrantRepository grantRepository = mock(RoleMenuGrantRepository.class);
  private final RoleRepository roleRepository = mock(RoleRepository.class);
  private final TenantRoleMenuService service =
      new TenantRoleMenuService(grantRepository, roleRepository);

  private RoleEntity roleEntity(UUID tenantId, UUID roleId) {
    RoleEntity e = new RoleEntity();
    e.setId(roleId);
    e.setTenantId(tenantId);
    e.setCode("admin");
    e.setName("Admin");
    return e;
  }

  private RoleMenuGrantEntity grantEntity(UUID tenantId, UUID roleId, List<UUID> menuIds) {
    RoleMenuGrantEntity e = new RoleMenuGrantEntity();
    e.setRoleId(roleId);
    e.setTenantId(tenantId);
    e.setMenuIds(menuIds);
    e.setUpdatedAt(OffsetDateTime.now());
    return e;
  }

  // ===== M09.F01.I01 — 查角色已授权菜单 =====

  @Test
  @Fn({"M09.F01.I01"})
  void get_returnsGrant() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    UUID m1 = UUID.randomUUID();
    UUID m2 = UUID.randomUUID();
    when(roleRepository.findById(rid)).thenReturn(Optional.of(roleEntity(tid, rid)));
    when(grantRepository.findByRoleId(rid))
        .thenReturn(Optional.of(grantEntity(tid, rid, List.of(m1, m2))));

    RoleMenuGrant g = service.get(tid, rid);

    assertEquals(rid, g.getRoleId());
    assertEquals(tid, g.getTenantId());
    // DTO menuIds 是 List<String>（UUID 字符串形式）
    assertIterableEquals(List.of(m1.toString(), m2.toString()), g.getMenuIds());
  }

  @Test
  @Fn({"M09.F01.I01"})
  void get_roleNotFound_throws() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    when(roleRepository.findById(rid)).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> service.get(tid, rid));
  }

  // ===== M09.F02.I01 — 整批设置 =====

  @Test
  @Fn({"M09.F02.I01"})
  void set_replacesMenuIds() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    UUID old1 = UUID.randomUUID();
    UUID new1 = UUID.randomUUID();
    UUID new2 = UUID.randomUUID();
    when(roleRepository.findById(rid)).thenReturn(Optional.of(roleEntity(tid, rid)));
    when(grantRepository.findByRoleId(rid))
        .thenReturn(Optional.of(grantEntity(tid, rid, List.of(old1))));
    when(grantRepository.save(any(RoleMenuGrantEntity.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // service.set 接受 List<String>（UUID 字符串），内部 parse 成 List<UUID>；DTO 输出 List<String>
    RoleMenuGrant g = service.set(tid, rid, List.of(new1.toString(), new2.toString()));

    assertIterableEquals(List.of(new1.toString(), new2.toString()), g.getMenuIds());
    verify(grantRepository).save(any(RoleMenuGrantEntity.class));
  }

  // ===== M09.F02.I03 — 清空 =====

  @Test
  @Fn({"M09.F02.I03"})
  void clear_deletesGrant() {
    UUID tid = UUID.randomUUID();
    UUID rid = UUID.randomUUID();
    UUID m1 = UUID.randomUUID();
    when(roleRepository.findById(rid)).thenReturn(Optional.of(roleEntity(tid, rid)));
    when(grantRepository.findByRoleId(rid))
        .thenReturn(Optional.of(grantEntity(tid, rid, List.of(m1))));

    service.clear(rid);

    verify(grantRepository).delete(any(RoleMenuGrantEntity.class));
  }

  @Test
  @Fn({"M09.F02.I03"})
  void clear_noExistingGrant_doesNothing() {
    UUID rid = UUID.randomUUID();
    when(roleRepository.findById(rid)).thenReturn(Optional.empty());
    when(grantRepository.findByRoleId(rid)).thenReturn(Optional.empty());

    service.clear(rid);

    verify(grantRepository, org.mockito.Mockito.never()).delete(any(RoleMenuGrantEntity.class));
  }
}
