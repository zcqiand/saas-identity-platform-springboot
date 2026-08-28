package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.lang.Nullable;

/** EffectiveMenuNode */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-28T16:38:49.530507900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class EffectiveMenuNode {

  private UUID id;

  private UUID appId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID parentId;

  private String code;

  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String path;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String icon;

  private MenuType type;

  private Integer sortOrder;

  private List<@Valid EffectiveMenuNode> children = new ArrayList<>();

  public EffectiveMenuNode() {
    super();
  }

  /** Constructor with only required parameters */
  public EffectiveMenuNode(
      UUID id,
      UUID appId,
      String code,
      String name,
      MenuType type,
      Integer sortOrder,
      List<@Valid EffectiveMenuNode> children) {
    this.id = id;
    this.appId = appId;
    this.code = code;
    this.name = name;
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

  public EffectiveMenuNode appId(UUID appId) {
    this.appId = appId;
    return this;
  }

  /**
   * Get appId
   *
   * @return appId
   */
  @NotNull
  @Valid
  @Schema(name = "appId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("appId")
  public UUID getAppId() {
    return appId;
  }

  @JsonProperty("appId")
  public void setAppId(UUID appId) {
    this.appId = appId;
  }

  public EffectiveMenuNode parentId(@Nullable UUID parentId) {
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

  public EffectiveMenuNode code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   *
   * @return code
   */
  @NotNull
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public EffectiveMenuNode name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public EffectiveMenuNode path(@Nullable String path) {
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

  public EffectiveMenuNode icon(@Nullable String icon) {
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

  public EffectiveMenuNode type(MenuType type) {
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
  public MenuType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(MenuType type) {
    this.type = type;
  }

  public EffectiveMenuNode sortOrder(Integer sortOrder) {
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
   *
   * @return children
   */
  @NotNull
  @Valid
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
    return Objects.equals(this.id, effectiveMenuNode.id)
        && Objects.equals(this.appId, effectiveMenuNode.appId)
        && Objects.equals(this.parentId, effectiveMenuNode.parentId)
        && Objects.equals(this.code, effectiveMenuNode.code)
        && Objects.equals(this.name, effectiveMenuNode.name)
        && Objects.equals(this.path, effectiveMenuNode.path)
        && Objects.equals(this.icon, effectiveMenuNode.icon)
        && Objects.equals(this.type, effectiveMenuNode.type)
        && Objects.equals(this.sortOrder, effectiveMenuNode.sortOrder)
        && Objects.equals(this.children, effectiveMenuNode.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, appId, parentId, code, name, path, icon, type, sortOrder, children);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EffectiveMenuNode {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    children: ").append(toIndentedString(children)).append("\n");
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
