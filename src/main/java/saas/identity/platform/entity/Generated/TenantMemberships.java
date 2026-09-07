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
 * DB-First scaffold：tenant_memberships（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends TenantMemberships 叠加在 src/main/java/.../entity/TenantMemberships.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts tenantMemberships。
 */
@Entity
@Table(name = "tenant_memberships")
public class TenantMemberships {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
  private UUID userId;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "role_ids", columnDefinition = "uuid[]", nullable = false)
  private List<UUID> roleIds;

  @Column(name = "status", columnDefinition = "membership_status", nullable = false)
  private String status;

  @Column(name = "joined_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime joinedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public List<UUID> getRoleIds() {
    return roleIds;
  }

  public void setRoleIds(List<UUID> roleIds) {
    this.roleIds = roleIds;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public OffsetDateTime getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(OffsetDateTime joinedAt) {
    this.joinedAt = joinedAt;
  }
}
