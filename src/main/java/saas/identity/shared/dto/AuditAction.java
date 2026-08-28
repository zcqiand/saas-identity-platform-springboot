package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;

/** Gets or Sets AuditAction */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-28T16:38:49.530507900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public enum AuditAction {
  USER_CREATED("user_created"),

  USER_UPDATED("user_updated"),

  USER_DELETED("user_deleted"),

  ROLE_ASSIGNED("role_assigned"),

  ROLE_REVOKED("role_revoked"),

  LOGIN_SUCCESS("login_success"),

  LOGIN_FAILED("login_failed"),

  OAUTH_TOKEN_ISSUED("oauth_token_issued"),

  API_KEY_CREATED("api_key_created"),

  API_KEY_REVOKED("api_key_revoked");

  private final String value;

  AuditAction(String value) {
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
  public static AuditAction fromValue(String value) {
    for (AuditAction b : AuditAction.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
