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

/** LockedAccountResponse */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-10T02:05:25.361968600+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class LockedAccountResponse {

  private String code;

  private String message;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime lockedUntil;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer remainingAttempts;

  public LockedAccountResponse() {
    super();
  }

  /** Constructor with only required parameters */
  public LockedAccountResponse(String code, String message, OffsetDateTime lockedUntil) {
    this.code = code;
    this.message = message;
    this.lockedUntil = lockedUntil;
  }

  public LockedAccountResponse code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   *
   * @return code
   */
  @NotNull
  @Schema(name = "code", example = "ACCOUNT_LOCKED", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public LockedAccountResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   *
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

  public LockedAccountResponse lockedUntil(OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
    return this;
  }

  /**
   * Get lockedUntil
   *
   * @return lockedUntil
   */
  @NotNull
  @Valid
  @Schema(name = "lockedUntil", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("lockedUntil")
  public OffsetDateTime getLockedUntil() {
    return lockedUntil;
  }

  @JsonProperty("lockedUntil")
  public void setLockedUntil(OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
  }

  public LockedAccountResponse remainingAttempts(@Nullable Integer remainingAttempts) {
    this.remainingAttempts = remainingAttempts;
    return this;
  }

  /**
   * Get remainingAttempts
   *
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LockedAccountResponse lockedAccountResponse = (LockedAccountResponse) o;
    return Objects.equals(this.code, lockedAccountResponse.code)
        && Objects.equals(this.message, lockedAccountResponse.message)
        && Objects.equals(this.lockedUntil, lockedAccountResponse.lockedUntil)
        && Objects.equals(this.remainingAttempts, lockedAccountResponse.remainingAttempts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, lockedUntil, remainingAttempts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LockedAccountResponse {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    lockedUntil: ").append(toIndentedString(lockedUntil)).append("\n");
    sb.append("    remainingAttempts: ").append(toIndentedString(remainingAttempts)).append("\n");
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
