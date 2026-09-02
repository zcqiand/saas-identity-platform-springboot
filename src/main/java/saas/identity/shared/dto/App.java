package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

/** App */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-02T23:27:00.762429900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class App {

  private UUID id;

  private String code;

  private String name;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String description;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String icon;

  private Integer sortOrder;

  private AppStatus status;

  private String clientId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String clientSecret;

  private List<String> redirectUris = new ArrayList<>();

  private List<String> scopes = new ArrayList<>();

  private List<OAuthGrantType> grantTypes = new ArrayList<>();

  private Boolean isFirstParty;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdAt;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedAt;

  public App() {
    super();
  }

  /** Constructor with only required parameters */
  public App(
      UUID id,
      String code,
      String name,
      Integer sortOrder,
      AppStatus status,
      String clientId,
      List<String> redirectUris,
      List<String> scopes,
      List<OAuthGrantType> grantTypes,
      Boolean isFirstParty,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.sortOrder = sortOrder;
    this.status = status;
    this.clientId = clientId;
    this.redirectUris = redirectUris;
    this.scopes = scopes;
    this.grantTypes = grantTypes;
    this.isFirstParty = isFirstParty;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public App id(UUID id) {
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

  public App code(String code) {
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

  public App name(String name) {
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

  public App description(@Nullable String description) {
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

  public App icon(@Nullable String icon) {
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

  public App sortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  /**
   * Get sortOrder
   *
   * @return sortOrder
   */
  @NotNull
  @Schema(name = "sortOrder", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("sortOrder")
  public Integer getSortOrder() {
    return sortOrder;
  }

  @JsonProperty("sortOrder")
  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public App status(AppStatus status) {
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

  public App clientId(String clientId) {
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

  public App clientSecret(@Nullable String clientSecret) {
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

  public App redirectUris(List<String> redirectUris) {
    this.redirectUris = redirectUris;
    return this;
  }

  public App addRedirectUrisItem(String redirectUrisItem) {
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

  public App scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public App addScopesItem(String scopesItem) {
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
  @NotNull
  @Schema(name = "scopes", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }

  @JsonProperty("scopes")
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  public App grantTypes(List<OAuthGrantType> grantTypes) {
    this.grantTypes = grantTypes;
    return this;
  }

  public App addGrantTypesItem(OAuthGrantType grantTypesItem) {
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
  @NotNull
  @Valid
  @Schema(name = "grantTypes", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("grantTypes")
  public List<OAuthGrantType> getGrantTypes() {
    return grantTypes;
  }

  @JsonProperty("grantTypes")
  public void setGrantTypes(List<OAuthGrantType> grantTypes) {
    this.grantTypes = grantTypes;
  }

  public App isFirstParty(Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
    return this;
  }

  /**
   * Get isFirstParty
   *
   * @return isFirstParty
   */
  @NotNull
  @Schema(name = "isFirstParty", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("isFirstParty")
  public Boolean getIsFirstParty() {
    return isFirstParty;
  }

  @JsonProperty("isFirstParty")
  public void setIsFirstParty(Boolean isFirstParty) {
    this.isFirstParty = isFirstParty;
  }

  public App createdAt(OffsetDateTime createdAt) {
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

  public App updatedAt(OffsetDateTime updatedAt) {
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
    App app = (App) o;
    return Objects.equals(this.id, app.id)
        && Objects.equals(this.code, app.code)
        && Objects.equals(this.name, app.name)
        && Objects.equals(this.description, app.description)
        && Objects.equals(this.icon, app.icon)
        && Objects.equals(this.sortOrder, app.sortOrder)
        && Objects.equals(this.status, app.status)
        && Objects.equals(this.clientId, app.clientId)
        && Objects.equals(this.clientSecret, app.clientSecret)
        && Objects.equals(this.redirectUris, app.redirectUris)
        && Objects.equals(this.scopes, app.scopes)
        && Objects.equals(this.grantTypes, app.grantTypes)
        && Objects.equals(this.isFirstParty, app.isFirstParty)
        && Objects.equals(this.createdAt, app.createdAt)
        && Objects.equals(this.updatedAt, app.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
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
        isFirstParty,
        createdAt,
        updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class App {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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
