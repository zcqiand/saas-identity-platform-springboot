package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.OauthAccessToken;
import saas.identity.platform.entity.Generated.OauthCode;
import saas.identity.platform.entity.Generated.OauthRefreshToken;
import saas.identity.platform.entity.Generated.SysUser;
import saas.identity.platform.repository.OauthAccessTokenRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.api.AuthApi;
import saas.identity.shared.dto.LockedAccountResponse;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;
import saas.identity.shared.dto.OidcCallbackRequest;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * M01.F04 SSO 登录 + 失败锁定 + OIDC + 登出（ADR-0020 路线 A）。
 *
 * <p>失败锁定策略（M01.F04.I02）：连续 5 次密码错误 → 锁定 15 分钟； 阈值与窗口在 application.yml 由消费后端配置，可在 DevDataFixer
 * 临时放低。
 */
@RestController
public class AuthController implements AuthApi {

  static final int LOCKOUT_THRESHOLD = 5;
  static final int LOCKOUT_MINUTES = 15;

  private final SysUserRepository users;
  private final OauthCodeRepository codes;
  private final OauthAccessTokenRepository accessTokens;
  private final OauthRefreshTokenRepository refreshTokens;
  private final JwtIssuer jwt;
  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

  public AuthController(
      SysUserRepository users,
      OauthCodeRepository codes,
      OauthAccessTokenRepository accessTokens,
      OauthRefreshTokenRepository refreshTokens,
      JwtIssuer jwt) {
    this.users = users;
    this.codes = codes;
    this.accessTokens = accessTokens;
    this.refreshTokens = refreshTokens;
    this.jwt = jwt;
  }

  @Override
  public ResponseEntity<LoginResponse> sessionsLogin(LoginRequest body) {
    SysUser user =
        users
            .findByUsername(body.getUsername())
            .orElseThrow(() -> new NoSuchElementException("user not found"));

    // M01.F04.I02 — 锁定窗口检查
    if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
      LockedAccountResponse locked = new LockedAccountResponse();
      locked.setCode("ACCOUNT_LOCKED");
      locked.setMessage("连续失败次数过多，请稍后再试");
      locked.setLockedUntil(user.getLockedUntil());
      return ResponseEntity.status(HttpStatus.LOCKED).body(null);
    }

    // 家族 dev 种子约定（nextjs seed-db.mjs）：password 列可存 "plain:{password}"
    // 占位（Phase 5；prod 换 argon2/bcrypt）。aspnetcore/nextjs 两侧已识别该前缀，
    // springboot 对齐，否则同一份种子三后端登录行为分叉（contract-test live 401）。
    boolean plainOk =
        user.getPassword() != null
            && user.getPassword().equals("plain:" + body.getPassword());
    if (!plainOk && !bcrypt.matches(body.getPassword(), user.getPassword())) {
      int attempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1;
      user.setFailedAttempts(attempts);
      if (attempts >= LOCKOUT_THRESHOLD) {
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCKOUT_MINUTES));
      }
      users.save(user);
      throw new IllegalArgumentException("invalid credentials");
    }

    // 成功：重置失败计数
    user.setFailedAttempts(0);
    user.setLockedUntil(null);
    users.save(user);

    UUID tenantId = UUID.randomUUID();
    String accessToken = jwt.issueAccessToken(user.getId(), tenantId);
    String refreshToken = persistRefreshToken(user.getId(), tenantId, body.getClientId());

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

  /** OIDC authorization_code → token exchange（ADR-0020 路线 A）。 一次性消费：code 表行用完即删，防 replay。 */
  @Override
  public ResponseEntity<TokenResponse> sessionsOidcCallback(OidcCallbackRequest body) {
    OauthCode code =
        codes
            .findByCode(body.getCode())
            .orElseThrow(() -> new IllegalArgumentException("invalid_grant: unknown code"));
    if (code.getExpiresAt() != null && code.getExpiresAt().isBefore(OffsetDateTime.now())) {
      codes.delete(code);
      throw new IllegalArgumentException("invalid_grant: expired code");
    }

    String accessToken = jwt.issueAccessToken(code.getUserId(), code.getTenantId());
    String refreshToken =
        persistRefreshToken(code.getUserId(), code.getTenantId(), body.getClientId());

    // 一次性消费：删 code 行
    codes.delete(code);

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

  /** refresh_token → new access_token。rotate：旧 rt 标记 revoked（默认 30 天）。 */
  @Override
  public ResponseEntity<TokenResponse> sessionsRefreshToken(TokenRequest body) {
    OauthRefreshToken rt =
        refreshTokens
            .findByRefreshToken(body.getRefreshToken())
            .orElseThrow(
                () -> new IllegalArgumentException("invalid_grant: unknown refresh_token"));
    if (Boolean.TRUE.equals(rt.getRevoked())) {
      throw new IllegalArgumentException("invalid_grant: revoked refresh_token");
    }

    String accessToken = jwt.issueAccessToken(rt.getUserId(), rt.getTenantId());
    // rotate：旧 rt 标 revoked
    rt.setRevoked(true);
    refreshTokens.save(rt);
    String newRefresh = persistRefreshToken(rt.getUserId(), rt.getTenantId(), body.getClientId());

    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(newRefresh);
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setUserId(rt.getUserId().toString());
    resp.setClientId(body.getClientId());
    resp.setTenantId(rt.getTenantId());
    return ResponseEntity.ok(resp);
  }

  private String persistRefreshToken(UUID userId, UUID tenantId, String clientId) {
    String token = "rt_" + UUID.randomUUID();
    // 注意：id 是 @GeneratedValue(UUID) —— 禁止手动 setId（手动设值会被 Hibernate
    // 当 detached 实体走 merge → ObjectOptimisticLockingFailureException，见
    // memory: springboot-write-path-double-bug）。子表 FK 用保存后的 getId() 回填。
    OauthAccessToken at = new OauthAccessToken();
    at.setTokenId("at_" + UUID.randomUUID());
    at.setAccessToken("n/a"); // 由 JwtIssuer 持有真签
    at.setUserId(userId);
    at.setTenantId(tenantId);
    at.setClientId(clientId);
    at.setExpiresAt(OffsetDateTime.now().plusHours(1));
    at.setRevoked(false);
    accessTokens.save(at);

    OauthRefreshToken rt = new OauthRefreshToken();
    rt.setRefreshToken(token);
    rt.setAccessTokenId(at.getId());
    rt.setUserId(userId);
    rt.setTenantId(tenantId);
    rt.setClientId(clientId);
    rt.setExpiresAt(OffsetDateTime.now().plusDays(30));
    rt.setRevoked(false);
    refreshTokens.save(rt);
    return token;
  }
}
