package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

/** DB-First scaffold：tenant_member_role（ADR-0025）。Composite PK (member_id, role_id)。 */
@Entity
@Table(name = "tenant_member_role")
@IdClass(TenantMemberRoleId.class)
public class TenantMemberRole {

  @Id
  @Column(name = "member_id", columnDefinition = "uuid", nullable = false)
  private UUID memberId;

  @Id
  @Column(name = "role_id", columnDefinition = "uuid", nullable = false)
  private UUID roleId;

  public UUID getMemberId() { return memberId; }
  public void setMemberId(UUID memberId) { this.memberId = memberId; }

  public UUID getRoleId() { return roleId; }
  public void setRoleId(UUID roleId) { this.roleId = roleId; }
}
