package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
 * EffectiveMenuNode
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-19T07:34:59.395972600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class EffectiveMenuNode {

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

  private List<@Valid EffectiveMenuNode> children = new ArrayList<>();

  public EffectiveMenuNode() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public EffectiveMenuNode(UUID id, String clientId, UUID parentId, String title, SysMenuType type, Integer sortOrder, List<@Valid EffectiveMenuNode> children) {
    this.id = id;
    this.clientId = clientId;
    this.parentId = parentId;
    this.title = title;
    this.type = type;
    this.sortOrder = sortOrder;
    this.children = children;
  }

  public EffectiveMenuNode id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @NotNull @Valid 
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(UUID id) {
    this.id = id;
  }

  public EffectiveMenuNode clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
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

  public EffectiveMenuNode parentId(UUID parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Get parentId
   * @return parentId
   */
  @NotNull @Valid 
  @Schema(name = "parentId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("parentId")
  public UUID getParentId() {
    return parentId;
  }

  @JsonProperty("parentId")
  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public EffectiveMenuNode title(String title) {
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

  public EffectiveMenuNode type(SysMenuType type) {
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

  public EffectiveMenuNode path(@Nullable String path) {
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

  public EffectiveMenuNode component(@Nullable String component) {
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

  public EffectiveMenuNode perms(@Nullable String perms) {
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

  public EffectiveMenuNode icon(@Nullable String icon) {
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

  public EffectiveMenuNode sortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
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

  public EffectiveMenuNode children(List<@Valid EffectiveMenuNode> children) {
    this.children = children;
    return this;
  }

  public EffectiveMenuNode addChildrenItem(EffectiveMenuNode childrenItem) {
    if (this.children == null) {
      this.children = new ArrayList<>();
    }
    this.children.add(childrenItem);
    return this;
  }

  /**
   * Get children
   * @return children
   */
  @NotNull @Valid 
  @Schema(name = "children", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("children")
  public List<@Valid EffectiveMenuNode> getChildren() {
    return children;
  }

  @JsonProperty("children")
  public void setChildren(List<@Valid EffectiveMenuNode> children) {
    this.children = children;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EffectiveMenuNode effectiveMenuNode = (EffectiveMenuNode) o;
    return Objects.equals(this.id, effectiveMenuNode.id) &&
        Objects.equals(this.clientId, effectiveMenuNode.clientId) &&
        Objects.equals(this.parentId, effectiveMenuNode.parentId) &&
        Objects.equals(this.title, effectiveMenuNode.title) &&
        Objects.equals(this.type, effectiveMenuNode.type) &&
        Objects.equals(this.path, effectiveMenuNode.path) &&
        Objects.equals(this.component, effectiveMenuNode.component) &&
        Objects.equals(this.perms, effectiveMenuNode.perms) &&
        Objects.equals(this.icon, effectiveMenuNode.icon) &&
        Objects.equals(this.sortOrder, effectiveMenuNode.sortOrder) &&
        Objects.equals(this.children, effectiveMenuNode.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, clientId, parentId, title, type, path, component, perms, icon, sortOrder, children);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EffectiveMenuNode {\n");
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
    sb.append("    children: ").append(toIndentedString(children)).append("\n");
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

