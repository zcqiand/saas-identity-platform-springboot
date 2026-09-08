package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * DB-First scaffold：sys_role_menu（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends SysRoleMenu 叠加在 src/main/java/.../entity/SysRoleMenu.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts sysRoleMenu。
 */
@Entity
@Table(name = "sys_role_menu")
public class SysRoleMenu {

  @Column(name = "role_id", columnDefinition = "uuid", nullable = false)
  private UUID roleId;

  @Column(name = "menu_id", columnDefinition = "uuid", nullable = false)
  private UUID menuId;

  public UUID getRoleId() {
    return roleId;
  }

  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public UUID getMenuId() {
    return menuId;
  }

  public void setMenuId(UUID menuId) {
    this.menuId = menuId;
  }
}
