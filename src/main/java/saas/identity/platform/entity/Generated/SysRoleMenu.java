package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

/** DB-First scaffold：sys_role_menu（ADR-0025）。Composite PK (role_id, menu_id)。 */
@Entity
@Table(name = "sys_role_menu")
@IdClass(SysRoleMenuId.class)
public class SysRoleMenu {

  @Id
  @Column(name = "role_id", columnDefinition = "uuid", nullable = false)
  private UUID roleId;

  @Id
  @Column(name = "menu_id", columnDefinition = "uuid", nullable = false)
  private UUID menuId;

  public UUID getRoleId() { return roleId; }
  public void setRoleId(UUID roleId) { this.roleId = roleId; }

  public UUID getMenuId() { return menuId; }
  public void setMenuId(UUID menuId) { this.menuId = menuId; }
}
