package saas.identity.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.service.AuthService;
import saas.identity.shared.api.AuthApi;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;
import saas.identity.shared.dto.OidcCallbackRequest;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * M03.F01/F02/F03 — 认证（密码登录 / OIDC / 登出 / refresh）。 全局（非 tenant-scoped），不走 TenantGuard。 业务在 {@link
 * AuthService}。
 */
@RestController
public class AuthController implements AuthApi {

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @Override
  public ResponseEntity<LoginResponse> authLogin(LoginRequest loginRequest) {
    return ResponseEntity.ok(service.login(loginRequest));
  }

  @Override
  public ResponseEntity<Void> authLogout() {
    service.logout();
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TokenResponse> authOidcCallback(OidcCallbackRequest oidcCallbackRequest) {
    return ResponseEntity.ok(service.oidcCallback(oidcCallbackRequest));
  }

  @Override
  public ResponseEntity<TokenResponse> authRefreshToken(TokenRequest tokenRequest) {
    return ResponseEntity.ok(service.refresh(tokenRequest));
  }
}
