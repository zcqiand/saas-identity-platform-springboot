package saas.identity.platform.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtIssuer — HS256 签名 JWT 服务 (Phase 6 真 OAuth).
 *
 * <p>镜像 saas-identity-platform-aspnetcore/src/Security/JwtIssuer.cs (v0.2.0): - HS256, ≥32 bytes
 * signing key - Claims: sub (userId), tenant_id, jti - JWT_SIGNING_KEY env (与 lab-springboot
 * LAB_JWT_SECRET 同值, 见 stateful-cuddling-cherny.md 决策 §1) - GenerateRefreshToken 格式
 * saas-rt-{userId}-{ts-ms}-{rand-base64} (与 saas-nextjs lib/oauth-store.ts:97-99 同款)
 */
@Component
public class JwtIssuer {

  private final MACSigner signer;
  private final String issuer;
  private final String audience;
  private final long ttlSeconds;

  // CT_CONSTRUCTOR_THROW 在 spotbugs-exclude.xml 集中屏蔽（Spring bean 生命周期）

  public JwtIssuer(
      @Value("${JWT_SIGNING_KEY:}") String signingKey,
      @Value("${JWT_ISSUER:}") String issuer,
      @Value("${JWT_AUDIENCE:}") String audience,
      @Value("${JWT_TTL_SECONDS:}") Long ttlSecondsRaw) {
    // ADR-0019：issuer/audience/ttl 缺失 throw,不允许 "saas-identity-platform" / 3600 字面兜底。
    if (signingKey == null || signingKey.isEmpty()) {
      throw new IllegalStateException("JWT_SIGNING_KEY env not configured (ADR-0019 禁字面默认值)");
    }
    if (signingKey.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException(
          "JWT_SIGNING_KEY must be >=32 bytes for HS256 (got "
              + signingKey.getBytes(StandardCharsets.UTF_8).length
              + ")");
    }
    if (issuer == null || issuer.isEmpty()) {
      throw new IllegalStateException(
          "JWT_ISSUER env required (ADR-0019 禁 \"saas-identity-platform\" 字面默认值)");
    }
    if (audience == null || audience.isEmpty()) {
      throw new IllegalStateException(
          "JWT_AUDIENCE env required (ADR-0019 禁 \"saas-identity-platform-clients\" 字面默认值)");
    }
    if (ttlSecondsRaw == null || ttlSecondsRaw <= 0) {
      throw new IllegalStateException(
          "JWT_TTL_SECONDS env required,正整数 (ADR-0019 禁 \"3600\" 字面默认值)");
    }
    MACSigner macSigner;
    try {
      macSigner = new MACSigner(signingKey.getBytes(StandardCharsets.UTF_8));
    } catch (JOSEException e) {
      throw new IllegalStateException("JWT_SIGNING_KEY invalid for HS256", e);
    }
    this.signer = macSigner;
    this.issuer = issuer;
    this.audience = audience;
    this.ttlSeconds = ttlSecondsRaw;
  }

  /** 签 access token。Claims: sub (userId), tenant_id, jti + 标准 iat/nbf/exp. */
  public String issueAccessToken(UUID userId, UUID tenantId) {
    return issueAccessToken(userId, tenantId, this.ttlSeconds);
  }

  /**
   * 测试 helper：给 L4 单元测试签 HS256 token。 允许任意 sub/tenant_id（绕过 entity 校验），方便 fixture-driven 测试。 prod
   * 路径不走这里（AuthService.issueAccessToken 才走 entity）。
   */
  public String issueAccessTokenForTest(String sub, String tenantId) {
    return issueAccessTokenForTest(sub, tenantId, this.ttlSeconds, null);
  }

  /** 测试 helper (允许覆盖 ttl + scope). */
  public String issueAccessTokenForTest(
      String sub, String tenantId, long ttlSecondsOverride, String scope) {
    try {
      Instant now = Instant.now();
      JWTClaimsSet.Builder builder =
          new JWTClaimsSet.Builder()
              .issuer(issuer)
              .audience(audience)
              .subject(sub)
              .claim("tenant_id", tenantId)
              .jwtID(UUID.randomUUID().toString())
              .issueTime(Date.from(now))
              .notBeforeTime(Date.from(now))
              .expirationTime(Date.from(now.plusSeconds(ttlSecondsOverride)));
      if (scope != null) {
        builder.claim("scope", scope);
      }
      SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
      jwt.sign(signer);
      return jwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to sign access token (test helper)", e);
    }
  }

  /** 签 access token (允许覆盖 ttl, /token endpoint 用 3600s 默认值)。 */
  public String issueAccessToken(UUID userId, UUID tenantId, long ttlSecondsOverride) {
    try {
      Instant now = Instant.now();
      JWTClaimsSet claims =
          new JWTClaimsSet.Builder()
              .issuer(issuer)
              .audience(audience)
              .subject(userId.toString())
              .claim("tenant_id", tenantId.toString())
              .jwtID(UUID.randomUUID().toString())
              .issueTime(Date.from(now))
              .notBeforeTime(Date.from(now))
              .expirationTime(Date.from(now.plusSeconds(ttlSecondsOverride)))
              .build();
      SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      jwt.sign(signer);
      return jwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to sign access token", e);
    }
  }

  /**
   * 生成 refresh token。格式 saas-rt-{userId}-{ts-ms}-{rand-base64} —— 与 saas-nextjs
   * lib/oauth-store.ts:97-99 同款, 便于跨 IdP 排障。 实际不解析格式 (仅 opaque string), 存 oauth_codes 表的 code 列。
   */
  public static String generateRefreshToken(UUID userId) {
    String rand =
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
    return "saas-rt-" + userId + "-" + Instant.now().toEpochMilli() + "-" + rand;
  }
}
