package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import saas.identity.platform.entity.TenantEntity;
import saas.identity.platform.enums.TenantStatus;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.shared.dto.CreateTenantRequest;
import saas.identity.shared.dto.Tenant;
import saas.identity.shared.dto.UpdateTenantRequest;

/** AdminTenantService 单测（M00.F01 平台租户 CRUD）。Mockito mock TenantRepository。 */
class AdminTenantServiceTest {

  private final TenantRepository tenantRepository = mock(TenantRepository.class);
  private final AdminTenantService service = new AdminTenantService(tenantRepository);

  private TenantEntity entity(UUID id) {
    TenantEntity e = new TenantEntity();
    e.setId(id);
    e.setCode("ACME");
    e.setName("Acme");
    e.setStatus(TenantStatus.ACTIVE);
    return e;
  }

  @Test
  @Fn({"M00.F01.I01"})
  void list_returnsPage() {
    UUID id = UUID.randomUUID();
    Page<TenantEntity> page = new PageImpl<>(java.util.List.of(entity(id)));
    when(tenantRepository.findAll(any(Pageable.class))).thenReturn(page);

    Page<Tenant> result = service.list(0, 20);
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals("ACME", result.getContent().get(0).getCode());
  }

  @Test
  @Fn({"M00.F01.I02"})
  void create_returnsTenant() {
    when(tenantRepository.save(any(TenantEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    CreateTenantRequest body = new CreateTenantRequest().code("ACME").name("Acme");
    Tenant t = service.create(body);
    assertEquals("ACME", t.getCode());
    assertEquals("Acme", t.getName());
  }

  @Test
  @Fn({"M00.F01.I03"})
  void get_returnsTenant() {
    UUID id = UUID.randomUUID();
    when(tenantRepository.findById(id)).thenReturn(Optional.of(entity(id)));
    Tenant t = service.get(id);
    assertEquals(id, t.getId());
    assertEquals("Acme", t.getName());
  }

  @Test
  @Fn({"M00.F01.I03"})
  void get_throwsIfMissing() {
    UUID id = UUID.randomUUID();
    when(tenantRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(id));
  }

  @Test
  @Fn({"M00.F01.I04"})
  void update_appliesAndSaves() {
    UUID id = UUID.randomUUID();
    when(tenantRepository.findById(id)).thenReturn(Optional.of(entity(id)));
    when(tenantRepository.save(any(TenantEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    UpdateTenantRequest body = new UpdateTenantRequest().name("Acme II");
    Tenant t = service.update(id, body);
    assertEquals("Acme II", t.getName());
  }

  @Test
  @Fn({"M00.F01.I05"})
  void delete_removesWhenPresent() {
    UUID id = UUID.randomUUID();
    TenantEntity e = entity(id);
    when(tenantRepository.findById(id)).thenReturn(Optional.of(e));
    service.delete(id);
    verify(tenantRepository).delete(e);
  }
}
