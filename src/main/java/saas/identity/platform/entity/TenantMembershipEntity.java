package saas.identity.platform.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import jakarta.persistence.Convert;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.Type;
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

  // 2026-08-30：合同测试抓到 @Transient 致 PG `role_ids` 列永不被读 → /me/tenants 返 []。
  // 改用 hypersistence-utils UUIDArrayType（ApiKeyEntity.scopes 走同样套路 @Type(ListArrayType)）。
  // 空数组在 3.9.0 触发 ArrayUtil.unwrapArray NPE —— V016 alice 实际有 a00000000001，
  // DevDataFixer 兜底空数组（与 UserEntity.roleIds 注释同约定）。Phase 5 与 UserEntity 一起
  // 升级 hypersistence-utils 彻底解决。
  @Type(ListArrayType.class)
  @Column(name = "role_ids", columnDefinition = "uuid[]", nullable = false)
  private List<UUID> roleIds = List.of();

  @Convert(converter = MembershipStatusConverter.class)
  @Column(name = "status", columnDefinition = "membership_status", nullable = false)
  private MembershipStatus status = MembershipStatus.INVITED;

  @Column(name = "joined_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;

  @PrePersist
  void onCreate() {
    if (joinedAt == null) joinedAt = Instant.EPOCH.atOffset(ZoneOffset.UTC);
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
