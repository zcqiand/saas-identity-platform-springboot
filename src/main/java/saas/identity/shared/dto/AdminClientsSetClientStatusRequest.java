package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** AdminClientsSetClientStatusRequest */
@JsonTypeName("AdminClients_setClientStatus_request")
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-12T08:51:22.603663900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class AdminClientsSetClientStatusRequest {

  private Integer status;

  public AdminClientsSetClientStatusRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public AdminClientsSetClientStatusRequest(Integer status) {
    this.status = status;
  }

  public AdminClientsSetClientStatusRequest status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @NotNull
  @Schema(name = "status", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("status")
  public Integer getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(Integer status) {
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
    AdminClientsSetClientStatusRequest adminClientsSetClientStatusRequest =
        (AdminClientsSetClientStatusRequest) o;
    return Objects.equals(this.status, adminClientsSetClientStatusRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdminClientsSetClientStatusRequest {\n");
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
