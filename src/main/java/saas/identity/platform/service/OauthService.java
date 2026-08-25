package saas.identity.platform.service;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.entity.OauthCodeEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.AuthorizeCodeRequest;
import saas.identity.shared.dto.OAuthAuthorize200Response;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * M04.F02 Phase 6 — OAuth 2.0 授权码签发 + 令牌交换 / 刷新.
 *
 * <p>镜像 saas-identity-platform-aspnetcore/src/Controllers/Implementation/OauthController.cs
 * (v0.2.0) 与 saas-nextjs/app/api/v1/oauth/{authorize,token}/route.ts 语义: - Authorize: apps.clientId
 * 校验 + apps.redirect_uris 包含 + apps.scopes 包含 - Token authorization_code: oauth_codes 表查 code →
 * 验未消费/未过期/redirectUri 一致 → 标 consumed + 写 refresh_token (TTL 7d) → 签 access + refresh - Token
 * refresh_token: oauth_codes 表查 refresh → 旋转换发 (旧 consumed, 新写入)
 *
 * <p>错误码: INVALID_CLIENT / INVALID_REDIRECT_URI / INVALID_SCOPE / INVALID_GRANT / INVALID_REQUEST
 * 通过 RuntimeException 抛出, 由 @RestControllerAdvice 转 4xx 响应 (Phase 6 follow-up)。
 *
 * <p>Phase 6 已知简化: - AppEntity.redirectUris/scopes/grantTypes 仍是 @Transient (hypersistence-utils
 * NPE 绕过), Phase 6 OAuthService 走 native SQL 查 text[] 列 (避免 entity 解析); 完整 entity 修复是独立 follow-up。
 * - dev 不验 clientSecret (saas-nextjs 同模式), prod 路径独立 PR (ArgonArg2 hash)。
 */
@Service
public class OauthService {

  private static final long CODE_TTL_SECONDS = 600; // 10 min
  private static final long REFRESH_TTL_SECONDS = 604800; // 7 days

  private final AppRepository appRepository;
  private final OauthCodeRepository oauthCodeRepository;
  private final UserRepository userRepository;
  private final JwtIssuer jwtIssuer;
  private final JdbcTemplate jdbc;

  @Autowired
  public OauthService(
      AppRepository appRepository,
      OauthCodeRepository oauthCodeRepository,
      UserRepository userRepository,
      JwtIssuer jwtIssuer,
      JdbcTemplate jdbc) {
    this.appRepository = appRepository;
    this.oauthCodeRepository = oauthCodeRepository;
    this.userRepository = userRepository;
    this.jwtIssuer = jwtIssuer;
    this.jdbc = jdbc;
  }

  // ===== M04.F02.I06 — 授权码签发 =====

  public OAuthAuthorize200Response authorize(AuthorizeCodeRequest req) {
    String clientIdStr = req.getClientId().toString();
    AppEntity app = appRepository.findByClientId(clientIdStr).orElse(null);
    if (app == null || !app.getStatus().name().equals("ACTIVE")) {
      throw new InvalidClientException(
          "INVALID_CLIENT: clientId=" + clientIdStr + " not registered");
    }

    // text[] 字段 entity 读不到 → native query 取 redirect_uris + scopes
    AppOauthFields fields = loadAppOauthFields(app.getId());

    if (!fields.redirectUris.contains(req.getRedirectUri())) {
      throw new InvalidRedirectUriException(
          "INVALID_REDIRECT_URI: " + req.getRedirectUri() + " not in app.redirect_uris");
    }
    if (req.getScope() == null || !fields.scopes.contains(req.getScope())) {
      throw new InvalidScopeException(
          "INVALID_SCOPE: scope '" + req.getScope() + "' not in app.scopes");
    }

    String code = generateCode();
    OauthCodeEntity oauthCode = new OauthCodeEntity();
    oauthCode.setCode(code);
    oauthCode.setGrantType("authorization_code");
    oauthCode.setAppId(app.getId());
    oauthCode.setTenantId(req.getTenantId());
    oauthCode.setRedirectUri(req.getRedirectUri());
    oauthCode.setScope(req.getScope());
    oauthCode.setExpiresAt(OffsetDateTime.now().plusSeconds(CODE_TTL_SECONDS));
    oauthCodeRepository.save(oauthCode);

    OAuthAuthorize200Response res = new OAuthAuthorize200Response();
    res.setCode(code);
    res.setState(req.getState());
    return res;
  }

  // ===== M04.F02.I07 + I08 — 令牌交换 + 刷新 =====

  public TokenResponse token(TokenRequest req) {
    String clientIdStr = req.getClientId().toString();
    AppEntity app = appRepository.findByClientId(clientIdStr).orElse(null);
    if (app == null || !app.getStatus().name().equals("ACTIVE")) {
      throw new InvalidClientException(
          "INVALID_CLIENT: clientId=" + clientIdStr + " not registered");
    }
    // dev 不验 clientSecret (saas-nextjs 同模式; prod Phase 6+ 加 ArgonArg2 hash 校验)

    String grantType = req.getGrantType().name();
    if ("AUTHORIZATION_CODE".equals(grantType)) {
      return exchangeAuthorizationCode(app, req);
    } else if ("REFRESH_TOKEN".equals(grantType)) {
      return rotateRefreshToken(app, req);
    } else {
      throw new IllegalArgumentException("UNSUPPORTED_GRANT_TYPE: " + grantType);
    }
  }

  private TokenResponse exchangeAuthorizationCode(AppEntity app, TokenRequest req) {
    if (req.getCode() == null || req.getCode().isEmpty()) {
      throw new IllegalArgumentException(
          "INVALID_REQUEST: code required for grantType=authorization_code");
    }
    if (req.getRedirectUri() == null || req.getRedirectUri().isEmpty()) {
      throw new IllegalArgumentException(
          "INVALID_REQUEST: redirectUri required for grantType=authorization_code");
    }

    OauthCodeEntity oauthCode =
        oauthCodeRepository
            .findByCode(req.getCode())
            .orElseThrow(() -> new InvalidGrantException("INVALID_GRANT: code not found"));
    if (!"authorization_code".equals(oauthCode.getGrantType())) {
      throw new InvalidGrantException("INVALID_GRANT: code is not authorization_code");
    }
    if (oauthCode.getConsumedAt() != null) {
      throw new InvalidGrantException("INVALID_GRANT: code already consumed");
    }
    if (oauthCode.getExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new InvalidGrantException("INVALID_GRANT: code expired");
    }
    if (!oauthCode.getAppId().equals(app.getId())) {
      throw new InvalidGrantException("INVALID_GRANT: code does not belong to this client");
    }
    if (!oauthCode.getTenantId().equals(req.getTenantId())) {
      throw new InvalidGrantException("INVALID_GRANT: tenantId mismatch");
    }
    if (!oauthCode.getRedirectUri().equals(req.getRedirectUri())) {
      throw new InvalidGrantException("INVALID_GRANT: redirectUri mismatch");
    }

    UserEntity user =
        userRepository
            .findByTenantId(req.getTenantId(), org.springframework.data.domain.PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("NO_USER: tenantId has no user"));

    // 一次性消费
    oauthCode.setConsumedAt(OffsetDateTime.now());
    oauthCode.setUserId(user.getId());

    // 写 refresh_token
    String refreshToken = JwtIssuer.generateRefreshToken(user.getId());
    OauthCodeEntity refreshRow = new OauthCodeEntity();
    refreshRow.setCode(refreshToken);
    refreshRow.setGrantType("refresh_token");
    refreshRow.setAppId(app.getId());
    refreshRow.setUserId(user.getId());
    refreshRow.setTenantId(req.getTenantId());
    refreshRow.setScope(oauthCode.getScope());
    refreshRow.setExpiresAt(OffsetDateTime.now().plusSeconds(REFRESH_TTL_SECONDS));
    oauthCodeRepository.save(refreshRow);

    oauthCodeRepository.save(oauthCode);

    TokenResponse res = new TokenResponse();
    res.setAccessToken(jwtIssuer.issueAccessToken(user.getId(), req.getTenantId()));
    res.setRefreshToken(refreshToken);
    res.setTokenType("Bearer");
    res.setExpiresIn(3600);
    res.setScope(oauthCode.getScope() == null ? "" : oauthCode.getScope());
    return res;
  }

  private TokenResponse rotateRefreshToken(AppEntity app, TokenRequest req) {
    if (req.getRefreshToken() == null || req.getRefreshToken().isEmpty()) {
      throw new IllegalArgumentException(
          "INVALID_REQUEST: refreshToken required for grantType=refresh_token");
    }
    OauthCodeEntity oldRefresh =
        oauthCodeRepository
            .findByCode(req.getRefreshToken())
            .orElseThrow(() -> new InvalidGrantException("INVALID_GRANT: refresh_token not found"));
    if (!"refresh_token".equals(oldRefresh.getGrantType())) {
      throw new InvalidGrantException("INVALID_GRANT: code is not refresh_token");
    }
    if (oldRefresh.getConsumedAt() != null) {
      throw new InvalidGrantException(
          "INVALID_GRANT: refresh_token already consumed (rotate-once semantics)");
    }
    if (oldRefresh.getExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new InvalidGrantException("INVALID_GRANT: refresh_token expired");
    }
    if (!oldRefresh.getAppId().equals(app.getId())) {
      throw new InvalidGrantException(
          "INVALID_GRANT: refresh_token does not belong to this client");
    }
    if (oldRefresh.getUserId() == null) {
      throw new InvalidGrantException("INVALID_GRANT: refresh_token has no user_id");
    }
    if (!oldRefresh.getTenantId().equals(req.getTenantId())) {
      throw new InvalidGrantException("INVALID_GRANT: tenantId mismatch");
    }

    oldRefresh.setConsumedAt(OffsetDateTime.now());

    String newRefresh = JwtIssuer.generateRefreshToken(oldRefresh.getUserId());
    OauthCodeEntity newRow = new OauthCodeEntity();
    newRow.setCode(newRefresh);
    newRow.setGrantType("refresh_token");
    newRow.setAppId(app.getId());
    newRow.setUserId(oldRefresh.getUserId());
    newRow.setTenantId(oldRefresh.getTenantId());
    newRow.setScope(oldRefresh.getScope());
    newRow.setExpiresAt(OffsetDateTime.now().plusSeconds(REFRESH_TTL_SECONDS));
    oauthCodeRepository.save(newRow);
    oauthCodeRepository.save(oldRefresh);

    TokenResponse res = new TokenResponse();
    res.setAccessToken(
        jwtIssuer.issueAccessToken(oldRefresh.getUserId(), oldRefresh.getTenantId()));
    res.setRefreshToken(newRefresh);
    res.setTokenType("Bearer");
    res.setExpiresIn(3600);
    res.setScope(oldRefresh.getScope() == null ? "" : oldRefresh.getScope());
    return res;
  }

  // ===== helpers =====

  /**
   * Phase 6 known limitation: AppEntity.redirectUris/scopes/grantTypes 是 @Transient
   * (hypersistence-utils NPE), entity 读不到 text[] 列。本方法走 native SQL 直接查 DB。 完整 entity 修复是独立
   * follow-up。
   */
  private AppOauthFields loadAppOauthFields(UUID appId) {
    AppOauthFields fields = new AppOauthFields();
    fields.redirectUris =
        jdbc
            .queryForList(
                "SELECT unnest(redirect_uris) FROM apps WHERE id = ?", String.class, appId)
            .stream()
            .filter(s -> s != null && !s.isEmpty())
            .collect(java.util.stream.Collectors.toList());
    if (fields.redirectUris.isEmpty()) {
      // empty redirect_uris 不是合法 app —— 但允许 fall through 让 Authorize 抛 INVALID_REDIRECT_URI
      fields.redirectUris = java.util.Collections.emptyList();
    }
    fields.scopes =
        jdbc
            .queryForList("SELECT unnest(scopes) FROM apps WHERE id = ?", String.class, appId)
            .stream()
            .filter(s -> s != null && !s.isEmpty())
            .collect(java.util.stream.Collectors.toList());
    if (fields.scopes.isEmpty()) {
      fields.scopes = java.util.Collections.emptyList();
    }
    return fields;
  }

  private static String generateCode() {
    String rand =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                UUID.randomUUID().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    return "saas-code-" + OffsetDateTime.now().toInstant().toEpochMilli() + "-" + rand;
  }

  private static class AppOauthFields {
    java.util.List<String> redirectUris;
    java.util.List<String> scopes;
  }

  // ===== exceptions (Phase 6 follow-up: 转 4xx by @RestControllerAdvice) =====

  public static class InvalidClientException extends RuntimeException {
    public InvalidClientException(String msg) {
      super(msg);
    }
  }

  public static class InvalidRedirectUriException extends RuntimeException {
    public InvalidRedirectUriException(String msg) {
      super(msg);
    }
  }

  public static class InvalidScopeException extends RuntimeException {
    public InvalidScopeException(String msg) {
      super(msg);
    }
  }

  public static class InvalidGrantException extends RuntimeException {
    public InvalidGrantException(String msg) {
      super(msg);
    }
  }
}
