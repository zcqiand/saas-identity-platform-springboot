package saas.identity.platform.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.Tenant;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.shared.api.AdminTenantsApi;
import saas.identity.shared.dto.AdminTenantsListTenants200Response;
import saas.identity.shared.dto.CreateTenantRequest;
import saas.identity.shared.dto.UpdateTenantRequest;

/** M00.F01 平台 admin 租户 CRUD。skeleton。 */
@RestController
public class AdminTenantsController implements AdminTenantsApi {

  private final TenantRepository tenants;

  public AdminTenantsController(TenantRepository tenants) {
    this.tenants = tenants;
  }

  @Override
  public ResponseEntity<AdminTenantsListTenants200Response> adminTenantsListTenants(
      Integer page, Integer pageSize) {
    int p = page == null ? 0 : page;
    int ps = pageSize == null ? 20 : pageSize;
    Page<Tenant> pg = tenants.findAll(PageRequest.of(p, ps));
    AdminTenantsListTenants200Response resp = new AdminTenantsListTenants200Response();
    resp.setItems(toDtos(pg.getContent()));
    resp.setTotal(pg.getTotalElements());
    resp.setPage(p);
    resp.setPageSize(ps);
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.Tenant> adminTenantsCreateTenant(
      CreateTenantRequest body) {
    Tenant t = tenants.save(toEntity(body));
    return ResponseEntity.ok(toDto(t));
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.Tenant> adminTenantsGetTenant(String id) {
    UUID uuid = UUID.fromString(id);
    Tenant t = tenants.findById(uuid).orElseThrow(() -> new NoSuchElementException("tenant " + id));
    return ResponseEntity.ok(toDto(t));
  }

  @Override
  public ResponseEntity<saas.identity.shared.dto.Tenant> adminTenantsUpdateTenant(
      String id, UpdateTenantRequest body) {
    UUID uuid = UUID.fromString(id);
    Tenant t = tenants.findById(uuid).orElseThrow(() -> new NoSuchElementException("tenant " + id));
    if (body.getName() != null) t.setName(body.getName());
    return ResponseEntity.ok(toDto(tenants.save(t)));
  }

  @Override
  public ResponseEntity<Void> adminTenantsDeleteTenant(String id) {
    UUID uuid = UUID.fromString(id);
    tenants.deleteById(uuid);
    return ResponseEntity.noContent().build();
  }

  private Tenant toEntity(CreateTenantRequest b) {
    Tenant t = new Tenant();
    t.setTenantKey(b.getTenantKey());
    t.setName(b.getName());
    t.setStatus((short) 1);
    return t;
  }

  private saas.identity.shared.dto.Tenant toDto(Tenant e) {
    saas.identity.shared.dto.Tenant d = new saas.identity.shared.dto.Tenant();
    d.setId(e.getId());
    d.setTenantKey(e.getTenantKey());
    d.setName(e.getName());
    d.setStatus(e.getStatus() == null ? null : saas.identity.shared.dto.TenantStatus.ACTIVE);
    return d;
  }

  private List<saas.identity.shared.dto.Tenant> toDtos(List<Tenant> es) {
    return es.stream().map(this::toDto).toList();
  }
}
