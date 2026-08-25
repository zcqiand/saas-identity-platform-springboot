package saas.identity.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.service.OauthService;
import saas.identity.shared.api.OauthApi;
import saas.identity.shared.dto.AuthorizeCodeRequest;
import saas.identity.shared.dto.OAuthAuthorize200Response;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * M04.F02 OAuth IdP Controller — Phase 6.
 *
 * <p>实现 NSwag codegen 出的 OauthApi 接口（不手写路由, 仅手写业务逻辑）。 业务逻辑委托给 OauthService.authorize /
 * OauthService.token。
 *
 * <p>Phase 6 已知简化：异常（InvalidClientException 等）未转 4xx, 默认 RuntimeException → 500。
 * 完整 @RestControllerAdvice 转 4xx (INVALID_CLIENT → 401, INVALID_REDIRECT_URI → 400 等) 是独立 PR。
 */
@RestController
public class OauthController implements OauthApi {

  private final OauthService oauthService;

  public OauthController(OauthService oauthService) {
    this.oauthService = oauthService;
  }

  @Override
  public ResponseEntity<OAuthAuthorize200Response> oAuthAuthorize(
      AuthorizeCodeRequest authorizeCodeRequest) {
    return ResponseEntity.ok(oauthService.authorize(authorizeCodeRequest));
  }

  @Override
  public ResponseEntity<TokenResponse> oAuthToken(TokenRequest tokenRequest) {
    return ResponseEntity.ok(oauthService.token(tokenRequest));
  }
}
