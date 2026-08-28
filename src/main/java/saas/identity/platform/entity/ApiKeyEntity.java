package saas.identity.platform.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import jakarta.persistence.Convert;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.Type;
import saas.identity.platform.enums.ApiKeyStatus;
import saas.identity.platform.enums.ApiKeyStatusConverter;

/** V004 — tenant-scoped API key（TypeSpec ApiKey）；secret_hash 不可逆散列。 */
@Entity
@Table(
    name = "api_keys",
    uniqueConstraints =
        @UniqueConstraint(
            name = "api_keys_tenant_prefix_unique",
            columnNames = {"tenant_id", "prefix"}))
public class ApiKeyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "name", length = 128, nullable = false)
  private String name;

  @Column(name = "prefix", length = 16, nullable = false)
  private String prefix;

  @Column(name = "secret_hash", length = 255, nullable = false)
  private String secretHash;

  @Convert(converter = ApiKeyStatusConverter.class)
  @Column(name = "status", columnDefinition = "api_key_status", nullable = false)
  private ApiKeyStatus status = ApiKeyStatus.ACTIVE;

  // ListArrayType（而非 StringArrayType）：StringArrayType 只支持 String[] 属性，
  // 用在 List<String> 上会在读回时 ArrayUtil.unwrapArray → Array.newInstance(null) NPE
  // （线上 GET api-keys 500 即此；AppEntity 的 @Transient 是同类问题的绕过，这里走正修）。
  @Type(ListArrayType.class)
  @Column(name = "scopes", columnDefinition = "text[]", nullable = false)
  private List<String> scopes = List.of();

  @Column(
      name = "created_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "last_used_at", columnDefinition = "timestamptz")
  private OffsetDateTime lastUsedAt;

  @Column(name = "expires_at", columnDefinition = "timestamptz")
  private OffsetDateTime expiresAt;

  @Column(name = "revoked_at", columnDefinition = "timestamptz")
  private OffsetDateTime revokedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public String getSecretHash() {
    return secretHash;
  }

  public void setSecretHash(String secretHash) {
    this.secretHash = secretHash;
  }

  public ApiKeyStatus getStatus() {
    return status;
  }

  public void setStatus(ApiKeyStatus status) {
    this.status = status;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getLastUsedAt() {
    return lastUsedAt;
  }

  public void setLastUsedAt(OffsetDateTime lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(OffsetDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public OffsetDateTime getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
  }
}
