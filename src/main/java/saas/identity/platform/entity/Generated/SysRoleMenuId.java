package saas.identity.platform.entity.Generated;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * DB-First scaffold：sys_role_menu composite PK（ADR-0025）。
 *
 * <p>由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。 配套 @IdClass(SysRoleMenuId.class) 用在
 * SysRoleMenu。
 */
public class SysRoleMenuId implements Serializable {

  private UUID roleId;
  private UUID menuId;

  public SysRoleMenuId() {}

  public SysRoleMenuId(UUID roleId, UUID menuId) {
    this.roleId = roleId;
    this.menuId = menuId;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SysRoleMenuId)) return false;
    SysRoleMenuId that = (SysRoleMenuId) o;
    return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, menuId);
  }
}
