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
 * Gets or Sets SysUserStatus
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public enum SysUserStatus {
  
  ACTIVE("active"),
  
  DISABLED("disabled");

  private final String value;

  SysUserStatus(String value) {
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
  public static SysUserStatus fromValue(String value) {
    for (SysUserStatus b : SysUserStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

