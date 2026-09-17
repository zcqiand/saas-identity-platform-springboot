package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.TenantMemberStatus;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantMembersChangeTenantUserStatusRequest
 */

@JsonTypeName("TenantMembers_changeTenantUserStatus_request")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T22:53:30.131396900+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantMembersChangeTenantUserStatusRequest {

  private TenantMemberStatus status;

  public TenantMembersChangeTenantUserStatusRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantMembersChangeTenantUserStatusRequest(TenantMemberStatus status) {
    this.status = status;
  }

  public TenantMembersChangeTenantUserStatusRequest status(TenantMemberStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @NotNull @Valid 
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public TenantMemberStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(TenantMemberStatus status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantMembersChangeTenantUserStatusRequest tenantMembersChangeTenantUserStatusRequest = (TenantMembersChangeTenantUserStatusRequest) o;
    return Objects.equals(this.status, tenantMembersChangeTenantUserStatusRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantMembersChangeTenantUserStatusRequest {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

