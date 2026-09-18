package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** CreateSysRoleRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-19T07:34:59.395972600+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class CreateSysRoleRequest {

  private String clientId;

  private String roleCode;

  private String roleName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Boolean isPreset;

  public CreateSysRoleRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public CreateSysRoleRequest(String clientId, String roleCode, String roleName) {
    this.clientId = clientId;
    this.roleCode = roleCode;
    this.roleName = roleName;
  }

  public CreateSysRoleRequest clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   *
   * @return clientId
   */
  @NotNull
  @Schema(name = "clientId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public CreateSysRoleRequest roleCode(String roleCode) {
    this.roleCode = roleCode;
    return this;
  }

  /**
   * Get roleCode
   *
   * @return roleCode
   */
  @NotNull
  @Size(min = 1, max = 64)
  @Schema(name = "roleCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleCode")
  public String getRoleCode() {
    return roleCode;
  }

  @JsonProperty("roleCode")
  public void setRoleCode(String roleCode) {
    this.roleCode = roleCode;
  }

  public CreateSysRoleRequest roleName(String roleName) {
    this.roleName = roleName;
    return this;
  }

  /**
   * Get roleName
   *
   * @return roleName
   */
  @NotNull
  @Size(min = 1, max = 64)
  @Schema(name = "roleName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleName")
  public String getRoleName() {
    return roleName;
  }

  @JsonProperty("roleName")
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public CreateSysRoleRequest description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   *
   * @return description
   */
  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public CreateSysRoleRequest isPreset(@Nullable Boolean isPreset) {
    this.isPreset = isPreset;
    return this;
  }

  /**
   * Get isPreset
   *
   * @return isPreset
   */
  @Schema(name = "isPreset", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isPreset")
  public @Nullable Boolean getIsPreset() {
    return isPreset;
  }

  @JsonProperty("isPreset")
  public void setIsPreset(@Nullable Boolean isPreset) {
    this.isPreset = isPreset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateSysRoleRequest createSysRoleRequest = (CreateSysRoleRequest) o;
    return Objects.equals(this.clientId, createSysRoleRequest.clientId)
        && Objects.equals(this.roleCode, createSysRoleRequest.roleCode)
        && Objects.equals(this.roleName, createSysRoleRequest.roleName)
        && Objects.equals(this.description, createSysRoleRequest.description)
        && Objects.equals(this.isPreset, createSysRoleRequest.isPreset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, roleCode, roleName, description, isPreset);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateSysRoleRequest {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    roleCode: ").append(toIndentedString(roleCode)).append("\n");
    sb.append("    roleName: ").append(toIndentedString(roleName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isPreset: ").append(toIndentedString(isPreset)).append("\n");
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
