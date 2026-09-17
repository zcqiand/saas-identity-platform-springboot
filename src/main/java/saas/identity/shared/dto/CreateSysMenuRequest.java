package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.UUID;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.SysMenuType;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateSysMenuRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T01:38:48.840222800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateSysMenuRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID parentId;

  private String title;

  private SysMenuType type;

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

  public CreateSysMenuRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateSysMenuRequest(String title, SysMenuType type) {
    this.title = title;
    this.type = type;
  }

  public CreateSysMenuRequest parentId(@Nullable UUID parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Get parentId
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

  public CreateSysMenuRequest title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   * @return title
   */
  @NotNull 
  @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  public CreateSysMenuRequest type(SysMenuType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @NotNull @Valid 
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public SysMenuType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(SysMenuType type) {
    this.type = type;
  }

  public CreateSysMenuRequest path(@Nullable String path) {
    this.path = path;
    return this;
  }

  /**
   * Get path
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

  public CreateSysMenuRequest component(@Nullable String component) {
    this.component = component;
    return this;
  }

  /**
   * Get component
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

  public CreateSysMenuRequest perms(@Nullable String perms) {
    this.perms = perms;
    return this;
  }

  /**
   * Get perms
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

  public CreateSysMenuRequest icon(@Nullable String icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Get icon
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

  public CreateSysMenuRequest sortOrder(@Nullable Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateSysMenuRequest createSysMenuRequest = (CreateSysMenuRequest) o;
    return Objects.equals(this.parentId, createSysMenuRequest.parentId) &&
        Objects.equals(this.title, createSysMenuRequest.title) &&
        Objects.equals(this.type, createSysMenuRequest.type) &&
        Objects.equals(this.path, createSysMenuRequest.path) &&
        Objects.equals(this.component, createSysMenuRequest.component) &&
        Objects.equals(this.perms, createSysMenuRequest.perms) &&
        Objects.equals(this.icon, createSysMenuRequest.icon) &&
        Objects.equals(this.sortOrder, createSysMenuRequest.sortOrder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentId, title, type, path, component, perms, icon, sortOrder);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateSysMenuRequest {\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    perms: ").append(toIndentedString(perms)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
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

