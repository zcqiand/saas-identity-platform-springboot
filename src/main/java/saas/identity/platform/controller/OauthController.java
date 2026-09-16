package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import saas.identity.platform.entity.Generated.OauthAccessToken;
import saas.identity.platform.entity.Generated.OauthClient;
import saas.identity.platform.entity.Generated.OauthCode;
import saas.identity.platform.entity.Generated.OauthRefreshToken;
import saas.identity.platform.repository.OauthAccessTokenRepository;
import saas.identity.platform.repository.OauthClientRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;
import saas.identity.platform.security.InvalidCredentialsException;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.api.OauthApi;
import saas.identity.shared.dto.AuthorizeCodeRequest;
import saas.identity.shared.dto.OAuthAuthorize200Response;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * M04.F03 OAuth authorize + token + refresh（ADR-0020 路线 A）。
 *
 * <p>2026-09-12 live 4-way 修复（R2/R4）： 1. authorize 的 oauth_code 行 user_id / tenant_id 是 FK →
 * sys_user / tenant（23503）， 随机 UUID 必炸；改为绑定当前 Bearer 用户 + 其 JWT tenant_id claim（与 msw
 * oracle「session 用户签出 code」语义一致）。scope / redirectUri 落库供 token 交换校验。 2. /oauth/token 此前忽略
 * grantType，任何合法 clientId 都发新 token 对——I27 code 重放、I28 未知 refreshToken 全部错误地 200。现在按 grantType
 * 路由：authorization_code 一次性消费 + 过期校验； refresh_token rotate（未知/已撤销 → 400），对齐 msw oracle。
 */
@RestController
public class OauthController implements OauthApi {

  private final OauthClientRepository clients;
  private final OauthCodeRepository codes;
  private final OauthAccessTokenRepository accessTokens;
  private final OauthRefreshTokenRepository refreshTokens;
  private final JwtIssuer jwt;
  private final TokenIssuer tokenIssuer;
  private final JwtDecoder jwtDecoder;

  public OauthController(
      OauthClientRepository clients,
      OauthCodeRepository codes,
      OauthAccessTokenRepository accessTokens,
      OauthRefreshTokenRepository refreshTokens,
      JwtIssuer jwt,
      TokenIssuer tokenIssuer,
      JwtDecoder jwtDecoder) {
    this.clients = clients;
    this.codes = codes;
    this.accessTokens = accessTokens;
    this.refreshTokens = refreshTokens;
    this.jwt = jwt;
    this.tokenIssuer = tokenIssuer;
    this.jwtDecoder = jwtDecoder;
  }

  @Override
  public ResponseEntity<OAuthAuthorize200Response> oAuthAuthorize(AuthorizeCodeRequest body) {
    OauthClient client =
        clients
            .findByClientId(body.getClientId())
            .orElseThrow(() -> new IllegalArgumentException("unknown client_id"));
    // 认证前置（四家共同语义）：无 / 坏 Bearer → 401，先于白名单校验。
    UUID userId = currentUserIdOrNull();
    if (userId == null) {
      throw new InvalidCredentialsException("Bearer sub required for authorize");
    }
    UUID tenantId = currentTenantIdOrNull();
    if (tenantId == null) {
      throw new InvalidCredentialsException("JWT tenant_id claim required for authorize");
    }
    // 2026-09-15 四家收敛：redirect 白名单校验（此前 springboot 单侧缺失）。
    // oauth_client.redirect_uris 是 csv 文本；匹配规则与 aspnetcore OAuthController /
    // nextjs authorize route 一致——精确相等，或白名单条目是请求的前缀且边界在 '?'
    // （RFC 6749 §3.1.2，lab 前端回跳带 ?from=<业务路径>）；子路径不算匹配。
    String requestedUri = body.getRedirectUri() == null ? "" : body.getRedirectUri();
    boolean redirectAllowed =
        Arrays.stream((client.getRedirectUris() == null ? "" : client.getRedirectUris()).split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .anyMatch(
                u ->
                    requestedUri.equals(u)
                        || (requestedUri.startsWith(u) && requestedUri.charAt(u.length()) == '?'));
    if (!redirectAllowed) {
      throw new IllegalArgumentException(
          "INVALID_REDIRECT_URI: " + body.getRedirectUri() + " not in oauth_client.redirect_uris");
    }
    String code = "ac_" + UUID.randomUUID();

    OauthCode row = new OauthCode();
    row.setCode(code);
    row.setClientId(client.getClientId());
    row.setUserId(userId);
    row.setTenantId(tenantId);
    row.setRedirectUri(body.getRedirectUri());
    row.setScope(body.getScope());
    row.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
    codes.save(row);

    OAuthAuthorize200Response resp = new OAuthAuthorize200Response();
    resp.setCode(code);
    resp.setState(body.getState());
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<TokenResponse> oAuthToken(TokenRequest body) {
    OauthClient client =
        clients
            .findByClientId(body.getClientId())
            .orElseThrow(() -> new IllegalArgumentException("INVALID_CLIENT: unknown client_id"));
    TokenRequest.GrantTypeEnum grantType = body.getGrantType();
    if (grantType == TokenRequest.GrantTypeEnum.AUTHORIZATION_CODE) {
      return exchangeAuthorizationCode(client, body);
    }
    if (grantType == TokenRequest.GrantTypeEnum.REFRESH_TOKEN) {
      return rotateRefreshToken(client, body);
    }
    throw new IllegalArgumentException("UNSUPPORTED_GRANT_TYPE: " + grantType);
  }

  /** authorization_code grant：code 一次性消费 + 过期校验（I27 重放 → 400）。 */
  private ResponseEntity<TokenResponse> exchangeAuthorizationCode(
      OauthClient client, TokenRequest body) {
    if (body.getCode() == null || body.getCode().isEmpty()) {
      throw new IllegalArgumentException(
          "INVALID_REQUEST: code required for grantType=authorization_code");
    }
    if (body.getRedirectUri() == null || body.getRedirectUri().isEmpty()) {
      throw new IllegalArgumentException(
          "INVALID_REQUEST: redirectUri required for grantType=authorization_code");
    }
    OauthCode row =
        codes
            .findByCode(body.getCode())
            .filter(c -> client.getClientId().equals(c.getClientId()))
            .orElseThrow(() -> new IllegalArgumentException("INVALID_GRANT: code 不存在或已被使用"));
    if (row.getExpiresAt() != null && row.getExpiresAt().isBefore(OffsetDateTime.now())) {
      codes.delete(row);
      throw new IllegalArgumentException("INVALID_GRANT: expired code");
    }
    // 2026-09-15 四家收敛：RFC 6749 §4.1.3——redirect_uri 必须与 authorize 时一致
    // （msw / nextjs / aspnetcore 已有此校验，springboot 此前单侧缺失）。
    if (!body.getRedirectUri().equals(row.getRedirectUri())) {
      codes.delete(row);
      throw new IllegalArgumentException("INVALID_GRANT: redirectUri mismatch");
    }
    // 一次性消费：删 code 行，防重放
    codes.delete(row);
    return ResponseEntity.ok(
        tokenResponse(
            jwt.issueAccessToken(row.getUserId(), row.getTenantId()),
            tokenIssuer.persistTokenPair(
                row.getUserId(), row.getTenantId(), client.getClientId(), row.getScope()),
            scopeOrNull(row.getScope()),
            row.getUserId(),
            client.getClientId(),
            row.getTenantId()));
  }

  /** refresh_token grant：rotate（I28 未知 / 已撤销 refreshToken → 400，此前恒 200）。 */
  private ResponseEntity<TokenResponse> rotateRefreshToken(OauthClient client, TokenRequest body) {
    if (body.getRefreshToken() == null || body.getRefreshToken().isEmpty()) {
      throw new IllegalArgumentException(
          "INVALID_REQUEST: refreshToken required for grantType=refresh_token");
    }
    OauthRefreshToken rt =
        refreshTokens
            .findByRefreshToken(body.getRefreshToken())
            .orElseThrow(
                () -> new IllegalArgumentException("INVALID_GRANT: refreshToken 不存在或已被使用"));
    if (Boolean.TRUE.equals(rt.getRevoked())) {
      throw new IllegalArgumentException("INVALID_GRANT: revoked refresh_token");
    }
    // rotate：旧 rt 标 revoked
    rt.setRevoked(true);
    refreshTokens.save(rt);
    // 新 refresh 行的 client_id 以旧 rt 行绑定的值为准（登录时已过 FK 校验），不信任请求体。
    String clientId = rt.getClientId() != null ? rt.getClientId() : client.getClientId();
    String scope =
        scopeOrNull(
            accessTokens
                .findById(rt.getAccessTokenId())
                .map(OauthAccessToken::getScope)
                .orElse(null));
    String newRefresh =
        tokenIssuer.persistTokenPair(rt.getUserId(), rt.getTenantId(), clientId, scope);
    return ResponseEntity.ok(
        tokenResponse(
            jwt.issueAccessToken(rt.getUserId(), rt.getTenantId()),
            newRefresh,
            scope,
            rt.getUserId(),
            clientId,
            rt.getTenantId()));
  }

  /**
   * TokenResponse 组装。userId / clientId / tenantId 必填三件回显（SSOT TokenResponse，
   * tsp/routes/oauth.tsp）：msw 已剔除，oracle = shared 契约本身；三方共库 → 同一
   * user/tenant UUID 逐字相等，回显即对齐（msw 时代「写库不回显」裁决随之作废）。
   */
  private TokenResponse tokenResponse(
      String accessToken,
      String refreshToken,
      String scope,
      UUID userId,
      String clientId,
      UUID tenantId) {
    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setTokenType("Bearer");
    resp.setExpiresIn((int) Math.min(Integer.MAX_VALUE, jwt.getTtlSeconds()));
    resp.setScope(scope);
    resp.setUserId(userId.toString());
    resp.setClientId(clientId);
    resp.setTenantId(tenantId);
    return resp;
  }

  /** msw oracle 的 scope 是 space-separated（RFC 6749 §3.3）；DB 列是 csv（V014 scopes）。 */
  private String scopeOrNull(String scope) {
    return scope == null ? null : scope.replace(',', ' ').trim();
  }

  private UUID currentUserIdOrNull() {
    Jwt token = bearerJwtOrNull();
    if (token == null) {
      return null;
    }
    try {
      return UUID.fromString(token.getSubject());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private UUID currentTenantIdOrNull() {
    Jwt token = bearerJwtOrNull();
    if (token == null) {
      return null;
    }
    try {
      return UUID.fromString(token.getClaimAsString("tenant_id"));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * 取当前 Bearer 身份。/api/v1/oauth/** 走 SecurityConfig 的独立 permitAll 链（Spring Security 6.x 资源服务器 +
   * permitAll 同链会让无 Bearer 请求 401，见 SecurityConfig @Order(1) 注释），该链不配
   * oauth2ResourceServer，SecurityContextHolder 里没有 Jwt principal——所以主上下文拿不到时手动 decode Authorization
   * 头。无 / 坏 Bearer 返回 null → 401（禁匿名签 code，禁身份字面量兜底）。
   */
  private Jwt bearerJwtOrNull() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof Jwt parsed) {
      return parsed;
    }
    if (!(RequestContextHolder.getRequestAttributes()
        instanceof ServletRequestAttributes servletAttributes)) {
      return null;
    }
    String header = servletAttributes.getRequest().getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      return null;
    }
    try {
      return jwtDecoder.decode(header.substring("Bearer ".length()).trim());
    } catch (RuntimeException e) {
      // JwtException / IllegalArgumentException —— 无效 token 视同匿名
      return null;
    }
  }
}
