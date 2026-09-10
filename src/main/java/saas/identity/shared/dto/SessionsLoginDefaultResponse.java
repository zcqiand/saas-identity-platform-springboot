package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.ErrorResponse;
import saas.identity.shared.dto.LockedAccountResponse;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SessionsLoginDefaultResponse
 */

@JsonTypeName("Sessions_login_default_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-10T08:08:05.999133+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class SessionsLoginDefaultResponse {

  private String code;

  private String message;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime lockedUntil;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer remainingAttempts;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> details = new HashMap<>();

  public SessionsLoginDefaultResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SessionsLoginDefaultResponse(String code, String message, OffsetDateTime lockedUntil) {
    this.code = code;
    this.message = message;
    this.lockedUntil = lockedUntil;
  }

  public SessionsLoginDefaultResponse code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @NotNull 
  @Schema(name = "code", example = "BAD_REQUEST", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public SessionsLoginDefaultResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   */
  @NotNull 
  @Schema(name = "message", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  @JsonProperty("message")
  public void setMessage(String message) {
    this.message = message;
  }

  public SessionsLoginDefaultResponse lockedUntil(OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
    return this;
  }

  /**
   * Get lockedUntil
   * @return lockedUntil
   */
  @NotNull @Valid 
  @Schema(name = "lockedUntil", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("lockedUntil")
  public OffsetDateTime getLockedUntil() {
    return lockedUntil;
  }

  @JsonProperty("lockedUntil")
  public void setLockedUntil(OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public SessionsLoginDefaultResponse remainingAttempts(@Nullable Integer remainingAttempts) {
    this.remainingAttempts = remainingAttempts;
    return this;
  }

  /**
   * Get remainingAttempts
   * @return remainingAttempts
   */
  
  @Schema(name = "remainingAttempts", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("remainingAttempts")
  public @Nullable Integer getRemainingAttempts() {
    return remainingAttempts;
  }

  @JsonProperty("remainingAttempts")
  public void setRemainingAttempts(@Nullable Integer remainingAttempts) {
    this.remainingAttempts = remainingAttempts;
  }

  public SessionsLoginDefaultResponse details(Map<String, Object> details) {
    this.details = details;
    return this;
  }

  public SessionsLoginDefaultResponse putDetailsItem(String key, Object detailsItem) {
    if (this.details == null) {
      this.details = new HashMap<>();
    }
    this.details.put(key, detailsItem);
    return this;
  }

  /**
   * Get details
   * @return details
   */
  
  @Schema(name = "details", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("details")
  public Map<String, Object> getDetails() {
    return details;
  }

  @JsonProperty("details")
  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SessionsLoginDefaultResponse sessionsLoginDefaultResponse = (SessionsLoginDefaultResponse) o;
    return Objects.equals(this.code, sessionsLoginDefaultResponse.code) &&
        Objects.equals(this.message, sessionsLoginDefaultResponse.message) &&
        Objects.equals(this.lockedUntil, sessionsLoginDefaultResponse.lockedUntil) &&
        Objects.equals(this.remainingAttempts, sessionsLoginDefaultResponse.remainingAttempts) &&
        Objects.equals(this.details, sessionsLoginDefaultResponse.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, lockedUntil, remainingAttempts, details);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SessionsLoginDefaultResponse {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    lockedUntil: ").append(toIndentedString(lockedUntil)).append("\n");
    sb.append("    remainingAttempts: ").append(toIndentedString(remainingAttempts)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

