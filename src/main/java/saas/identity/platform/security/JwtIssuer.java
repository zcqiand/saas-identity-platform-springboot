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
      @Value("${JWT_ISSUER:saas-identity-platform}") String issuer,
      @Value("${JWT_AUDIENCE:saas-identity-platform-clients}") String audience,
      @Value("${JWT_TTL_SECONDS:3600}") long ttlSeconds) {
    if (signingKey == null || signingKey.isEmpty()) {
      throw new IllegalStateException("JWT_SIGNING_KEY env not configured");
    }
    if (signingKey.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException(
          "JWT_SIGNING_KEY must be >=32 bytes for HS256 (got "
              + signingKey.getBytes(StandardCharsets.UTF_8).length
              + ")");
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
    this.ttlSeconds = ttlSeconds;
  }

  /** 签 access token。Claims: sub (userId), tenant_id, jti + 标准 iat/nbf/exp. */
  public String issueAccessToken(UUID userId, UUID tenantId) {
    return issueAccessToken(userId, tenantId, this.ttlSeconds);
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
