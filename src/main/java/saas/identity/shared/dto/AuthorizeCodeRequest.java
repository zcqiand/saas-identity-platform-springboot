package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * AuthorizeCodeRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-10T15:16:10.537909700+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class AuthorizeCodeRequest {

  private String clientId;

  private String redirectUri;

  /**
   * Gets or Sets responseType
   */
  public enum ResponseTypeEnum {
    CODE("code");

    private final String value;

    ResponseTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ResponseTypeEnum fromValue(String value) {
      for (ResponseTypeEnum b : ResponseTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private ResponseTypeEnum responseType;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String scope;

  private String state;

  public AuthorizeCodeRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AuthorizeCodeRequest(String clientId, String redirectUri, ResponseTypeEnum responseType, String state) {
    this.clientId = clientId;
    this.redirectUri = redirectUri;
    this.responseType = responseType;
    this.state = state;
  }

  public AuthorizeCodeRequest clientId(String clientId) {
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

  public AuthorizeCodeRequest redirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

  /**
   * Get redirectUri
   * @return redirectUri
   */
  @NotNull @Size(min = 1, max = 500) 
  @Schema(name = "redirectUri", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("redirectUri")
  public String getRedirectUri() {
    return redirectUri;
  }

  @JsonProperty("redirectUri")
  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public AuthorizeCodeRequest responseType(ResponseTypeEnum responseType) {
    this.responseType = responseType;
    return this;
  }

  /**
   * Get responseType
   * @return responseType
   */
  @NotNull 
  @Schema(name = "responseType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("responseType")
  public ResponseTypeEnum getResponseType() {
    return responseType;
  }

  @JsonProperty("responseType")
  public void setResponseType(ResponseTypeEnum responseType) {
    this.responseType = responseType;
  }

  public AuthorizeCodeRequest scope(@Nullable String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   * @return scope
   */
  
  @Schema(name = "scope", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scope")
  public @Nullable String getScope() {
    return scope;
  }

  @JsonProperty("scope")
  public void setScope(@Nullable String scope) {
    this.scope = scope;
  }

  public AuthorizeCodeRequest state(String state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
   */
  @NotNull 
  @Schema(name = "state", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("state")
  public String getState() {
    return state;
  }

  @JsonProperty("state")
  public void setState(String state) {
    this.state = state;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuthorizeCodeRequest authorizeCodeRequest = (AuthorizeCodeRequest) o;
    return Objects.equals(this.clientId, authorizeCodeRequest.clientId) &&
        Objects.equals(this.redirectUri, authorizeCodeRequest.redirectUri) &&
        Objects.equals(this.responseType, authorizeCodeRequest.responseType) &&
        Objects.equals(this.scope, authorizeCodeRequest.scope) &&
        Objects.equals(this.state, authorizeCodeRequest.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, redirectUri, responseType, scope, state);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorizeCodeRequest {\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
    sb.append("    responseType: ").append(toIndentedString(responseType)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
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

