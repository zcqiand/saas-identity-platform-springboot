package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** AdminAppsSetAppStatusRequest */
@JsonTypeName("AdminApps_setAppStatus_request")
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-28T16:38:49.530507900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class AdminAppsSetAppStatusRequest {

  private AppStatus status;

  public AdminAppsSetAppStatusRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public AdminAppsSetAppStatusRequest(AppStatus status) {
    this.status = status;
  }

  public AdminAppsSetAppStatusRequest status(AppStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @NotNull
  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public AppStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(AppStatus status) {
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
    AdminAppsSetAppStatusRequest adminAppsSetAppStatusRequest = (AdminAppsSetAppStatusRequest) o;
    return Objects.equals(this.status, adminAppsSetAppStatusRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdminAppsSetAppStatusRequest {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
