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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

/** UpdateTenantApplicationRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-10T10:23:40.344575800+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class UpdateTenantApplicationRequest {

  private Integer status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime expireTime;

  public UpdateTenantApplicationRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public UpdateTenantApplicationRequest(Integer status) {
    this.status = status;
  }

  public UpdateTenantApplicationRequest status(Integer status) {
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

  public UpdateTenantApplicationRequest expireTime(@Nullable OffsetDateTime expireTime) {
    this.expireTime = expireTime;
    return this;
  }

  /**
   * Get expireTime
   *
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateTenantApplicationRequest updateTenantApplicationRequest =
        (UpdateTenantApplicationRequest) o;
    return Objects.equals(this.status, updateTenantApplicationRequest.status)
        && Objects.equals(this.expireTime, updateTenantApplicationRequest.expireTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, expireTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateTenantApplicationRequest {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    expireTime: ").append(toIndentedString(expireTime)).append("\n");
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
