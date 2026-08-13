package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.service.AdminOauthAppService;
import saas.identity.shared.api.AdminOauthAppsApi;
import saas.identity.shared.dto.AdminOAuthAppsAuthorize200Response;
import saas.identity.shared.dto.AdminOAuthAppsListOAuthApps200Response;
import saas.identity.shared.dto.AuthorizeCodeRequest;
import saas.identity.shared.dto.CreateOAuthAppRequest;
import saas.identity.shared.dto.OAuthApp;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;
import saas.identity.shared.dto.UpdateOAuthAppRequest;

/**
 * M07.F01 + M07.F02 + M04 — 平台 admin OAuth 应用 CRUD + 授权码签发 + 令牌交换。
 *
 * <p>authorize / token 走 Phase 5 mock（与 aspnetcore OauthController 对齐：apps/oauth_codes 表 Phase 6
 * 引入）；CRUD 委托 {@link AdminOauthAppService}。平台级（非 tenant-scoped），不走 TenantGuard。
 */
@RestController
public class AdminOauthAppsController implements AdminOauthAppsApi {

  private static final int PAGE_DEFAULT = 0;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final AdminOauthAppService service;

  public AdminOauthAppsController(AdminOauthAppService service) {
    this.service = service;
  }

  private int normPage(Integer page) {
    return page == null ? PAGE_DEFAULT : Math.max(0, page);
  }

  private int normPageSize(Integer pageSize) {
    return pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
  }

  // M04.F02.I02 — 授权码签发（Phase 5 mock）
  @Override
  public ResponseEntity<AdminOAuthAppsAuthorize200Response> adminOAuthAppsAuthorize(
      AuthorizeCodeRequest authorizeCodeRequest) {
    var body =
        new AdminOAuthAppsAuthorize200Response()
            .code(UUID.randomUUID().toString().replace("-", ""))
            .state(authorizeCodeRequest.getState());
    return ResponseEntity.ok(body);
  }

  // M04.F02.I03 — 令牌交换（Phase 5 mock）
  @Override
  public ResponseEntity<TokenResponse> adminOAuthAppsToken(TokenRequest tokenRequest) {
    var body =
        new TokenResponse()
            .accessToken("oauth-access-token-" + UUID.randomUUID().toString().replace("-", ""))
            .refreshToken("oauth-refresh-token-" + UUID.randomUUID().toString().replace("-", ""))
            .tokenType("Bearer")
            .expiresIn(3600);
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<AdminOAuthAppsListOAuthApps200Response> adminOAuthAppsListOAuthApps(
      Integer page, Integer pageSize) {
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    var result = service.list(p, ps);
    var body =
        new AdminOAuthAppsListOAuthApps200Response()
            .items(result.getContent())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<OAuthApp> adminOAuthAppsCreateOAuthApp(
      CreateOAuthAppRequest createOAuthAppRequest) {
    return ResponseEntity.ok(service.create(createOAuthAppRequest));
  }

  @Override
  public ResponseEntity<OAuthApp> adminOAuthAppsGetOAuthApp(String appId) {
    return ResponseEntity.ok(service.get(UUID.fromString(appId)));
  }

  @Override
  public ResponseEntity<OAuthApp> adminOAuthAppsUpdateOAuthApp(
      String appId, UpdateOAuthAppRequest updateOAuthAppRequest) {
    return ResponseEntity.ok(service.update(UUID.fromString(appId), updateOAuthAppRequest));
  }

  @Override
  public ResponseEntity<Void> adminOAuthAppsDeleteOAuthApp(String appId) {
    service.delete(UUID.fromString(appId));
    return ResponseEntity.noContent().build();
  }
}
