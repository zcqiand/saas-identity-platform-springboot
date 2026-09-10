package saas.identity.platform.controller;

import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.repository.TenantApplicationRepository;
import saas.identity.shared.api.TenantApplicationsApi;
import saas.identity.shared.dto.SubscribeTenantApplicationRequest;
import saas.identity.shared.dto.TenantApplication;
import saas.identity.shared.dto.TenantApplicationsListTenantApplications200Response;
import saas.identity.shared.dto.UpdateTenantApplicationRequest;

/** M00.F05 租户应用订阅。skeleton。 */
@RestController
@Transactional
public class TenantApplicationsController implements TenantApplicationsApi {

  private final TenantApplicationRepository apps;

  public TenantApplicationsController(TenantApplicationRepository apps) {
    this.apps = apps;
  }

  @Override
  public ResponseEntity<TenantApplicationsListTenantApplications200Response>
      tenantApplicationsListTenantApplications(String tenantId, Integer page, Integer pageSize) {
    int p = page == null ? 0 : page;
    int ps = pageSize == null ? 20 : pageSize;
    UUID tenantUuid = UUID.fromString(tenantId);
    var pg = apps.findByTenantId(tenantUuid, PageRequest.of(p, ps));
    TenantApplicationsListTenantApplications200Response resp =
        new TenantApplicationsListTenantApplications200Response();
    resp.setItems(pg.getContent().stream().map(this::toDto).toList());
    resp.setTotal(pg.getTotalElements());
    resp.setPage(p);
    resp.setPageSize(ps);
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<TenantApplication> tenantApplicationsSubscribeTenantApplication(
      String tenantId, SubscribeTenantApplicationRequest body) {
    java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
    saas.identity.platform.entity.Generated.TenantApplication e =
        new saas.identity.platform.entity.Generated.TenantApplication();
    e.setTenantId(UUID.fromString(tenantId));
    e.setClientId(body.getClientId());
    e.setStatus((short) 1);
    e.setCreatedAt(now); // createdAt 列 NOT NULL（schema-first DB-First）
    return ResponseEntity.ok(toDto(apps.save(e)));
  }

  @Override
  public ResponseEntity<TenantApplication> tenantApplicationsUpdateTenantApplication(
      String tenantId, String clientId, UpdateTenantApplicationRequest body) {
    UUID tenantUuid = UUID.fromString(tenantId);
    saas.identity.platform.entity.Generated.TenantApplication e =
        apps.findByTenantIdAndClientId(tenantUuid, clientId)
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "tenant_application tenant=" + tenantId + " client=" + clientId));
    if (body.getStatus() != null) e.setStatus(body.getStatus().shortValue());
    if (body.getExpireTime() != null) e.setExpireTime(body.getExpireTime());
    return ResponseEntity.ok(toDto(apps.save(e)));
  }

  @Override
  public ResponseEntity<Void> tenantApplicationsRemoveTenantApplication(
      String tenantId, String clientId) {
    UUID tenantUuid = UUID.fromString(tenantId);
    apps.findByTenantIdAndClientId(tenantUuid, clientId).ifPresent(a -> apps.deleteById(a.getId()));
    return ResponseEntity.noContent().build();
  }

  private TenantApplication toDto(saas.identity.platform.entity.Generated.TenantApplication e) {
    TenantApplication d = new TenantApplication();
    d.setId(e.getId());
    d.setTenantId(e.getTenantId());
    d.setClientId(e.getClientId());
    d.setStatus(e.getStatus() == null ? null : e.getStatus().intValue());
    d.setExpireTime(e.getExpireTime());
    return d;
  }
}
