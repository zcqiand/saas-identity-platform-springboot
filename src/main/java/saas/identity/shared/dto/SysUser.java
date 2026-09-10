package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.SysUserStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SysUser
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-10T15:16:10.537909700+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class SysUser {

  private UUID id;

  private String username;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String mobile;

  private SysUserStatus status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer failedAttempts;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime lockedUntil;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public SysUser() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SysUser(UUID id, String username, SysUserStatus status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    this.id = id;
    this.username = username;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public SysUser id(UUID id) {
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

  public SysUser username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get username
   * @return username
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "username", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public void setUsername(String username) {
    this.username = username;
  }

  public SysUser email(@Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  @jakarta.validation.constraints.Email 
  @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public @Nullable String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  public SysUser mobile(@Nullable String mobile) {
    this.mobile = mobile;
    return this;
  }

  /**
   * Get mobile
   * @return mobile
   */
  
  @Schema(name = "mobile", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mobile")
  public @Nullable String getMobile() {
    return mobile;
  }

  @JsonProperty("mobile")
  public void setMobile(@Nullable String mobile) {
    this.mobile = mobile;
  }

  public SysUser status(SysUserStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public SysUserStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(SysUserStatus status) {
    this.status = status;
  }

  public SysUser failedAttempts(@Nullable Integer failedAttempts) {
    this.failedAttempts = failedAttempts;
    return this;
  }

  /**
   * Get failedAttempts
   * @return failedAttempts
   */
  
  @Schema(name = "failedAttempts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("failedAttempts")
  public @Nullable Integer getFailedAttempts() {
    return failedAttempts;
  }

  @JsonProperty("failedAttempts")
  public void setFailedAttempts(@Nullable Integer failedAttempts) {
    this.failedAttempts = failedAttempts;
  }

  public SysUser lockedUntil(@Nullable OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
    return this;
  }

  /**
   * Get lockedUntil
   * @return lockedUntil
   */
  @Valid 
  @Schema(name = "lockedUntil", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lockedUntil")
  public @Nullable OffsetDateTime getLockedUntil() {
    return lockedUntil;
  }

  @JsonProperty("lockedUntil")
  public void setLockedUntil(@Nullable OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public SysUser createdAt(OffsetDateTime createdAt) {
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

  public SysUser updatedAt(OffsetDateTime updatedAt) {
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
    SysUser sysUser = (SysUser) o;
    return Objects.equals(this.id, sysUser.id) &&
        Objects.equals(this.username, sysUser.username) &&
        Objects.equals(this.email, sysUser.email) &&
        Objects.equals(this.mobile, sysUser.mobile) &&
        Objects.equals(this.status, sysUser.status) &&
        Objects.equals(this.failedAttempts, sysUser.failedAttempts) &&
        Objects.equals(this.lockedUntil, sysUser.lockedUntil) &&
        Objects.equals(this.createdAt, sysUser.createdAt) &&
        Objects.equals(this.updatedAt, sysUser.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, email, mobile, status, failedAttempts, lockedUntil, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SysUser {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    mobile: ").append(toIndentedString(mobile)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    failedAttempts: ").append(toIndentedString(failedAttempts)).append("\n");
    sb.append("    lockedUntil: ").append(toIndentedString(lockedUntil)).append("\n");
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

