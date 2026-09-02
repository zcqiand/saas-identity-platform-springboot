package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;

/** Gets or Sets TenantStatus */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-01T23:20:59.484585600+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public enum TenantStatus {
  ACTIVE("active"),

  SUSPENDED("suspended"),

  ARCHIVED("archived");

  private final String value;

  TenantStatus(String value) {
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
  public static TenantStatus fromValue(String value) {
    for (TenantStatus b : TenantStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
