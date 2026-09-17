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
 * CreateTenantRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T01:38:48.840222800+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class CreateTenantRequest {

  private String tenantKey;

  private String name;

  public CreateTenantRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateTenantRequest(String tenantKey, String name) {
    this.tenantKey = tenantKey;
    this.name = name;
  }

  public CreateTenantRequest tenantKey(String tenantKey) {
    this.tenantKey = tenantKey;
    return this;
  }

  /**
   * Get tenantKey
   * @return tenantKey
   */
  @NotNull @Size(min = 2, max = 64) 
  @Schema(name = "tenantKey", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tenantKey")
  public String getTenantKey() {
    return tenantKey;
  }

  @JsonProperty("tenantKey")
  public void setTenantKey(String tenantKey) {
    this.tenantKey = tenantKey;
  }

  public CreateTenantRequest name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   */
  @NotNull @Size(min = 2, max = 128) 
  @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateTenantRequest createTenantRequest = (CreateTenantRequest) o;
    return Objects.equals(this.tenantKey, createTenantRequest.tenantKey) &&
        Objects.equals(this.name, createTenantRequest.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantKey, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateTenantRequest {\n");
    sb.append("    tenantKey: ").append(toIndentedString(tenantKey)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

