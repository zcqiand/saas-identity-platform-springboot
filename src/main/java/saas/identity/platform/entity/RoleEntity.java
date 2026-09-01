package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** V003 — tenant-scoped role（TypeSpec Role）。 */
@Entity
@Table(
    name = "roles",
    uniqueConstraints =
        @UniqueConstraint(
            name = "roles_tenant_code_unique",
            columnNames = {"tenant_id", "code"}))
public class RoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "code", length = 64, nullable = false)
  private String code;

  @Column(name = "name", length = 255, nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(
      name = "created_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  // 2026-09-01 contract-test：@PrePersist 不可靠（参见 ApiKeyEntity 注释）；用 Hibernate
  // @CreationTimestamp / @UpdateTimestamp 替代。
  @CreationTimestamp
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  @UpdateTimestamp
  private OffsetDateTime updatedAt;

  // @PrePersist/@PreUpdate 由 Hibernate @CreationTimestamp/@UpdateTimestamp 替代（2026-09-01
  // contract-test：@PrePersist 不可靠导致 PG 落 -infinity）。

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
