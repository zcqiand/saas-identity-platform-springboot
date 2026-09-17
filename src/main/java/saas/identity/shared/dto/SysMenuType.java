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
 * Gets or Sets SysMenuType
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T01:38:48.840222800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public enum SysMenuType {
  
  DIRECTORY("directory"),
  
  MENU("menu"),
  
  BUTTON("button");

  private final String value;

  SysMenuType(String value) {
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
  public static SysMenuType fromValue(String value) {
    for (SysMenuType b : SysMenuType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

