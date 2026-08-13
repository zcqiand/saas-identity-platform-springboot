package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import saas.identity.platform.converter.UuidArrayConverter;
import saas.identity.platform.enums.UserStatus;

/**
 * V002__init_users_memberships.sql — tenant-scoped 用户档案（TypeSpec User）。 一行 / (用户, 租户)；与
 * tenant_memberships（cross-tenant）通过 user_id 关联。
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "users_tenant_email_unique",
          columnNames = {"tenant_id", "email"}),
      @UniqueConstraint(
          name = "users_tenant_username_unique",
          columnNames = {"tenant_id", "username"})
    })
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "username", length = 64, nullable = false)
  private String username;

  @Column(name = "email", length = 255, nullable = false)
  private String email;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status", columnDefinition = "user_status", nullable = false)
  private UserStatus status = UserStatus.INVITED;

  @Column(name = "password_hash", length = 255)
  private String passwordHash;

  /**
   * roleIds 在 users 表本身是冗余的（authoritative 在 tenant_memberships.role_ids）；本列镜像 shared SQL 「TypeSpec
   * User.roleIds: string[]」字段。Phase 5：删本列，统一从 tenant_memberships 聚合。
   */
  @Convert(converter = UuidArrayConverter.class)
  @Column(name = "role_ids", columnDefinition = "uuid[]", nullable = false)
  private List<UUID> roleIds = List.of();

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

  // ===== getters / setters =====

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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public List<UUID> getRoleIds() {
    return roleIds;
  }

  public void setRoleIds(List<UUID> roleIds) {
    this.roleIds = roleIds;
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
