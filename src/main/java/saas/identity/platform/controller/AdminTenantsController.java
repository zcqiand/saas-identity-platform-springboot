package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.service.AdminTenantService;
import saas.identity.shared.api.AdminTenantsApi;
import saas.identity.shared.dto.AdminTenantsListTenants200Response;
import saas.identity.shared.dto.CreateTenantRequest;
import saas.identity.shared.dto.Tenant;
import saas.identity.shared.dto.UpdateTenantRequest;

/**
 * M00.F01 — 平台 admin 租户 CRUD。 平台级（非 tenant-scoped），不走 TenantGuard。 Controller 仅做 HTTP 适配 + 参数规整，业务在
 * {@link AdminTenantService}。
 */
@RestController
public class AdminTenantsController implements AdminTenantsApi {

  private static final int PAGE_DEFAULT = 0;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final AdminTenantService service;

  public AdminTenantsController(AdminTenantService service) {
    this.service = service;
  }

  private int normPage(Integer page) {
    return page == null ? PAGE_DEFAULT : Math.max(0, page);
  }

  private int normPageSize(Integer pageSize) {
    return pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
  }

  @Override
  public ResponseEntity<AdminTenantsListTenants200Response> adminTenantsListTenants(
      Integer page, Integer pageSize) {
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    var result = service.list(p, ps);
    var body =
        new AdminTenantsListTenants200Response()
            .items(result.getContent())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<Tenant> adminTenantsCreateTenant(CreateTenantRequest createTenantRequest) {
    return ResponseEntity.ok(service.create(createTenantRequest));
  }

  @Override
  public ResponseEntity<Tenant> adminTenantsGetTenant(String id) {
    return ResponseEntity.ok(service.get(UUID.fromString(id)));
  }

  @Override
  public ResponseEntity<Tenant> adminTenantsUpdateTenant(
      String id, UpdateTenantRequest updateTenantRequest) {
    return ResponseEntity.ok(service.update(UUID.fromString(id), updateTenantRequest));
  }

  @Override
  public ResponseEntity<Void> adminTenantsDeleteTenant(String id) {
    service.delete(UUID.fromString(id));
    return ResponseEntity.noContent().build();
  }
}
