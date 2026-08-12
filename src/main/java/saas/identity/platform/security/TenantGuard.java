package saas.identity.platform.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Verifies that a tenant-scoped Controller method's path tenantId matches the JWT tenant_id claim.
 * MANDATORY call at the start of every tenant-scoped endpoint.
 *
 * <p>If you skip this guard, an attacker can pass any tenantId in the URL and read another tenant's
 * data — that's the whole point of this check.
 */
@Component
public class TenantGuard {
  private final TenantContext tenantContext;

  public TenantGuard(TenantContext tenantContext) {
    this.tenantContext = tenantContext;
  }

  public void verifyPathTenant(String pathTenantId) {
    String jwtTenantId = tenantContext.currentTenantId();
    if (pathTenantId == null || !pathTenantId.equals(jwtTenantId)) {
      throw new AccessDeniedException(
          "tenant mismatch: path=" + pathTenantId + " jwt=" + jwtTenantId);
    }
  }
}
