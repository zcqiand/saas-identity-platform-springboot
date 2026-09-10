package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SetSysRoleMenusRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-10T15:16:10.537909700+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class SetSysRoleMenusRequest {

  private List<String> menuIds = new ArrayList<>();

  public SetSysRoleMenusRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SetSysRoleMenusRequest(List<String> menuIds) {
    this.menuIds = menuIds;
  }

  public SetSysRoleMenusRequest menuIds(List<String> menuIds) {
    this.menuIds = menuIds;
    return this;
  }

  public SetSysRoleMenusRequest addMenuIdsItem(String menuIdsItem) {
    if (this.menuIds == null) {
      this.menuIds = new ArrayList<>();
    }
    this.menuIds.add(menuIdsItem);
    return this;
  }

  /**
   * Get menuIds
   * @return menuIds
   */
  @NotNull 
  @Schema(name = "menuIds", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("menuIds")
  public List<String> getMenuIds() {
    return menuIds;
  }

  @JsonProperty("menuIds")
  public void setMenuIds(List<String> menuIds) {
    this.menuIds = menuIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetSysRoleMenusRequest setSysRoleMenusRequest = (SetSysRoleMenusRequest) o;
    return Objects.equals(this.menuIds, setSysRoleMenusRequest.menuIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(menuIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SetSysRoleMenusRequest {\n");
    sb.append("    menuIds: ").append(toIndentedString(menuIds)).append("\n");
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

