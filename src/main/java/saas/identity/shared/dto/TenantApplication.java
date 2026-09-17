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
 * TenantApplication
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T22:53:30.131396900+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantApplication {

  private UUID id;

  private UUID tenantId;

  private String clientId;

  private Integer status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime expireTime;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  public TenantApplication() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantApplication(UUID id, UUID tenantId, String clientId, Integer status, OffsetDateTime createdAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.clientId = clientId;
    this.status = status;
    this.createdAt = createdAt;
  }

  public TenantApplication id(UUID id) {
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

  public TenantApplication tenantId(UUID tenantId) {
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

  public TenantApplication clientId(String clientId) {
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

  public TenantApplication status(Integer status) {
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

  public TenantApplication expireTime(@Nullable OffsetDateTime expireTime) {
    this.expireTime = expireTime;
    return this;
  }

  /**
   * Get expireTime
   * @return expireTime
   */
  @Valid 
  @Schema(name = "expireTime", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expireTime")
  public @Nullable OffsetDateTime getExpireTime() {
    return expireTime;
  }

  @JsonProperty("expireTime")
  public void setExpireTime(@Nullable OffsetDateTime expireTime) {
    this.expireTime = expireTime;
  }

  public TenantApplication createdAt(OffsetDateTime createdAt) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantApplication tenantApplication = (TenantApplication) o;
    return Objects.equals(this.id, tenantApplication.id) &&
        Objects.equals(this.tenantId, tenantApplication.tenantId) &&
        Objects.equals(this.clientId, tenantApplication.clientId) &&
        Objects.equals(this.status, tenantApplication.status) &&
        Objects.equals(this.expireTime, tenantApplication.expireTime) &&
        Objects.equals(this.createdAt, tenantApplication.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, clientId, status, expireTime, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantApplication {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    expireTime: ").append(toIndentedString(expireTime)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

