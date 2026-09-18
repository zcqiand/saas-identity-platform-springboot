package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
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
 * SubscribeTenantApplicationRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-19T07:34:59.395972600+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class SubscribeTenantApplicationRequest {

  private String clientId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime expireTime;

  public SubscribeTenantApplicationRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SubscribeTenantApplicationRequest(String clientId) {
    this.clientId = clientId;
  }

  public SubscribeTenantApplicationRequest clientId(String clientId) {
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

  public SubscribeTenantApplicationRequest expireTime(@Nullable OffsetDateTime expireTime) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscribeTenantApplicationRequest subscribeTenantApplicationRequest = (SubscribeTenantApplicationRequest) o;
    return Objects.equals(this.clientId, subscribeTenantApplicationRequest.clientId) &&
        Objects.equals(this.expireTime, subscribeTenantApplicationRequest.expireTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, expireTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscribeTenantApplicationRequest {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    expireTime: ").append(toIndentedString(expireTime)).append("\n");
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

