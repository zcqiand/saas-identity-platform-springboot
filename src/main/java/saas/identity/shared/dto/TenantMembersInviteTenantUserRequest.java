package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantMembersInviteTenantUserRequest
 */

@JsonTypeName("TenantMembers_inviteTenantUser_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T22:53:30.131396900+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantMembersInviteTenantUserRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String email;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String mobile;

  public TenantMembersInviteTenantUserRequest email(@Nullable String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
   */
  
  @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("email")
  public @Nullable String getEmail() {
    return email;
  }

  @JsonProperty("email")
  public void setEmail(@Nullable String email) {
    this.email = email;
  }

  public TenantMembersInviteTenantUserRequest mobile(@Nullable String mobile) {
    this.mobile = mobile;
    return this;
  }

  /**
   * Get mobile
   * @return mobile
   */
  
  @Schema(name = "mobile", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mobile")
  public @Nullable String getMobile() {
    return mobile;
  }

  @JsonProperty("mobile")
  public void setMobile(@Nullable String mobile) {
    this.mobile = mobile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantMembersInviteTenantUserRequest tenantMembersInviteTenantUserRequest = (TenantMembersInviteTenantUserRequest) o;
    return Objects.equals(this.email, tenantMembersInviteTenantUserRequest.email) &&
        Objects.equals(this.mobile, tenantMembersInviteTenantUserRequest.mobile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, mobile);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantMembersInviteTenantUserRequest {\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    mobile: ").append(toIndentedString(mobile)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

