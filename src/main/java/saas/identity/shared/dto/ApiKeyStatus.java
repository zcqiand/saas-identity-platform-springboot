package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;

/** Gets or Sets ApiKeyStatus */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-31T17:07:11.941023200+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public enum ApiKeyStatus {
  ACTIVE("active"),

  REVOKED("revoked"),

  EXPIRED("expired");

  private final String value;

  ApiKeyStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ApiKeyStatus fromValue(String value) {
    for (ApiKeyStatus b : ApiKeyStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
