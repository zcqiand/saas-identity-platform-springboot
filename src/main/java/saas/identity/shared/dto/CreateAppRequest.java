package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** CreateAppRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-13T19:43:32.481885100+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class CreateAppRequest {

  private String code;

  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String icon;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer sortOrder;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable AppStatus status;

  private String clientId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String clientSecret;

  private List<String> redirectUris = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> scopes = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<OAuthGrantType> grantTypes = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Boolean isFirstParty;

  public CreateAppRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public CreateAppRequest(String code, String name, String clientId, List<String> redirectUris) {
    this.code = code;
    this.name = name;
    this.clientId = clientId;
    this.redirectUris = redirectUris;
  }

  public CreateAppRequest code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   *
   * @return code
   */
  @NotNull
  @Size(min = 2, max = 64)
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public CreateAppRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @Size(min = 2, max = 255)
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  public CreateAppRequest description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   *
   * @return description
   */
  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public CreateAppRequest icon(@Nullable String icon) {
    this.icon = icon;
    return this;
  }

  /**
   * Get icon
   *
   * @return icon
   */
  @Schema(name = "icon", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("icon")
  public @Nullable String getIcon() {
    return icon;
  }

  @JsonProperty("icon")
  public void setIcon(@Nullable String icon) {
    this.icon = icon;
  }

  public CreateAppRequest sortOrder(@Nullable Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
   *
   * @return sortOrder
   */
  @Schema(name = "sortOrder", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sortOrder")
  public @Nullable Integer getSortOrder() {
    return sortOrder;
  }

  @JsonProperty("sortOrder")
  public void setSortOrder(@Nullable Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public CreateAppRequest status(@Nullable AppStatus status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public @Nullable AppStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(@Nullable AppStatus status) {
    this.status = status;
  }

  public CreateAppRequest clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   *
   * @return clientId
   */
  @NotNull
  @Size(min = 2, max = 128)
  @Schema(name = "clientId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public CreateAppRequest clientSecret(@Nullable String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  /**
   * Get clientSecret
   *
   * @return clientSecret
   */
  @Schema(name = "clientSecret", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clientSecret")
  public @Nullable String getClientSecret() {
    return clientSecret;
  }

  @JsonProperty("clientSecret")
  public void setClientSecret(@Nullable String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public CreateAppRequest redirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  public CreateAppRequest addRedirectUrisItem(String redirectUrisItem) {
    if (this.redirectUris == null) {
      this.redirectUris = new ArrayList<>();
    }
    this.redirectUris.add(redirectUrisItem);
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
  public List<String> getRedirectUris() {
    return redirectUris;
  }

  @JsonProperty("redirectUris")
  public void setRedirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
  }

  public CreateAppRequest scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public CreateAppRequest addScopesItem(String scopesItem) {
    if (this.scopes == null) {
      this.scopes = new ArrayList<>();
    }
    this.scopes.add(scopesItem);
    return this;
  }

  /**
   * Get scopes
   *
   * @return scopes
   */
  @Schema(name = "scopes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }

  @JsonProperty("scopes")
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public CreateAppRequest grantTypes(List<OAuthGrantType> grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  public CreateAppRequest addGrantTypesItem(OAuthGrantType grantTypesItem) {
    if (this.grantTypes == null) {
      this.grantTypes = new ArrayList<>();
    }
    this.grantTypes.add(grantTypesItem);
    return this;
  }

  /**
   * Get grantTypes
   *
   * @return grantTypes
   */
  @Valid
  @Schema(name = "grantTypes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grantTypes")
  public List<OAuthGrantType> getGrantTypes() {
    return grantTypes;
  }

  @JsonProperty("grantTypes")
  public void setGrantTypes(List<OAuthGrantType> grantTypes) {
    this.grantTypes = grantTypes;
  }

  public CreateAppRequest isFirstParty(@Nullable Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
    return this;
  }

  /**
   * Get isFirstParty
   *
   * @return isFirstParty
   */
  @Schema(name = "isFirstParty", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("isFirstParty")
  public @Nullable Boolean getIsFirstParty() {
    return isFirstParty;
  }

  @JsonProperty("isFirstParty")
  public void setIsFirstParty(@Nullable Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateAppRequest createAppRequest = (CreateAppRequest) o;
    return Objects.equals(this.code, createAppRequest.code)
        && Objects.equals(this.name, createAppRequest.name)
        && Objects.equals(this.description, createAppRequest.description)
        && Objects.equals(this.icon, createAppRequest.icon)
        && Objects.equals(this.sortOrder, createAppRequest.sortOrder)
        && Objects.equals(this.status, createAppRequest.status)
        && Objects.equals(this.clientId, createAppRequest.clientId)
        && Objects.equals(this.clientSecret, createAppRequest.clientSecret)
        && Objects.equals(this.redirectUris, createAppRequest.redirectUris)
        && Objects.equals(this.scopes, createAppRequest.scopes)
        && Objects.equals(this.grantTypes, createAppRequest.grantTypes)
        && Objects.equals(this.isFirstParty, createAppRequest.isFirstParty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        code,
        name,
        description,
        icon,
        sortOrder,
        status,
        clientId,
        clientSecret,
        redirectUris,
        scopes,
        grantTypes,
        isFirstParty);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateAppRequest {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
    sb.append("    sortOrder: ").append(toIndentedString(sortOrder)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
    sb.append("    redirectUris: ").append(toIndentedString(redirectUris)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    grantTypes: ").append(toIndentedString(grantTypes)).append("\n");
    sb.append("    isFirstParty: ").append(toIndentedString(isFirstParty)).append("\n");
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
