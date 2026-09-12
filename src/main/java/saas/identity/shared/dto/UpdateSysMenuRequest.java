package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** UpdateSysMenuRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-12T08:51:22.603663900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class UpdateSysMenuRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID parentId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String title;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable SysMenuType type;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String path;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String component;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String perms;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String icon;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer sortOrder;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer status;

  public UpdateSysMenuRequest parentId(@Nullable UUID parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Get parentId
   *
   * @return parentId
   */
  @Valid
  @Schema(name = "parentId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("parentId")
  public @Nullable UUID getParentId() {
    return parentId;
  }

  @JsonProperty("parentId")
  public void setParentId(@Nullable UUID parentId) {
    this.parentId = parentId;
  }

  public UpdateSysMenuRequest title(@Nullable String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   *
   * @return title
   */
  @Schema(name = "title", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("title")
  public @Nullable String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(@Nullable String title) {
    this.title = title;
  }

  public UpdateSysMenuRequest type(@Nullable SysMenuType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   *
   * @return type
   */
  @Valid
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("type")
  public @Nullable SysMenuType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable SysMenuType type) {
    this.type = type;
  }

  public UpdateSysMenuRequest path(@Nullable String path) {
    this.path = path;
    return this;
  }

  /**
   * Get path
   *
   * @return path
   */
  @Schema(name = "path", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("path")
  public @Nullable String getPath() {
    return path;
  }

  @JsonProperty("path")
  public void setPath(@Nullable String path) {
    this.path = path;
  }

  public UpdateSysMenuRequest component(@Nullable String component) {
    this.component = component;
    return this;
  }

  /**
   * Get component
   *
   * @return component
   */
  @Schema(name = "component", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("component")
  public @Nullable String getComponent() {
    return component;
  }

  @JsonProperty("component")
  public void setComponent(@Nullable String component) {
    this.component = component;
  }

  public UpdateSysMenuRequest perms(@Nullable String perms) {
    this.perms = perms;
    return this;
  }

  /**
   * Get perms
   *
   * @return perms
   */
  @Schema(name = "perms", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("perms")
  public @Nullable String getPerms() {
    return perms;
  }

  @JsonProperty("perms")
  public void setPerms(@Nullable String perms) {
    this.perms = perms;
  }

  public UpdateSysMenuRequest icon(@Nullable String icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Get icon
   *
   * @return icon
   */
  @Schema(name = "icon", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("icon")
  public @Nullable String getIcon() {
    return icon;
  }

  @JsonProperty("icon")
  public void setIcon(@Nullable String icon) {
    this.icon = icon;
  }

  public UpdateSysMenuRequest sortOrder(@Nullable Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
   *
   * @return sortOrder
   */
  @Schema(name = "sortOrder", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sortOrder")
  public @Nullable Integer getSortOrder() {
    return sortOrder;
  }

  @JsonProperty("sortOrder")
  public void setSortOrder(@Nullable Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public UpdateSysMenuRequest status(@Nullable Integer status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable Integer getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable Integer status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateSysMenuRequest updateSysMenuRequest = (UpdateSysMenuRequest) o;
    return Objects.equals(this.parentId, updateSysMenuRequest.parentId)
        && Objects.equals(this.title, updateSysMenuRequest.title)
        && Objects.equals(this.type, updateSysMenuRequest.type)
        && Objects.equals(this.path, updateSysMenuRequest.path)
        && Objects.equals(this.component, updateSysMenuRequest.component)
        && Objects.equals(this.perms, updateSysMenuRequest.perms)
        && Objects.equals(this.icon, updateSysMenuRequest.icon)
        && Objects.equals(this.sortOrder, updateSysMenuRequest.sortOrder)
        && Objects.equals(this.status, updateSysMenuRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentId, title, type, path, component, perms, icon, sortOrder, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateSysMenuRequest {\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    perms: ").append(toIndentedString(perms)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
