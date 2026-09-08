package saas.identity.platform.entity.Generated;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * DB-First scaffold：tenant_member_role composite PK（ADR-0025）。
 *
 * <p>由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。 配套 @IdClass(TenantMemberRoleId.class) 用在
 * TenantMemberRole。
 */
public class TenantMemberRoleId implements Serializable {

  private UUID memberId;
  private UUID roleId;

  public TenantMemberRoleId() {}

  public TenantMemberRoleId(UUID memberId, UUID roleId) {
    this.memberId = memberId;
    this.roleId = roleId;
  }

  public UUID getMemberId() {
    return memberId;
  }

  public void setMemberId(UUID memberId) {
    this.memberId = memberId;
  }

  public UUID getRoleId() {
    return roleId;
  }

  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TenantMemberRoleId)) return false;
    TenantMemberRoleId that = (TenantMemberRoleId) o;
    return Objects.equals(memberId, that.memberId) && Objects.equals(roleId, that.roleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(memberId, roleId);
  }
}
