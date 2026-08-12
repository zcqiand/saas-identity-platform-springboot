package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** TenantUsersInviteUserRequest */
@JsonTypeName("TenantUsers_inviteUser_request")
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-12T13:11:49.950871300+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class TenantUsersInviteUserRequest {

  private String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> roleIds = new ArrayList<>();

  public TenantUsersInviteUserRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public TenantUsersInviteUserRequest(String email) {
    this.email = email;
  }

  public TenantUsersInviteUserRequest email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   *
   * @return email
   */
  @NotNull
  @Schema(name = "email", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(String email) {
    this.email = email;
  }

  public TenantUsersInviteUserRequest roleIds(List<String> roleIds) {
    this.roleIds = roleIds;
    return this;
  }

  public TenantUsersInviteUserRequest addRoleIdsItem(String roleIdsItem) {
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
  @Schema(name = "roleIds", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    TenantUsersInviteUserRequest tenantUsersInviteUserRequest = (TenantUsersInviteUserRequest) o;
    return Objects.equals(this.email, tenantUsersInviteUserRequest.email)
        && Objects.equals(this.roleIds, tenantUsersInviteUserRequest.roleIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, roleIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantUsersInviteUserRequest {\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
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
