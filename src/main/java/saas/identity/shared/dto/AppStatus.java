package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;

/** Gets or Sets AppStatus */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-02T23:27:00.762429900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public enum AppStatus {
  ACTIVE("active"),

  DISABLED("disabled");

  private final String value;

  AppStatus(String value) {
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
  public static AppStatus fromValue(String value) {
    for (AppStatus b : AppStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
