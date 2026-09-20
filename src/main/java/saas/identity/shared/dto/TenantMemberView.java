package saas.identity.shared.dto;

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

/** TenantMemberView */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-09-20T12:05:54.325061900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class TenantMemberView {

  private TenantMember member;

  private SysUser user;

  private List<String> roles;

  public TenantMemberView() {
    super();
  }

  /** Constructor with only required parameters */
  public TenantMemberView(TenantMember member, SysUser user, List<String> roles) {
    this.member = member;
    this.user = user;
    this.roles = roles;
  }

  public TenantMemberView member(TenantMember member) {
    this.member = member;
    return this;
  }

  /**
   * Get member
   *
   * @return member
   */
  @NotNull
  @Valid
  @Schema(name = "member", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("member")
  public TenantMember getMember() {
    return member;
  }

  @JsonProperty("member")
  public void setMember(TenantMember member) {
    this.member = member;
  }

  public TenantMemberView user(SysUser user) {
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

  public TenantMemberView roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  public TenantMemberView addRolesItem(String rolesItem) {
    if (this.roles == null) {
      this.roles = new ArrayList<>();
    }
    this.roles.add(rolesItem);
    return this;
  }

  /**
   * Get roles
   *
   * @return roles
   */
  @NotNull
  @Schema(name = "roles", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }

  @JsonProperty("roles")
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantMemberView tenantMemberView = (TenantMemberView) o;
    return Objects.equals(this.member, tenantMemberView.member)
        && Objects.equals(this.user, tenantMemberView.user)
        && Objects.equals(this.roles, tenantMemberView.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(member, user, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantMemberView {\n");
    sb.append("    member: ").append(toIndentedString(member)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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
