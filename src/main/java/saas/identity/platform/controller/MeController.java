package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.security.TenantContext;
import saas.identity.platform.service.MeService;
import saas.identity.shared.api.MeApi;
import saas.identity.shared.dto.CurrentUser;
import saas.identity.shared.dto.SwitchTenantResponse;
import saas.identity.shared.dto.TenantMembership;

/**
 * M00.F02 — 当前用户身份（whoami / 跨租户切换 / 我的租户）。 用户级（非 tenant-scoped 路径校验），不走 TenantGuard；当前 userId 从 JWT
 * sub claim 取（{@link TenantContext#currentUserId()}）。 业务在 {@link MeService}。
 */
@RestController
public class MeController implements MeApi {

  private final MeService service;
  private final TenantContext tenantContext;

  public MeController(MeService service, TenantContext tenantContext) {
    this.service = service;
    this.tenantContext = tenantContext;
  }

  private UUID currentUserId() {
    String sub = tenantContext.currentUserId();
    if (sub == null) {
      throw new org.springframework.security.access.AccessDeniedException("no JWT sub claim");
    }
    return UUID.fromString(sub);
  }

  @Override
  public ResponseEntity<CurrentUser> meWhoami() {
    return ResponseEntity.ok(service.whoami(currentUserId()));
  }

  @Override
  public ResponseEntity<java.util.List<TenantMembership>> meListMyTenants() {
    return ResponseEntity.ok(service.listMyTenants(currentUserId()));
  }

  @Override
  public ResponseEntity<SwitchTenantResponse> meSwitchTenant(String tenantId) {
    return ResponseEntity.ok(service.switchTenant(currentUserId(), UUID.fromString(tenantId)));
  }
}
