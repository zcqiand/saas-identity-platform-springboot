package saas.identity.platform.entity;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import saas.identity.platform.enums.MembershipStatus;
import saas.identity.platform.enums.MembershipStatusConverter;

/** V002 — cross-tenant 成员关系（TypeSpec TenantMembership）。 */
@Entity
@Table(
    name = "tenant_memberships",
    uniqueConstraints =
        @UniqueConstraint(
            name = "memberships_user_tenant_unique",
            columnNames = {"user_id", "tenant_id"}))
public class TenantMembershipEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
  private UUID userId;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Transient private List<UUID> roleIds = List.of();

  @Convert(converter = MembershipStatusConverter.class)
  @Column(name = "status", columnDefinition = "membership_status", nullable = false)
  private MembershipStatus status = MembershipStatus.INVITED;

  @Column(name = "joined_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;

  @PrePersist
  void onCreate() {
    if (joinedAt == null) joinedAt = OffsetDateTime.now();
  }

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

  public MembershipStatus getStatus() {
    return status;
  }

  public void setStatus(MembershipStatus status) {
    this.status = status;
  }

  public OffsetDateTime getJoinedAt() {
    return joinedAt;
  }

  public void setJoinedAt(OffsetDateTime joinedAt) {
    this.joinedAt = joinedAt;
  }
}
