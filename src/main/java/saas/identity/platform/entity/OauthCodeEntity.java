package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * V009 — OAuth 2.0 authorization_code + refresh_token 存储。 镜像
 * shared/sql/migrations/V014__seed_lab_mgmt_app.sql 的 oauth_codes 表。
 *
 * <p>grant_type 列区分： - "authorization_code" 一次性消费（consumed_at 非 NULL = 已用），TTL 10min -
 * "refresh_token" 旋转换发（每次 /token 旧 refresh 被 consumed，新 refresh 写入），TTL 7d
 *
 * <p>3 个 saas 后端共用此 schema: - saas-aspnetcore: src/Domain/Entities/OauthCode.cs (Phase 6 v0.2.0) -
 * saas-springboot: OauthCodeEntity.java (Phase 6 v0.2.0) - saas-nextjs: Drizzle schema (后续迁移)
 */
@Entity
@Table(
    name = "oauth_codes",
    uniqueConstraints = {@UniqueConstraint(name = "oauth_codes_code_unique", columnNames = "code")},
    indexes = {
      @Index(name = "idx_oauth_codes_app_id", columnList = "app_id"),
      @Index(name = "idx_oauth_codes_expires_at", columnList = "expires_at"),
      @Index(name = "idx_oauth_codes_user_id", columnList = "user_id")
    })
public class OauthCodeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "code", length = 255, nullable = false)
  private String code;

  @Column(name = "grant_type", length = 32, nullable = false)
  private String grantType = "authorization_code";

  @Column(name = "app_id", columnDefinition = "uuid", nullable = false)
  private UUID appId;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "redirect_uri", length = 2048)
  private String redirectUri;

  @Column(name = "scope", length = 512)
  private String scope;

  @Column(name = "expires_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "consumed_at", columnDefinition = "timestamptz")
  private OffsetDateTime consumedAt;

  @Column(
      name = "created_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = Instant.EPOCH.atOffset(ZoneOffset.UTC);
  }

  // ===== getters/setters =====

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

  public String getGrantType() {
    return grantType;
  }

  public void setGrantType(String grantType) {
    this.grantType = grantType;
  }

  public UUID getAppId() {
    return appId;
  }

  public void setAppId(UUID appId) {
    this.appId = appId;
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

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public OffsetDateTime getConsumedAt() {
    return consumedAt;
  }

  public void setConsumedAt(OffsetDateTime consumedAt) {
    this.consumedAt = consumedAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
