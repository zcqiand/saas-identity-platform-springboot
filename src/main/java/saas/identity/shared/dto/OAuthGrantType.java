package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;

/** Gets or Sets OAuthGrantType */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-02T23:27:00.762429900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
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
