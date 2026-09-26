package saas.identity.platform.controller;

// @impl M04.F03.I02 — book anchor (xr-know-007)

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;
import saas.identity.platform.entity.Generated.OauthAccessToken;
import saas.identity.platform.entity.Generated.OauthRefreshToken;
import saas.identity.platform.repository.OauthAccessTokenRepository;
import saas.identity.platform.repository.OauthClientRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;

/**
 * 2026-09-12 live 4-way 修复：AuthController 与 OauthController 共用的 token 对落库组件。
 *
 * <p>此前 persistRefreshToken 私有在 AuthController，OauthController 的 /oauth/token grant 路由
 * 修复（I27/I28）需要同款「校验 clientId FK → 写 oauth_access_token + oauth_refresh_token」 语义，抽到这里避免两份漂移。
 */
@Component
public class TokenIssuer {

  private final OauthClientRepository oauthClients;
  private final OauthAccessTokenRepository accessTokens;
  private final OauthRefreshTokenRepository refreshTokens;

  public TokenIssuer(
      OauthClientRepository oauthClients,
      OauthAccessTokenRepository accessTokens,
      OauthRefreshTokenRepository refreshTokens) {
    this.oauthClients = oauthClients;
    this.accessTokens = accessTokens;
    this.refreshTokens = refreshTokens;
  }

  /**
   * 写一对外常规 access/refresh 行，返回新 refresh token 字符串。
   *
   * <p>client_id 是 FK → oauth_client.client_id（库里是 "lab-management" 这类 code）。请求来源 给未知值时盲插 23503 →
   * 500 裸错误体，写库前校验，未知 → IllegalArgumentException → 400。 scope 透传（oauth_code.scope → token 链保持同一
   * grant 的 scope，I28 断言响应必有 scope）。
   */
  public String persistTokenPair(UUID userId, UUID tenantId, String clientId, String scope) {
    oauthClients
        .findByClientId(clientId)
        .orElseThrow(() -> new IllegalArgumentException("unknown clientId: " + clientId));
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
    // oauth_access_token.{token_type,created_at} 是 NOT NULL（共享仓 SSOT 起列就是 not null）；
    // Hibernate scaffold 实体只声明列不推 NotNull，业务代码必须显式赋值（created_at 另有
    // AuditTimestampInterceptor 兜底）。
    at.setTokenType("Bearer");
    at.setScope(scope);
    at.setExpiresAt(OffsetDateTime.now().plusHours(1));
    at.setRevoked(false);
    at.setCreatedAt(OffsetDateTime.now());
    accessTokens.save(at);

    OauthRefreshToken rt = new OauthRefreshToken();
    rt.setRefreshToken(token);
    rt.setAccessTokenId(at.getId());
    rt.setUserId(userId);
    rt.setTenantId(tenantId);
    rt.setClientId(clientId);
    rt.setExpiresAt(OffsetDateTime.now().plusDays(30));
    rt.setRevoked(false);
    rt.setCreatedAt(OffsetDateTime.now());
    refreshTokens.save(rt);
    return token;
  }
}
