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

/** TenantMember */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-12T08:51:22.603663900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class TenantMember {

  private UUID id;

  private UUID tenantId;

  private UUID userId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String memberName;

  private Boolean isOwner;

  private TenantMemberStatus status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public TenantMember() {
    super();
  }

  /** Constructor with only required parameters */
  public TenantMember(
      UUID id,
      UUID tenantId,
      UUID userId,
      Boolean isOwner,
      TenantMemberStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.tenantId = tenantId;
    this.userId = userId;
    this.isOwner = isOwner;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public TenantMember id(UUID id) {
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

  public TenantMember tenantId(UUID tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Get tenantId
   *
   * @return tenantId
   */
  @NotNull
  @Valid
  @Schema(name = "tenantId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tenantId")
  public UUID getTenantId() {
    return tenantId;
  }

  @JsonProperty("tenantId")
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public TenantMember userId(UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get userId
   *
   * @return userId
   */
  @NotNull
  @Valid
  @Schema(name = "userId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("userId")
  public UUID getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public TenantMember memberName(@Nullable String memberName) {
    this.memberName = memberName;
    return this;
  }

  /**
   * Get memberName
   *
   * @return memberName
   */
  @Schema(name = "memberName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("memberName")
  public @Nullable String getMemberName() {
    return memberName;
  }

  @JsonProperty("memberName")
  public void setMemberName(@Nullable String memberName) {
    this.memberName = memberName;
  }

  public TenantMember isOwner(Boolean isOwner) {
    this.isOwner = isOwner;
    return this;
  }

  /**
   * Get isOwner
   *
   * @return isOwner
   */
  @NotNull
  @Schema(name = "isOwner", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isOwner")
  public Boolean getIsOwner() {
    return isOwner;
  }

  @JsonProperty("isOwner")
  public void setIsOwner(Boolean isOwner) {
    this.isOwner = isOwner;
  }

  public TenantMember status(TenantMemberStatus status) {
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
  public TenantMemberStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(TenantMemberStatus status) {
    this.status = status;
  }

  public TenantMember createdAt(OffsetDateTime createdAt) {
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

  public TenantMember updatedAt(OffsetDateTime updatedAt) {
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
    TenantMember tenantMember = (TenantMember) o;
    return Objects.equals(this.id, tenantMember.id)
        && Objects.equals(this.tenantId, tenantMember.tenantId)
        && Objects.equals(this.userId, tenantMember.userId)
        && Objects.equals(this.memberName, tenantMember.memberName)
        && Objects.equals(this.isOwner, tenantMember.isOwner)
        && Objects.equals(this.status, tenantMember.status)
        && Objects.equals(this.createdAt, tenantMember.createdAt)
        && Objects.equals(this.updatedAt, tenantMember.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, userId, memberName, isOwner, status, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantMember {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    memberName: ").append(toIndentedString(memberName)).append("\n");
    sb.append("    isOwner: ").append(toIndentedString(isOwner)).append("\n");
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
