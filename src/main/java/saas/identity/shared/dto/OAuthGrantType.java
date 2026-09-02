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
 * Gets or Sets OAuthGrantType
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-02T22:47:47.334506300+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public enum OAuthGrantType {
  
  AUTHORIZATION_CODE("authorization_code"),
  
  REFRESH_TOKEN("refresh_token"),
  
  CLIENT_CREDENTIALS("client_credentials"),
  
  PASSWORD("password");

  private final String value;

  OAuthGrantType(String value) {
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
  public static OAuthGrantType fromValue(String value) {
    for (OAuthGrantType b : OAuthGrantType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

