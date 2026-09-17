package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * UpdateOAuthClientRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T22:53:30.131396900+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class UpdateOAuthClientRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String clientName;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String grantTypes;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String redirectUris;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String scopes;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer accessTokenValidity;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer refreshTokenValidity;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Boolean autoApprove;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer status;

  public UpdateOAuthClientRequest clientName(@Nullable String clientName) {
    this.clientName = clientName;
    return this;
  }

  /**
   * Get clientName
   * @return clientName
   */
  
  @Schema(name = "clientName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clientName")
  public @Nullable String getClientName() {
    return clientName;
  }

  @JsonProperty("clientName")
  public void setClientName(@Nullable String clientName) {
    this.clientName = clientName;
  }

  public UpdateOAuthClientRequest grantTypes(@Nullable String grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  /**
   * Get grantTypes
   * @return grantTypes
   */
  
  @Schema(name = "grantTypes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grantTypes")
  public @Nullable String getGrantTypes() {
    return grantTypes;
  }

  @JsonProperty("grantTypes")
  public void setGrantTypes(@Nullable String grantTypes) {
    this.grantTypes = grantTypes;
  }

  public UpdateOAuthClientRequest redirectUris(@Nullable String redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  /**
   * Get redirectUris
   * @return redirectUris
   */
  
  @Schema(name = "redirectUris", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("redirectUris")
  public @Nullable String getRedirectUris() {
    return redirectUris;
  }

  @JsonProperty("redirectUris")
  public void setRedirectUris(@Nullable String redirectUris) {
    this.redirectUris = redirectUris;
  }

  public UpdateOAuthClientRequest scopes(@Nullable String scopes) {
    this.scopes = scopes;
    return this;
  }

  /**
   * Get scopes
   * @return scopes
   */
  
  @Schema(name = "scopes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scopes")
  public @Nullable String getScopes() {
    return scopes;
  }

  @JsonProperty("scopes")
  public void setScopes(@Nullable String scopes) {
    this.scopes = scopes;
  }

  public UpdateOAuthClientRequest accessTokenValidity(@Nullable Integer accessTokenValidity) {
    this.accessTokenValidity = accessTokenValidity;
    return this;
  }

  /**
   * Get accessTokenValidity
   * @return accessTokenValidity
   */
  
  @Schema(name = "accessTokenValidity", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("accessTokenValidity")
  public @Nullable Integer getAccessTokenValidity() {
    return accessTokenValidity;
  }

  @JsonProperty("accessTokenValidity")
  public void setAccessTokenValidity(@Nullable Integer accessTokenValidity) {
    this.accessTokenValidity = accessTokenValidity;
  }

  public UpdateOAuthClientRequest refreshTokenValidity(@Nullable Integer refreshTokenValidity) {
    this.refreshTokenValidity = refreshTokenValidity;
    return this;
  }

  /**
   * Get refreshTokenValidity
   * @return refreshTokenValidity
   */
  
  @Schema(name = "refreshTokenValidity", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("refreshTokenValidity")
  public @Nullable Integer getRefreshTokenValidity() {
    return refreshTokenValidity;
  }

  @JsonProperty("refreshTokenValidity")
  public void setRefreshTokenValidity(@Nullable Integer refreshTokenValidity) {
    this.refreshTokenValidity = refreshTokenValidity;
  }

  public UpdateOAuthClientRequest autoApprove(@Nullable Boolean autoApprove) {
    this.autoApprove = autoApprove;
    return this;
  }

  /**
   * Get autoApprove
   * @return autoApprove
   */
  
  @Schema(name = "autoApprove", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("autoApprove")
  public @Nullable Boolean getAutoApprove() {
    return autoApprove;
  }

  @JsonProperty("autoApprove")
  public void setAutoApprove(@Nullable Boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public UpdateOAuthClientRequest status(@Nullable Integer status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   */
  
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable Integer getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable Integer status) {
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
    UpdateOAuthClientRequest updateOAuthClientRequest = (UpdateOAuthClientRequest) o;
    return Objects.equals(this.clientName, updateOAuthClientRequest.clientName) &&
        Objects.equals(this.grantTypes, updateOAuthClientRequest.grantTypes) &&
        Objects.equals(this.redirectUris, updateOAuthClientRequest.redirectUris) &&
        Objects.equals(this.scopes, updateOAuthClientRequest.scopes) &&
        Objects.equals(this.accessTokenValidity, updateOAuthClientRequest.accessTokenValidity) &&
        Objects.equals(this.refreshTokenValidity, updateOAuthClientRequest.refreshTokenValidity) &&
        Objects.equals(this.autoApprove, updateOAuthClientRequest.autoApprove) &&
        Objects.equals(this.status, updateOAuthClientRequest.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientName, grantTypes, redirectUris, scopes, accessTokenValidity, refreshTokenValidity, autoApprove, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateOAuthClientRequest {\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
    sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    accessTokenValidity: ").append(toIndentedString(accessTokenValidity)).append("\n");
    sb.append("    refreshTokenValidity: ").append(toIndentedString(refreshTokenValidity)).append("\n");
    sb.append("    autoApprove: ").append(toIndentedString(autoApprove)).append("\n");
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

