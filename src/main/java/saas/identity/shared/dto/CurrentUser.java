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

/** CurrentUser */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-09T23:05:30.488412700+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class CurrentUser {

  private SysUser user;

  private List<@Valid TenantMember> memberships = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable UUID currentTenantId;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String clientId;

  public CurrentUser() {
    super();
  }

  /** Constructor with only required parameters */
  public CurrentUser(SysUser user, List<@Valid TenantMember> memberships) {
    this.user = user;
    this.memberships = memberships;
  }

  public CurrentUser user(SysUser user) {
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

  public CurrentUser memberships(List<@Valid TenantMember> memberships) {
    this.memberships = memberships;
    return this;
  }

  public CurrentUser addMembershipsItem(TenantMember membershipsItem) {
    if (this.memberships == null) {
      this.memberships = new ArrayList<>();
    }
    this.memberships.add(membershipsItem);
    return this;
  }

  /**
   * Get memberships
   *
   * @return memberships
   */
  @NotNull
  @Valid
  @Schema(name = "memberships", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("memberships")
  public List<@Valid TenantMember> getMemberships() {
    return memberships;
  }

  @JsonProperty("memberships")
  public void setMemberships(List<@Valid TenantMember> memberships) {
    this.memberships = memberships;
  }

  public CurrentUser currentTenantId(@Nullable UUID currentTenantId) {
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

  public CurrentUser clientId(@Nullable String clientId) {
    this.clientId = clientId;
    return this;
  }

  /**
   * Get clientId
   *
   * @return clientId
   */
  @Schema(name = "clientId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("clientId")
  public @Nullable String getClientId() {
    return clientId;
  }

  @JsonProperty("clientId")
  public void setClientId(@Nullable String clientId) {
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
    CurrentUser currentUser = (CurrentUser) o;
    return Objects.equals(this.user, currentUser.user)
        && Objects.equals(this.memberships, currentUser.memberships)
        && Objects.equals(this.currentTenantId, currentUser.currentTenantId)
        && Objects.equals(this.clientId, currentUser.clientId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, memberships, currentTenantId, clientId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CurrentUser {\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    memberships: ").append(toIndentedString(memberships)).append("\n");
    sb.append("    currentTenantId: ").append(toIndentedString(currentTenantId)).append("\n");
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
