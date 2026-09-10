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
 * CreateOAuthClientRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-10T08:08:05.999133+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateOAuthClientRequest {

  private String clientId;

  private String clientName;

  private String clientSecret;

  private String grantTypes;

  private String redirectUris;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String scopes;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer accessTokenValidity;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer refreshTokenValidity;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Boolean autoApprove;

  public CreateOAuthClientRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateOAuthClientRequest(String clientId, String clientName, String clientSecret, String grantTypes, String redirectUris) {
    this.clientId = clientId;
    this.clientName = clientName;
    this.clientSecret = clientSecret;
    this.grantTypes = grantTypes;
    this.redirectUris = redirectUris;
  }

  public CreateOAuthClientRequest clientId(String clientId) {
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

  public CreateOAuthClientRequest clientName(String clientName) {
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

  public CreateOAuthClientRequest clientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  /**
   * Get clientSecret
   * @return clientSecret
   */
  @NotNull 
  @Schema(name = "clientSecret", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientSecret")
  public String getClientSecret() {
    return clientSecret;
  }

  @JsonProperty("clientSecret")
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public CreateOAuthClientRequest grantTypes(String grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  /**
   * Get grantTypes
   * @return grantTypes
   */
  @NotNull 
  @Schema(name = "grantTypes", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("grantTypes")
  public String getGrantTypes() {
    return grantTypes;
  }

  @JsonProperty("grantTypes")
  public void setGrantTypes(String grantTypes) {
    this.grantTypes = grantTypes;
  }

  public CreateOAuthClientRequest redirectUris(String redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  /**
   * Get redirectUris
   * @return redirectUris
   */
  @NotNull 
  @Schema(name = "redirectUris", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("redirectUris")
  public String getRedirectUris() {
    return redirectUris;
  }

  @JsonProperty("redirectUris")
  public void setRedirectUris(String redirectUris) {
    this.redirectUris = redirectUris;
  }

  public CreateOAuthClientRequest scopes(@Nullable String scopes) {
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

  public CreateOAuthClientRequest accessTokenValidity(@Nullable Integer accessTokenValidity) {
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

  public CreateOAuthClientRequest refreshTokenValidity(@Nullable Integer refreshTokenValidity) {
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

  public CreateOAuthClientRequest autoApprove(@Nullable Boolean autoApprove) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateOAuthClientRequest createOAuthClientRequest = (CreateOAuthClientRequest) o;
    return Objects.equals(this.clientId, createOAuthClientRequest.clientId) &&
        Objects.equals(this.clientName, createOAuthClientRequest.clientName) &&
        Objects.equals(this.clientSecret, createOAuthClientRequest.clientSecret) &&
        Objects.equals(this.grantTypes, createOAuthClientRequest.grantTypes) &&
        Objects.equals(this.redirectUris, createOAuthClientRequest.redirectUris) &&
        Objects.equals(this.scopes, createOAuthClientRequest.scopes) &&
        Objects.equals(this.accessTokenValidity, createOAuthClientRequest.accessTokenValidity) &&
        Objects.equals(this.refreshTokenValidity, createOAuthClientRequest.refreshTokenValidity) &&
        Objects.equals(this.autoApprove, createOAuthClientRequest.autoApprove);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, clientName, clientSecret, grantTypes, redirectUris, scopes, accessTokenValidity, refreshTokenValidity, autoApprove);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateOAuthClientRequest {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
    sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    accessTokenValidity: ").append(toIndentedString(accessTokenValidity)).append("\n");
    sb.append("    refreshTokenValidity: ").append(toIndentedString(refreshTokenValidity)).append("\n");
    sb.append("    autoApprove: ").append(toIndentedString(autoApprove)).append("\n");
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

