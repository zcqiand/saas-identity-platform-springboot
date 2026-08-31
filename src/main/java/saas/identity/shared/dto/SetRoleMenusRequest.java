package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** SetRoleMenusRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-31T17:07:11.941023200+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class SetRoleMenusRequest {

  private List<String> menuIds = new ArrayList<>();

  public SetRoleMenusRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public SetRoleMenusRequest(List<String> menuIds) {
    this.menuIds = menuIds;
  }

  public SetRoleMenusRequest menuIds(List<String> menuIds) {
    this.menuIds = menuIds;
    return this;
  }

  public SetRoleMenusRequest addMenuIdsItem(String menuIdsItem) {
    if (this.menuIds == null) {
      this.menuIds = new ArrayList<>();
    }
    this.menuIds.add(menuIdsItem);
    return this;
  }

  /**
   * Get menuIds
   *
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
    SetRoleMenusRequest setRoleMenusRequest = (SetRoleMenusRequest) o;
    return Objects.equals(this.menuIds, setRoleMenusRequest.menuIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(menuIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SetRoleMenusRequest {\n");
    sb.append("    menuIds: ").append(toIndentedString(menuIds)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
