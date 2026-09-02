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

/** CreateMenuRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-02T23:27:00.762429900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class CreateMenuRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID parentId;

  private String code;

  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String path;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String icon;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable MenuType type;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer sortOrder;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable MenuStatus status;

  public CreateMenuRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public CreateMenuRequest(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public CreateMenuRequest parentId(@Nullable UUID parentId) {
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

  public CreateMenuRequest code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   *
   * @return code
   */
  @NotNull
  @Size(min = 2, max = 64)
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public CreateMenuRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @Size(min = 2, max = 255)
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public CreateMenuRequest path(@Nullable String path) {
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

  public CreateMenuRequest icon(@Nullable String icon) {
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

  public CreateMenuRequest type(@Nullable MenuType type) {
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
  public @Nullable MenuType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(@Nullable MenuType type) {
    this.type = type;
  }

  public CreateMenuRequest sortOrder(@Nullable Integer sortOrder) {
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

  public CreateMenuRequest status(@Nullable MenuStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable MenuStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable MenuStatus status) {
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
    CreateMenuRequest createMenuRequest = (CreateMenuRequest) o;
    return Objects.equals(this.parentId, createMenuRequest.parentId)
        && Objects.equals(this.code, createMenuRequest.code)
        && Objects.equals(this.name, createMenuRequest.name)
        && Objects.equals(this.path, createMenuRequest.path)
        && Objects.equals(this.icon, createMenuRequest.icon)
        && Objects.equals(this.type, createMenuRequest.type)
        && Objects.equals(this.sortOrder, createMenuRequest.sortOrder)
        && Objects.equals(this.status, createMenuRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentId, code, name, path, icon, type, sortOrder, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateMenuRequest {\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
