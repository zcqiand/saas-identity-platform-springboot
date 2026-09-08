package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * OAuthClientPublicInfo
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class OAuthClientPublicInfo {

  private String clientId;

  private String clientName;

  private Integer status;

  public OAuthClientPublicInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public OAuthClientPublicInfo(String clientId, String clientName, Integer status) {
    this.clientId = clientId;
    this.clientName = clientName;
    this.status = status;
  }

  public OAuthClientPublicInfo clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   * @return clientId
   */
  @NotNull 
  @Schema(name = "clientId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public OAuthClientPublicInfo clientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  /**
   * Get clientName
   * @return clientName
   */
  @NotNull 
  @Schema(name = "clientName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientName")
  public String getClientName() {
    return clientName;
  }

  @JsonProperty("clientName")
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public OAuthClientPublicInfo status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
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
    OAuthClientPublicInfo oauthClientPublicInfo = (OAuthClientPublicInfo) o;
    return Objects.equals(this.clientId, oauthClientPublicInfo.clientId) &&
        Objects.equals(this.clientName, oauthClientPublicInfo.clientName) &&
        Objects.equals(this.status, oauthClientPublicInfo.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, clientName, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OAuthClientPublicInfo {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
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

