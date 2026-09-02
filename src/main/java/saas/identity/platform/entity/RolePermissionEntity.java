package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/** V003 — role ↔ permission M:N junction。PK 是 (role_id, permission_id) 复合。 */
@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionEntity.PK.class)
public class RolePermissionEntity {

  @Id
  @Column(name = "role_id", columnDefinition = "uuid")
  private UUID roleId;

  @Id
  @Column(name = "permission_id", columnDefinition = "uuid")
  private UUID permissionId;

  @Column(
      name = "granted_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  private OffsetDateTime grantedAt;

  @PrePersist
  void onCreate() {
    if (grantedAt == null) grantedAt = Instant.EPOCH.atOffset(ZoneOffset.UTC);
  }

  public UUID getRoleId() {
    return roleId;
  }

  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public UUID getPermissionId() {
    return permissionId;
  }

  public void setPermissionId(UUID permissionId) {
    this.permissionId = permissionId;
  }

  public OffsetDateTime getGrantedAt() {
    return grantedAt;
  }

  public void setGrantedAt(OffsetDateTime grantedAt) {
    this.grantedAt = grantedAt;
  }

  /** 复合主键类。 */
  public static class PK implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID roleId;
    private UUID permissionId;

    public PK() {}

    public PK(UUID roleId, UUID permissionId) {
      this.roleId = roleId;
      this.permissionId = permissionId;
    }

    public UUID getRoleId() {
      return roleId;
    }

    public void setRoleId(UUID roleId) {
      this.roleId = roleId;
    }

    public UUID getPermissionId() {
      return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
      this.permissionId = permissionId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PK)) return false;
      PK pk = (PK) o;
      return Objects.equals(roleId, pk.roleId) && Objects.equals(permissionId, pk.permissionId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(roleId, permissionId);
    }
  }
}
