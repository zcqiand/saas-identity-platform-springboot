package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SysRoleMenu
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class SysRoleMenu {

  private UUID roleId;

  private UUID menuId;

  public SysRoleMenu() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SysRoleMenu(UUID roleId, UUID menuId) {
    this.roleId = roleId;
    this.menuId = menuId;
  }

  public SysRoleMenu roleId(UUID roleId) {
    this.roleId = roleId;
    return this;
  }

  /**
   * Get roleId
   * @return roleId
   */
  @NotNull @Valid 
  @Schema(name = "roleId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleId")
  public UUID getRoleId() {
    return roleId;
  }

  @JsonProperty("roleId")
  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public SysRoleMenu menuId(UUID menuId) {
    this.menuId = menuId;
    return this;
  }

  /**
   * Get menuId
   * @return menuId
   */
  @NotNull @Valid 
  @Schema(name = "menuId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("menuId")
  public UUID getMenuId() {
    return menuId;
  }

  @JsonProperty("menuId")
  public void setMenuId(UUID menuId) {
    this.menuId = menuId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SysRoleMenu sysRoleMenu = (SysRoleMenu) o;
    return Objects.equals(this.roleId, sysRoleMenu.roleId) &&
        Objects.equals(this.menuId, sysRoleMenu.menuId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, menuId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SysRoleMenu {\n");
    sb.append("    roleId: ").append(toIndentedString(roleId)).append("\n");
    sb.append("    menuId: ").append(toIndentedString(menuId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

