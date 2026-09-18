package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

/** OAuthClient */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-19T07:34:59.395972600+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class OAuthClient {

  private UUID id;

  private String clientId;

  private String clientName;

  private String grantTypes;

  private String redirectUris;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String scopes;

  private Integer accessTokenValidity;

  private Integer refreshTokenValidity;

  private Boolean autoApprove;

  private Integer status;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public OAuthClient() {
    super();
  }

  /** Constructor with only required parameters */
  public OAuthClient(
      UUID id,
      String clientId,
      String clientName,
      String grantTypes,
      String redirectUris,
      Integer accessTokenValidity,
      Integer refreshTokenValidity,
      Boolean autoApprove,
      Integer status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.clientId = clientId;
    this.clientName = clientName;
    this.grantTypes = grantTypes;
    this.redirectUris = redirectUris;
    this.accessTokenValidity = accessTokenValidity;
    this.refreshTokenValidity = refreshTokenValidity;
    this.autoApprove = autoApprove;
    this.status = status;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public OAuthClient id(UUID id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   *
   * @return id
   */
  @NotNull
  @Valid
  @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(UUID id) {
    this.id = id;
  }

  public OAuthClient clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   *
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

  public OAuthClient clientName(String clientName) {
    this.clientName = clientName;
    return this;
  }

  /**
   * Get clientName
   *
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

  public OAuthClient grantTypes(String grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  /**
   * Get grantTypes
   *
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

  public OAuthClient redirectUris(String redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  /**
   * Get redirectUris
   *
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

  public OAuthClient scopes(@Nullable String scopes) {
    this.scopes = scopes;
    return this;
  }

  /**
   * Get scopes
   *
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

  public OAuthClient accessTokenValidity(Integer accessTokenValidity) {
    this.accessTokenValidity = accessTokenValidity;
    return this;
  }

  /**
   * Get accessTokenValidity
   *
   * @return accessTokenValidity
   */
  @NotNull
  @Schema(name = "accessTokenValidity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("accessTokenValidity")
  public Integer getAccessTokenValidity() {
    return accessTokenValidity;
  }

  @JsonProperty("accessTokenValidity")
  public void setAccessTokenValidity(Integer accessTokenValidity) {
    this.accessTokenValidity = accessTokenValidity;
  }

  public OAuthClient refreshTokenValidity(Integer refreshTokenValidity) {
    this.refreshTokenValidity = refreshTokenValidity;
    return this;
  }

  /**
   * Get refreshTokenValidity
   *
   * @return refreshTokenValidity
   */
  @NotNull
  @Schema(name = "refreshTokenValidity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("refreshTokenValidity")
  public Integer getRefreshTokenValidity() {
    return refreshTokenValidity;
  }

  @JsonProperty("refreshTokenValidity")
  public void setRefreshTokenValidity(Integer refreshTokenValidity) {
    this.refreshTokenValidity = refreshTokenValidity;
  }

  public OAuthClient autoApprove(Boolean autoApprove) {
    this.autoApprove = autoApprove;
    return this;
  }

  /**
   * Get autoApprove
   *
   * @return autoApprove
   */
  @NotNull
  @Schema(name = "autoApprove", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("autoApprove")
  public Boolean getAutoApprove() {
    return autoApprove;
  }

  @JsonProperty("autoApprove")
  public void setAutoApprove(Boolean autoApprove) {
    this.autoApprove = autoApprove;
  }

  public OAuthClient status(Integer status) {
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

  public OAuthClient createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   *
   * @return createdAt
   */
  @NotNull
  @Valid
  @Schema(name = "createdAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("createdAt")
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OAuthClient updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   *
   * @return updatedAt
   */
  @NotNull
  @Valid
  @Schema(name = "updatedAt", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updatedAt")
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OAuthClient oauthClient = (OAuthClient) o;
    return Objects.equals(this.id, oauthClient.id)
        && Objects.equals(this.clientId, oauthClient.clientId)
        && Objects.equals(this.clientName, oauthClient.clientName)
        && Objects.equals(this.grantTypes, oauthClient.grantTypes)
        && Objects.equals(this.redirectUris, oauthClient.redirectUris)
        && Objects.equals(this.scopes, oauthClient.scopes)
        && Objects.equals(this.accessTokenValidity, oauthClient.accessTokenValidity)
        && Objects.equals(this.refreshTokenValidity, oauthClient.refreshTokenValidity)
        && Objects.equals(this.autoApprove, oauthClient.autoApprove)
        && Objects.equals(this.status, oauthClient.status)
        && Objects.equals(this.createdAt, oauthClient.createdAt)
        && Objects.equals(this.updatedAt, oauthClient.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        clientId,
        clientName,
        grantTypes,
        redirectUris,
        scopes,
        accessTokenValidity,
        refreshTokenValidity,
        autoApprove,
        status,
        createdAt,
        updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OAuthClient {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientName: ").append(toIndentedString(clientName)).append("\n");
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
    sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    accessTokenValidity: ")
        .append(toIndentedString(accessTokenValidity))
        .append("\n");
    sb.append("    refreshTokenValidity: ")
        .append(toIndentedString(refreshTokenValidity))
        .append("\n");
    sb.append("    autoApprove: ").append(toIndentedString(autoApprove)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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
