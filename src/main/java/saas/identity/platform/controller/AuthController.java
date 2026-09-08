package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.OauthCode;
import saas.identity.platform.entity.Generated.OauthRefreshToken;
import saas.identity.platform.entity.Generated.SysUser;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.api.AuthApi;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;
import saas.identity.shared.dto.OidcCallbackRequest;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/** M01.F04 SSO 登录 + OIDC + 登出（ADR-0020 路线 A）。skeleton 跳密码校验 / 锁定。 */
@RestController
public class AuthController implements AuthApi {

  private final SysUserRepository users;
  private final OauthCodeRepository codes;
  private final OauthRefreshTokenRepository refreshTokens;
  private final JwtIssuer jwt;

  public AuthController(
      SysUserRepository users,
      OauthCodeRepository codes,
      OauthRefreshTokenRepository refreshTokens,
      JwtIssuer jwt) {
    this.users = users;
    this.codes = codes;
    this.refreshTokens = refreshTokens;
    this.jwt = jwt;
  }

  @Override
  public ResponseEntity<LoginResponse> sessionsLogin(LoginRequest body) {
    SysUser user = new SysUser();
    user.setId(UUID.randomUUID());
    user.setUsername(body.getUsername());
    user.setEmail(body.getUsername());

    UUID tenantId = UUID.randomUUID();
    String accessToken = jwt.issueAccessToken(user.getId(), tenantId);
    String refreshToken = "rt_" + UUID.randomUUID();

    saas.identity.shared.dto.SysUser dto = new saas.identity.shared.dto.SysUser();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());

    LoginResponse resp = new LoginResponse();
    resp.setUser(dto);
    resp.setAvailableTenants(java.util.List.of());
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setClientId(body.getClientId());
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<Void> sessionsLogout() {
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TokenResponse> sessionsOidcCallback(OidcCallbackRequest body) {
    OauthCode code = codes.findByCode(body.getCode()).orElse(null);
    if (code == null) {
      return ResponseEntity.status(400).body(null);
    }

    String accessToken = jwt.issueAccessToken(code.getUserId(), code.getTenantId());
    String refreshToken = "rt_" + UUID.randomUUID();

    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setUserId(code.getUserId().toString());
    resp.setClientId(body.getClientId());
    resp.setTenantId(code.getTenantId());
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<TokenResponse> sessionsRefreshToken(TokenRequest body) {
    OauthRefreshToken rt = refreshTokens.findByRefreshToken(body.getRefreshToken()).orElse(null);
    if (rt == null || Boolean.TRUE.equals(rt.getRevoked())) {
      return ResponseEntity.status(400).body(null);
    }
    String accessToken = jwt.issueAccessToken(rt.getUserId(), rt.getTenantId());
    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(body.getRefreshToken());
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setUserId(rt.getUserId().toString());
    resp.setClientId(body.getClientId());
    resp.setTenantId(rt.getTenantId());
    return ResponseEntity.ok(resp);
  }
}
