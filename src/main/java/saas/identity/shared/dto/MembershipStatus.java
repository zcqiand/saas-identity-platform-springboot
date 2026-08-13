package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;

/** Gets or Sets MembershipStatus */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-13T19:43:32.481885100+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public enum MembershipStatus {
  ACTIVE("active"),

  INVITED("invited"),

  REMOVED("removed");

  private final String value;

  MembershipStatus(String value) {
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
  public static MembershipStatus fromValue(String value) {
    for (MembershipStatus b : MembershipStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
