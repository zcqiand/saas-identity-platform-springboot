package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** LoginRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class LoginRequest {

  private String username;

  private String password;

  private String clientId;

  public LoginRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public LoginRequest(String username, String password, String clientId) {
    this.username = username;
    this.password = password;
    this.clientId = clientId;
  }

  public LoginRequest username(String username) {
    this.username = username;
    return this;
  }

  /**
   * Get username
   *
   * @return username
   */
  @NotNull
  @Size(min = 1, max = 64)
  @Schema(name = "username", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("username")
  public void setUsername(String username) {
    this.username = username;
  }

  public LoginRequest password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Get password
   *
   * @return password
   */
  @NotNull
  @Size(min = 1, max = 128)
  @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
  }

  public LoginRequest clientId(String clientId) {
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
    LoginRequest loginRequest = (LoginRequest) o;
    return Objects.equals(this.username, loginRequest.username)
        && Objects.equals(this.password, loginRequest.password)
        && Objects.equals(this.clientId, loginRequest.clientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, clientId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoginRequest {\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    password: ").append(toIndentedString(password)).append("\n");
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
