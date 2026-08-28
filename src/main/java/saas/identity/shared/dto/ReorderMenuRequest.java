package saas.identity.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.Nullable;

/** ReorderMenuRequest */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-08-28T16:38:49.530507900+08:00[Asia/Shanghai]",
    comments = "Generator version: 7.24.0")
public class ReorderMenuRequest {

  private List<String> orderedMenuIds = new ArrayList<>();

  public ReorderMenuRequest() {
    super();
  }

  /** Constructor with only required parameters */
  public ReorderMenuRequest(List<String> orderedMenuIds) {
    this.orderedMenuIds = orderedMenuIds;
  }

  public ReorderMenuRequest orderedMenuIds(List<String> orderedMenuIds) {
    this.orderedMenuIds = orderedMenuIds;
    return this;
  }

  public ReorderMenuRequest addOrderedMenuIdsItem(String orderedMenuIdsItem) {
    if (this.orderedMenuIds == null) {
      this.orderedMenuIds = new ArrayList<>();
    }
    this.orderedMenuIds.add(orderedMenuIdsItem);
    return this;
  }

  /**
   * Get orderedMenuIds
   *
   * @return orderedMenuIds
   */
  @NotNull
  @Schema(name = "orderedMenuIds", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("orderedMenuIds")
  public List<String> getOrderedMenuIds() {
    return orderedMenuIds;
  }

  @JsonProperty("orderedMenuIds")
  public void setOrderedMenuIds(List<String> orderedMenuIds) {
    this.orderedMenuIds = orderedMenuIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReorderMenuRequest reorderMenuRequest = (ReorderMenuRequest) o;
    return Objects.equals(this.orderedMenuIds, reorderMenuRequest.orderedMenuIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderedMenuIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReorderMenuRequest {\n");
    sb.append("    orderedMenuIds: ").append(toIndentedString(orderedMenuIds)).append("\n");
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
