package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** AdminAppMenusMoveMenuRequest */
@JsonTypeName("AdminAppMenus_moveMenu_request")
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-13T23:52:54.053972+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class AdminAppMenusMoveMenuRequest {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private @Nullable String parentId;

  public AdminAppMenusMoveMenuRequest parentId(@Nullable String parentId) {
    this.parentId = parentId;
    return this;
  }

  /**
   * Get parentId
   *
   * @return parentId
   */
  @Schema(name = "parentId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("parentId")
  public @Nullable String getParentId() {
    return parentId;
  }

  @JsonProperty("parentId")
  public void setParentId(@Nullable String parentId) {
    this.parentId = parentId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdminAppMenusMoveMenuRequest adminAppMenusMoveMenuRequest = (AdminAppMenusMoveMenuRequest) o;
    return Objects.equals(this.parentId, adminAppMenusMoveMenuRequest.parentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parentId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdminAppMenusMoveMenuRequest {\n");
    sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
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
