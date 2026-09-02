package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets AppStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-02T22:47:47.334506300+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
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

