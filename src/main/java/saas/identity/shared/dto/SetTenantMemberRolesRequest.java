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

/** SetTenantMemberRolesRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-10T10:23:40.344575800+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class SetTenantMemberRolesRequest {

  private List<String> roleIds = new ArrayList<>();

  public SetTenantMemberRolesRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public SetTenantMemberRolesRequest(List<String> roleIds) {
    this.roleIds = roleIds;
  }

  public SetTenantMemberRolesRequest roleIds(List<String> roleIds) {
    this.roleIds = roleIds;
    return this;
  }

  public SetTenantMemberRolesRequest addRoleIdsItem(String roleIdsItem) {
    if (this.roleIds == null) {
      this.roleIds = new ArrayList<>();
    }
    this.roleIds.add(roleIdsItem);
    return this;
  }

  /**
   * Get roleIds
   *
   * @return roleIds
   */
  @NotNull
  @Schema(name = "roleIds", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleIds")
  public List<String> getRoleIds() {
    return roleIds;
  }

  @JsonProperty("roleIds")
  public void setRoleIds(List<String> roleIds) {
    this.roleIds = roleIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetTenantMemberRolesRequest setTenantMemberRolesRequest = (SetTenantMemberRolesRequest) o;
    return Objects.equals(this.roleIds, setTenantMemberRolesRequest.roleIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SetTenantMemberRolesRequest {\n");
    sb.append("    roleIds: ").append(toIndentedString(roleIds)).append("\n");
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
