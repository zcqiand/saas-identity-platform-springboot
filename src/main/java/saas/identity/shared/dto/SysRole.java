package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SysRole
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class SysRole {

  private UUID id;

  private UUID tenantId;

  private String clientId;

  private String roleCode;

  private String roleName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  private Boolean isPreset;

  private Integer status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public SysRole() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SysRole(UUID id, UUID tenantId, String clientId, String roleCode, String roleName, Boolean isPreset, Integer status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.clientId = clientId;
    this.roleCode = roleCode;
    this.roleName = roleName;
    this.isPreset = isPreset;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public SysRole id(UUID id) {
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

  public SysRole tenantId(UUID tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   * @return tenantId
   */
  @NotNull @Valid 
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tenantId")
  public UUID getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public SysRole clientId(String clientId) {
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

  public SysRole roleCode(String roleCode) {
    this.roleCode = roleCode;
    return this;
  }

  /**
   * Get roleCode
   * @return roleCode
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "roleCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleCode")
  public String getRoleCode() {
    return roleCode;
  }

  @JsonProperty("roleCode")
  public void setRoleCode(String roleCode) {
    this.roleCode = roleCode;
  }

  public SysRole roleName(String roleName) {
    this.roleName = roleName;
    return this;
  }

  /**
   * Get roleName
   * @return roleName
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "roleName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roleName")
  public String getRoleName() {
    return roleName;
  }

  @JsonProperty("roleName")
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public SysRole description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
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

  public SysRole isPreset(Boolean isPreset) {
    this.isPreset = isPreset;
    return this;
  }

  /**
   * Get isPreset
   * @return isPreset
   */
  @NotNull 
  @Schema(name = "isPreset", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isPreset")
  public Boolean getIsPreset() {
    return isPreset;
  }

  @JsonProperty("isPreset")
  public void setIsPreset(Boolean isPreset) {
    this.isPreset = isPreset;
  }

  public SysRole status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
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

  public SysRole createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @NotNull @Valid 
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public SysRole updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @NotNull @Valid 
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
    SysRole sysRole = (SysRole) o;
    return Objects.equals(this.id, sysRole.id) &&
        Objects.equals(this.tenantId, sysRole.tenantId) &&
        Objects.equals(this.clientId, sysRole.clientId) &&
        Objects.equals(this.roleCode, sysRole.roleCode) &&
        Objects.equals(this.roleName, sysRole.roleName) &&
        Objects.equals(this.description, sysRole.description) &&
        Objects.equals(this.isPreset, sysRole.isPreset) &&
        Objects.equals(this.status, sysRole.status) &&
        Objects.equals(this.createdAt, sysRole.createdAt) &&
        Objects.equals(this.updatedAt, sysRole.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, clientId, roleCode, roleName, description, isPreset, status, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SysRole {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    roleCode: ").append(toIndentedString(roleCode)).append("\n");
    sb.append("    roleName: ").append(toIndentedString(roleName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isPreset: ").append(toIndentedString(isPreset)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

