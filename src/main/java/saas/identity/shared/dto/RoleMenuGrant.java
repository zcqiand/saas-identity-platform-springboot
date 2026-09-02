package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

/** RoleMenuGrant */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-01T23:20:59.484585600+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class RoleMenuGrant {

  private UUID roleId;

  private UUID tenantId;

  private List<String> menuIds = new ArrayList<>();

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public RoleMenuGrant() {
    super();
  }

  /** Constructor with only required parameters */
  public RoleMenuGrant(UUID roleId, UUID tenantId, List<String> menuIds, OffsetDateTime updatedAt) {
    this.roleId = roleId;
    this.tenantId = tenantId;
    this.menuIds = menuIds;
    this.updatedAt = updatedAt;
  }

  public RoleMenuGrant roleId(UUID roleId) {
    this.roleId = roleId;
    return this;
  }

  /**
   * Get roleId
   *
   * @return roleId
   */
  @NotNull
  @Valid
  @Schema(name = "roleId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleId")
  public UUID getRoleId() {
    return roleId;
  }

  @JsonProperty("roleId")
  public void setRoleId(UUID roleId) {
    this.roleId = roleId;
  }

  public RoleMenuGrant tenantId(UUID tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   *
   * @return tenantId
   */
  @NotNull
  @Valid
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tenantId")
  public UUID getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public RoleMenuGrant menuIds(List<String> menuIds) {
    this.menuIds = menuIds;
    return this;
  }

  public RoleMenuGrant addMenuIdsItem(String menuIdsItem) {
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

  public RoleMenuGrant updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   *
   * @return updatedAt
   */
  @NotNull
  @Valid
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoleMenuGrant roleMenuGrant = (RoleMenuGrant) o;
    return Objects.equals(this.roleId, roleMenuGrant.roleId)
        && Objects.equals(this.tenantId, roleMenuGrant.tenantId)
        && Objects.equals(this.menuIds, roleMenuGrant.menuIds)
        && Objects.equals(this.updatedAt, roleMenuGrant.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, tenantId, menuIds, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoleMenuGrant {\n");
    sb.append("    roleId: ").append(toIndentedString(roleId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    menuIds: ").append(toIndentedString(menuIds)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
