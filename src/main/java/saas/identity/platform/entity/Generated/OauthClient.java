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
 * DB-First scaffold：oauth_client（ADR-0025）。 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等） 通过 extends OauthClient 叠加在
 * src/main/java/.../entity/OauthClient.java。
 *
 * <p>字段含义见 saas-identity-platform-shared/src/db/schema.ts oauthClient。
 */
@Entity
@Table(name = "oauth_client")
public class OauthClient {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "client_id", columnDefinition = "character varying", nullable = false)
  private String clientId;

  @Column(name = "client_secret", columnDefinition = "character varying", nullable = false)
  private String clientSecret;

  @Column(name = "client_name", columnDefinition = "character varying", nullable = false)
  private String clientName;

  @Column(name = "grant_types", columnDefinition = "character varying", nullable = false)
  private String grantTypes;

  @Column(name = "redirect_uris", columnDefinition = "text", nullable = false)
  private String redirectUris;

  @Column(name = "scopes", columnDefinition = "character varying")
  private String scopes;

  @Column(name = "access_token_validity", columnDefinition = "integer", nullable = false)
  private Integer accessTokenValidity;

  @Column(name = "refresh_token_validity", columnDefinition = "integer", nullable = false)
  private Integer refreshTokenValidity;

  @Column(name = "auto_approve", columnDefinition = "boolean", nullable = false)
  private Boolean autoApprove;

  @Column(name = "status", columnDefinition = "smallint", nullable = false)
  private Short status;

  @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getGrantTypes() {
    return grantTypes;
  }

  public void setGrantTypes(String grantTypes) {
    this.grantTypes = grantTypes;
  }

  public String getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(String redirectUris) {
    this.redirectUris = redirectUris;
  }

  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  public Integer getAccessTokenValidity() {
    return accessTokenValidity;
  }

  public void setAccessTokenValidity(Integer accessTokenValidity) {
    this.accessTokenValidity = accessTokenValidity;
  }

  public Integer getRefreshTokenValidity() {
    return refreshTokenValidity;
  }

  public void setRefreshTokenValidity(Integer refreshTokenValidity) {
    this.refreshTokenValidity = refreshTokenValidity;
  }

  public Boolean getAutoApprove() {
    return autoApprove;
  }

  public void setAutoApprove(Boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public Short getStatus() {
    return status;
  }

  public void setStatus(Short status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
