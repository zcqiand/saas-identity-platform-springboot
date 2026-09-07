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
 * DB-First scaffold：role_menu_grants（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends RoleMenuGrants 叠加在 src/main/java/.../entity/RoleMenuGrants.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts roleMenuGrants。
 */
@Entity
@Table(name = "role_menu_grants")
public class RoleMenuGrants {

  @Column(name = "role_id", columnDefinition = "uuid", nullable = false)
  private UUID roleId;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "menu_ids", columnDefinition = "uuid[]", nullable = false)
  private List<UUID> menuIds;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime updatedAt;

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
