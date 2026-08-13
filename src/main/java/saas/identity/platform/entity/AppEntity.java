package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import saas.identity.platform.converter.EnumArrayConverter;
import saas.identity.platform.converter.StringArrayConverter;
import saas.identity.platform.enums.AppStatus;

/** V005 — 平台级统一实体：菜单承载 + OAuth client（TypeSpec App）。 */
@Entity
@Table(
    name = "apps",
    uniqueConstraints = {
      @UniqueConstraint(name = "apps_code_unique", columnNames = "code"),
      @UniqueConstraint(name = "apps_client_id_unique", columnNames = "client_id")
    })
public class AppEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "code", length = 64, nullable = false)
  private String code;

  @Column(name = "name", length = 255, nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "icon", length = 64)
  private String icon;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "app_status", nullable = false)
  private AppStatus status = AppStatus.ACTIVE;

  @Column(name = "client_id", length = 128, nullable = false)
  private String clientId;

  @Column(name = "client_secret_hash", length = 255)
  private String clientSecretHash;

  @Convert(converter = StringArrayConverter.class)
  @Column(name = "redirect_uris", columnDefinition = "text[]", nullable = false)
  private List<String> redirectUris = List.of();

  @Convert(converter = StringArrayConverter.class)
  @Column(name = "scopes", columnDefinition = "text[]", nullable = false)
  private List<String> scopes = List.of();

  @Convert(converter = EnumArrayConverter.class)
  @Column(name = "grant_types", columnDefinition = "oauth_grant_type[]", nullable = false)
  private List<String> grantTypes = List.of();

  @Column(name = "is_first_party", nullable = false)
  private Boolean isFirstParty = false;

  @Column(
      name = "created_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = createdAt;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

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

  public AppStatus getStatus() {
    return status;
  }

  public void setStatus(AppStatus status) {
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

  public void setIsFirstParty(Boolean firstParty) {
    isFirstParty = firstParty;
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
