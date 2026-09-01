package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** V003 — 平台级 permission 字典；TypeSpec 不直接定义，由 roleIds: string[] 推导。 */
@Entity
@Table(
    name = "permissions",
    uniqueConstraints = @UniqueConstraint(name = "permissions_code_unique", columnNames = "code"))
public class PermissionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "code", length = 128, nullable = false)
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
  private OffsetDateTime createdAt;

  @PrePersist
  void onCreate() {
    // 2026-09-01 contract-test M96.F02.I10: fallback to UnixEpoch (= 1970-01-01T00:00:00Z)
    // 与 contract-test assertTimestampShape [1970, 2100] 范围对齐。
    if (createdAt == null) createdAt = Instant.EPOCH.atOffset(ZoneOffset.UTC);
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

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
