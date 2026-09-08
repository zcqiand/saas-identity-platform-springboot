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
 * DB-First scaffold：oauth_access_token（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends OauthAccessToken 叠加在 src/main/java/.../entity/OauthAccessToken.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts oauthAccessToken。

 */
@Entity
@Table(name = "oauth_access_token")
public class OauthAccessToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "token_id", columnDefinition = "character varying", nullable = false)
  private String tokenId;

  @Column(name = "access_token", columnDefinition = "text", nullable = false)
  private String accessToken;

  @Column(name = "client_id", columnDefinition = "character varying", nullable = false)
  private String clientId;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "tenant_id", columnDefinition = "uuid")
  private UUID tenantId;

  @Column(name = "scope", columnDefinition = "character varying")
  private String scope;

  @Column(name = "token_type", columnDefinition = "character varying", nullable = false)
  private String tokenType;

  @Column(name = "expires_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "revoked", columnDefinition = "boolean", nullable = false)
  private Boolean revoked;

  @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTokenId() {
    return tokenId;
  }

  public void setTokenId(String tokenId) {
    this.tokenId = tokenId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
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

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Boolean getRevoked() {
    return revoked;
  }

  public void setRevoked(Boolean revoked) {
    this.revoked = revoked;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
