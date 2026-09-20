package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

/** SysMenu */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    comments = "Generator version: 7.24.0")
public class SysMenu {

  private UUID id;

  private String clientId;

  private UUID parentId;

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

  private Integer sortOrder;

  private Integer status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  public SysMenu() {
    super();
  }

  /** Constructor with only required parameters */
  public SysMenu(
      UUID id,
      String clientId,
      UUID parentId,
      String title,
      SysMenuType type,
      Integer sortOrder,
      Integer status,
      OffsetDateTime createdAt) {
    this.id = id;
    this.clientId = clientId;
    this.parentId = parentId;
    this.title = title;
    this.type = type;
    this.sortOrder = sortOrder;
    this.status = status;
    this.createdAt = createdAt;
  }

  public SysMenu id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   *
   * @return id
   */
  @NotNull
  @Valid
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(UUID id) {
    this.id = id;
  }

  public SysMenu clientId(String clientId) {
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

  public SysMenu parentId(UUID parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Get parentId
   *
   * @return parentId
   */
  @NotNull
  @Valid
  @Schema(name = "parentId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("parentId")
  public UUID getParentId() {
    return parentId;
  }

  @JsonProperty("parentId")
  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public SysMenu title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Get title
   *
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

  public SysMenu type(SysMenuType type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   *
   * @return type
   */
  @NotNull
  @Valid
  @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("type")
  public SysMenuType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(SysMenuType type) {
    this.type = type;
  }

  public SysMenu path(@Nullable String path) {
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

  public SysMenu component(@Nullable String component) {
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

  public SysMenu perms(@Nullable String perms) {
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

  public SysMenu icon(@Nullable String icon) {
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

  public SysMenu sortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
   *
   * @return sortOrder
   */
  @NotNull
  @Schema(name = "sortOrder", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("sortOrder")
  public Integer getSortOrder() {
    return sortOrder;
  }

  @JsonProperty("sortOrder")
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public SysMenu status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @NotNull
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public Integer getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(Integer status) {
    this.status = status;
  }

  public SysMenu createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   *
   * @return createdAt
   */
  @NotNull
  @Valid
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SysMenu sysMenu = (SysMenu) o;
    return Objects.equals(this.id, sysMenu.id)
        && Objects.equals(this.clientId, sysMenu.clientId)
        && Objects.equals(this.parentId, sysMenu.parentId)
        && Objects.equals(this.title, sysMenu.title)
        && Objects.equals(this.type, sysMenu.type)
        && Objects.equals(this.path, sysMenu.path)
        && Objects.equals(this.component, sysMenu.component)
        && Objects.equals(this.perms, sysMenu.perms)
        && Objects.equals(this.icon, sysMenu.icon)
        && Objects.equals(this.sortOrder, sysMenu.sortOrder)
        && Objects.equals(this.status, sysMenu.status)
        && Objects.equals(this.createdAt, sysMenu.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, clientId, parentId, title, type, path, component, perms, icon, sortOrder, status,
        createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SysMenu {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    perms: ").append(toIndentedString(perms)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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
