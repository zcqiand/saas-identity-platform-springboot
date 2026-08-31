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

/** Menu */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-31T17:07:11.941023200+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class Menu {

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

  private MenuStatus status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public Menu() {
    super();
  }

  /** Constructor with only required parameters */
  public Menu(
      UUID id,
      UUID appId,
      String code,
      String name,
      MenuType type,
      Integer sortOrder,
      MenuStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.appId = appId;
    this.code = code;
    this.name = name;
    this.type = type;
    this.sortOrder = sortOrder;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Menu id(UUID id) {
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

  public Menu appId(UUID appId) {
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

  public Menu parentId(@Nullable UUID parentId) {
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

  public Menu code(String code) {
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

  public Menu name(String name) {
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

  public Menu path(@Nullable String path) {
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

  public Menu icon(@Nullable String icon) {
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

  public Menu type(MenuType type) {
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

  public Menu sortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
   *
   * @return sortOrder
   */
  @NotNull
  @Schema(name = "sortOrder", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("sortOrder")
  public Integer getSortOrder() {
    return sortOrder;
  }

  @JsonProperty("sortOrder")
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Menu status(MenuStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @NotNull
  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public MenuStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(MenuStatus status) {
    this.status = status;
  }

  public Menu createdAt(OffsetDateTime createdAt) {
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

  public Menu updatedAt(OffsetDateTime updatedAt) {
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
    Menu menu = (Menu) o;
    return Objects.equals(this.id, menu.id)
        && Objects.equals(this.appId, menu.appId)
        && Objects.equals(this.parentId, menu.parentId)
        && Objects.equals(this.code, menu.code)
        && Objects.equals(this.name, menu.name)
        && Objects.equals(this.path, menu.path)
        && Objects.equals(this.icon, menu.icon)
        && Objects.equals(this.type, menu.type)
        && Objects.equals(this.sortOrder, menu.sortOrder)
        && Objects.equals(this.status, menu.status)
        && Objects.equals(this.createdAt, menu.createdAt)
        && Objects.equals(this.updatedAt, menu.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, appId, parentId, code, name, path, icon, type, sortOrder, status, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Menu {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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
