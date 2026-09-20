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
import java.util.UUID;
import org.springframework.lang.Nullable;

/** LoginResponse */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-20T12:05:54.325061900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class LoginResponse {

  private SysUser user;

  private List<@Valid TenantMembership> availableTenants;

  private UUID userId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID currentTenantId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String accessToken;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String refreshToken;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String tokenType;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable Integer expiresIn;

  private String clientId;

  public LoginResponse() {
    super();
  }

  /** Constructor with only required parameters */
  public LoginResponse(
      SysUser user, List<@Valid TenantMembership> availableTenants, UUID userId, String clientId) {
    this.user = user;
    this.availableTenants = availableTenants;
    this.userId = userId;
    this.clientId = clientId;
  }

  public LoginResponse user(SysUser user) {
    this.user = user;
    return this;
  }

  /**
   * Get user
   *
   * @return user
   */
  @NotNull
  @Valid
  @Schema(name = "user", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("user")
  public SysUser getUser() {
    return user;
  }

  @JsonProperty("user")
  public void setUser(SysUser user) {
    this.user = user;
  }

  public LoginResponse availableTenants(List<@Valid TenantMembership> availableTenants) {
    this.availableTenants = availableTenants;
    return this;
  }

  public LoginResponse addAvailableTenantsItem(TenantMembership availableTenantsItem) {
    if (this.availableTenants == null) {
      this.availableTenants = new ArrayList<>();
    }
    this.availableTenants.add(availableTenantsItem);
    return this;
  }

  /**
   * Get availableTenants
   *
   * @return availableTenants
   */
  @NotNull
  @Valid
  @Schema(name = "availableTenants", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("availableTenants")
  public List<@Valid TenantMembership> getAvailableTenants() {
    return availableTenants;
  }

  @JsonProperty("availableTenants")
  public void setAvailableTenants(List<@Valid TenantMembership> availableTenants) {
    this.availableTenants = availableTenants;
  }

  public LoginResponse userId(UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   * Get userId
   *
   * @return userId
   */
  @NotNull
  @Valid
  @Schema(name = "userId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("userId")
  public UUID getUserId() {
    return userId;
  }

  @JsonProperty("userId")
  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public LoginResponse currentTenantId(@Nullable UUID currentTenantId) {
    this.currentTenantId = currentTenantId;
    return this;
  }

  /**
   * Get currentTenantId
   *
   * @return currentTenantId
   */
  @Valid
  @Schema(name = "currentTenantId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currentTenantId")
  public @Nullable UUID getCurrentTenantId() {
    return currentTenantId;
  }

  @JsonProperty("currentTenantId")
  public void setCurrentTenantId(@Nullable UUID currentTenantId) {
    this.currentTenantId = currentTenantId;
  }

  public LoginResponse accessToken(@Nullable String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

  /**
   * Get accessToken
   *
   * @return accessToken
   */
  @Schema(name = "accessToken", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("accessToken")
  public @Nullable String getAccessToken() {
    return accessToken;
  }

  @JsonProperty("accessToken")
  public void setAccessToken(@Nullable String accessToken) {
    this.accessToken = accessToken;
  }

  public LoginResponse refreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  /**
   * Get refreshToken
   *
   * @return refreshToken
   */
  @Schema(name = "refreshToken", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("refreshToken")
  public @Nullable String getRefreshToken() {
    return refreshToken;
  }

  @JsonProperty("refreshToken")
  public void setRefreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public LoginResponse tokenType(@Nullable String tokenType) {
    this.tokenType = tokenType;
    return this;
  }

  /**
   * Get tokenType
   *
   * @return tokenType
   */
  @Schema(name = "tokenType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tokenType")
  public @Nullable String getTokenType() {
    return tokenType;
  }

  @JsonProperty("tokenType")
  public void setTokenType(@Nullable String tokenType) {
    this.tokenType = tokenType;
  }

  public LoginResponse expiresIn(@Nullable Integer expiresIn) {
    this.expiresIn = expiresIn;
    return this;
  }

  /**
   * Get expiresIn
   *
   * @return expiresIn
   */
  @Schema(name = "expiresIn", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("expiresIn")
  public @Nullable Integer getExpiresIn() {
    return expiresIn;
  }

  @JsonProperty("expiresIn")
  public void setExpiresIn(@Nullable Integer expiresIn) {
    this.expiresIn = expiresIn;
  }

  public LoginResponse clientId(String clientId) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoginResponse loginResponse = (LoginResponse) o;
    return Objects.equals(this.user, loginResponse.user)
        && Objects.equals(this.availableTenants, loginResponse.availableTenants)
        && Objects.equals(this.userId, loginResponse.userId)
        && Objects.equals(this.currentTenantId, loginResponse.currentTenantId)
        && Objects.equals(this.accessToken, loginResponse.accessToken)
        && Objects.equals(this.refreshToken, loginResponse.refreshToken)
        && Objects.equals(this.tokenType, loginResponse.tokenType)
        && Objects.equals(this.expiresIn, loginResponse.expiresIn)
        && Objects.equals(this.clientId, loginResponse.clientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        user,
        availableTenants,
        userId,
        currentTenantId,
        accessToken,
        refreshToken,
        tokenType,
        expiresIn,
        clientId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoginResponse {\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    availableTenants: ").append(toIndentedString(availableTenants)).append("\n");
    sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
    sb.append("    currentTenantId: ").append(toIndentedString(currentTenantId)).append("\n");
    sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
    sb.append("    refreshToken: ").append(toIndentedString(refreshToken)).append("\n");
    sb.append("    tokenType: ").append(toIndentedString(tokenType)).append("\n");
    sb.append("    expiresIn: ").append(toIndentedString(expiresIn)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
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
