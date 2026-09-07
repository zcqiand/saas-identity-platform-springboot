package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DB-First scaffold：apps（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends Apps 叠加在 src/main/java/.../entity/Apps.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts apps。
 */
@Entity
@Table(name = "apps")
public class Apps {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "code", columnDefinition = "character varying", nullable = false)
  private String code;

  @Column(name = "name", columnDefinition = "character varying", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "icon", columnDefinition = "character varying")
  private String icon;

  @Column(name = "sort_order", columnDefinition = "integer", nullable = false)
  private Integer sortOrder;

  @Column(name = "status", columnDefinition = "app_status", nullable = false)
  private String status;

  @Column(name = "client_id", columnDefinition = "character varying", nullable = false)
  private String clientId;

  @Column(name = "client_secret_hash", columnDefinition = "character varying")
  private String clientSecretHash;

  @Column(name = "redirect_uris", columnDefinition = "text[]", nullable = false)
  private List<String> redirectUris;

  @Column(name = "scopes", columnDefinition = "text[]", nullable = false)
  private List<String> scopes;

  @Column(name = "grant_types", columnDefinition = "oauth_grant_type[]", nullable = false)
  private List<String> grantTypes;

  @Column(name = "is_first_party", columnDefinition = "boolean", nullable = false)
  private Boolean isFirstParty;

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

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecretHash() {
    return clientSecretHash;
  }

  public void setClientSecretHash(String clientSecretHash) {
    this.clientSecretHash = clientSecretHash;
  }

  public List<String> getRedirectUris() {
    return redirectUris;
  }

  public void setRedirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
  }

  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public List<String> getGrantTypes() {
    return grantTypes;
  }

  public void setGrantTypes(List<String> grantTypes) {
    this.grantTypes = grantTypes;
  }

  public Boolean getIsFirstParty() {
    return isFirstParty;
  }

  public void setIsFirstParty(Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
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
