package saas.identity.platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Holds the current tenant_id from the authenticated JWT. Used by TenantGuard to verify
 * path-carried tenantId matches JWT claim.
 */
@Component
public class TenantContext {
  public String currentTenantId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return null;
    return jwt.getClaimAsString("tenant_id");
  }

  public String currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) return null;
    return jwt.getSubject();
  }
}
