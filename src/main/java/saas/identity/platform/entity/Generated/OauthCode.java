package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DB-First scaffold：oauth_code（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends OauthCode 叠加在 src/main/java/.../entity/OauthCode.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts oauthCode。

 */
@Entity
@Table(name = "oauth_code")
public class OauthCode {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "code", columnDefinition = "character varying", nullable = false)
  private String code;

  @Column(name = "client_id", columnDefinition = "character varying", nullable = false)
  private String clientId;

  @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
  private UUID userId;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "redirect_uri", columnDefinition = "character varying")
  private String redirectUri;

  @Column(name = "scope", columnDefinition = "character varying")
  private String scope;

  @Column(name = "code_challenge", columnDefinition = "character varying")
  private String codeChallenge;

  @Column(name = "code_challenge_method", columnDefinition = "character varying")
  private String codeChallengeMethod;

  @Column(name = "expires_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getCodeChallenge() {
    return codeChallenge;
  }

  public void setCodeChallenge(String codeChallenge) {
    this.codeChallenge = codeChallenge;
  }

  public String getCodeChallengeMethod() {
    return codeChallengeMethod;
  }

  public void setCodeChallengeMethod(String codeChallengeMethod) {
    this.codeChallengeMethod = codeChallengeMethod;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
