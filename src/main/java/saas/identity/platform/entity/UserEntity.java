package saas.identity.platform.entity;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import saas.identity.platform.enums.UserStatus;
import saas.identity.platform.enums.UserStatusConverter;

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

  @Convert(converter = UserStatusConverter.class)
  @Column(name = "status", columnDefinition = "user_status", nullable = false)
  private UserStatus status = UserStatus.INVITED;

  @Column(name = "password_hash", length = 255)
  private String passwordHash;

  /**
   * roleIds 在 users 表本身是冗余的（authoritative 在 tenant_memberships.role_ids）；本列镜像 shared SQL 「TypeSpec
   * User.roleIds: string[]」字段。Phase 5：删本列，统一从 tenant_memberships 聚合。
   *
   * <p>用 hypersistence-utils UUIDArrayType 而非 @Convert(UuidArrayConverter)： - AttributeConverter 返回
   * Java 数组（UUID[]）Hibernate 找不到对应 JDBC type code， 启动期 buildStaticUpdateGroup 会崩。UserType 自己声明
   * Types.ARRAY + nullSafe Get/Set 走 JDBC Array API 是 Hibernate 6 官方推荐做法。 - dev unblock：saas_dev
   * 的空数组会被 DevDataFixer 灌一个 dummy UUID， 避免 hypersistence-utils 在空数组时 ArrayUtil.unwrapArray
   * NPE（3.9.0 bug）。
   */
  @Transient private List<UUID> roleIds = List.of();

  @Column(
      name = "created_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  // 2026-09-01 contract-test：@PrePersist 在某些路径不可靠（并发/save 竞吃），导致 PG 落
  // `-infinity`。Hibernate @CreationTimestamp / @UpdateTimestamp 在 INSERT/UPDATE 时由 Hibernate
  // 显式生成值，替代 @PrePersist/@PreUpdate 兜底。
  @CreationTimestamp
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  @UpdateTimestamp
  private OffsetDateTime updatedAt;

  // @PrePersist/@PreUpdate 由 Hibernate @CreationTimestamp/@UpdateTimestamp 替代（2026-09-01
  // contract-test：@PrePersist 不可靠导致 PG 落 -infinity）。

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
