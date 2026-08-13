package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** OAuthAuthorize200Response */
@JsonTypeName("OAuth_authorize_200_response")
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-13T23:52:54.053972+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class OAuthAuthorize200Response {

  private String code;

  private String state;

  public OAuthAuthorize200Response() {
    super();
  }

  /** Constructor with only required parameters */
  public OAuthAuthorize200Response(String code, String state) {
    this.code = code;
    this.state = state;
  }

  public OAuthAuthorize200Response code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   *
   * @return code
   */
  @NotNull
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public String getCode() {
    return code;
  }

  @JsonProperty("code")
  public void setCode(String code) {
    this.code = code;
  }

  public OAuthAuthorize200Response state(String state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   *
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
    OAuthAuthorize200Response oauthAuthorize200Response = (OAuthAuthorize200Response) o;
    return Objects.equals(this.code, oauthAuthorize200Response.code)
        && Objects.equals(this.state, oauthAuthorize200Response.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, state);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OAuthAuthorize200Response {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
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
