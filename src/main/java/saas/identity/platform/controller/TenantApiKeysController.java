package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.security.TenantGuard;
import saas.identity.platform.service.TenantApiKeyService;
import saas.identity.shared.api.TenantApiKeysApi;
import saas.identity.shared.dto.ApiKey;
import saas.identity.shared.dto.CreateApiKeyRequest;
import saas.identity.shared.dto.CreateApiKeyResponse;
import saas.identity.shared.dto.TenantApiKeysListApiKeys200Response;

/**
 * M05.F01 — 租户内 API Key 生命周期（list / create / revoke / rotate）。 tenant-scoped：每个方法首调
 * tenantGuard.verifyPathTenant。业务在 {@link TenantApiKeyService}。
 */
@RestController
public class TenantApiKeysController implements TenantApiKeysApi {

  private static final int PAGE_DEFAULT = 0;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final TenantApiKeyService service;
  private final TenantGuard tenantGuard;

  public TenantApiKeysController(TenantApiKeyService service, TenantGuard tenantGuard) {
    this.service = service;
    this.tenantGuard = tenantGuard;
  }

  private int normPage(Integer page) {
    return page == null ? PAGE_DEFAULT : Math.max(0, page);
  }

  private int normPageSize(Integer pageSize) {
    return pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
  }

  @Override
  public ResponseEntity<TenantApiKeysListApiKeys200Response> tenantApiKeysListApiKeys(
      String tenantId, Integer page, Integer pageSize) {
    tenantGuard.verifyPathTenant(tenantId);
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    var result = service.list(UUID.fromString(tenantId), p, ps);
    var body =
        new TenantApiKeysListApiKeys200Response()
            .items(result.getContent())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<CreateApiKeyResponse> tenantApiKeysCreateApiKey(
      String tenantId, CreateApiKeyRequest createApiKeyRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.create(UUID.fromString(tenantId), createApiKeyRequest));
  }

  @Override
  public ResponseEntity<ApiKey> tenantApiKeysRevokeApiKey(String tenantId, String keyId) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.revoke(UUID.fromString(tenantId), UUID.fromString(keyId)));
  }

  @Override
  public ResponseEntity<CreateApiKeyResponse> tenantApiKeysRotateApiKey(
      String tenantId, String keyId) {
    tenantGuard.verifyPathTenant(tenantId);
    return ResponseEntity.ok(service.rotate(UUID.fromString(tenantId), UUID.fromString(keyId)));
  }

  // @entry M05.F01.I05
  @Override
  public ResponseEntity<Void> tenantApiKeysDeleteApiKey(String tenantId, String keyId) {
    tenantGuard.verifyPathTenant(tenantId);
    service.delete(UUID.fromString(tenantId), UUID.fromString(keyId));
    return ResponseEntity.noContent().build();
  }
}
