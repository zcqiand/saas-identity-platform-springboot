package saas.identity.platform.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import saas.identity.platform.converter.UuidArrayConverter;

/** V005 — tenant-scoped 角色↔菜单 M:N（TypeSpec RoleMenuGrant；整批 PUT）。 */
@Entity
@Table(name = "role_menu_grants")
public class RoleMenuGrantEntity {

  @Id
  @Column(name = "role_id", columnDefinition = "uuid")
  private UUID roleId;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Convert(converter = UuidArrayConverter.class)
  @Column(name = "menu_ids", columnDefinition = "uuid[]", nullable = false)
  private List<UUID> menuIds = List.of();

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void onSave() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getRoleId() {
    return roleId;
  }

  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public List<UUID> getMenuIds() {
    return menuIds;
  }

  public void setMenuIds(List<UUID> menuIds) {
    this.menuIds = menuIds;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
