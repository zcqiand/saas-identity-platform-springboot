package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ReorderSysMenuRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class ReorderSysMenuRequest {

  private List<String> orderedMenuIds = new ArrayList<>();

  public ReorderSysMenuRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ReorderSysMenuRequest(List<String> orderedMenuIds) {
    this.orderedMenuIds = orderedMenuIds;
  }

  public ReorderSysMenuRequest orderedMenuIds(List<String> orderedMenuIds) {
    this.orderedMenuIds = orderedMenuIds;
    return this;
  }

  public ReorderSysMenuRequest addOrderedMenuIdsItem(String orderedMenuIdsItem) {
    if (this.orderedMenuIds == null) {
      this.orderedMenuIds = new ArrayList<>();
    }
    this.orderedMenuIds.add(orderedMenuIdsItem);
    return this;
  }

  /**
   * Get orderedMenuIds
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
    ReorderSysMenuRequest reorderSysMenuRequest = (ReorderSysMenuRequest) o;
    return Objects.equals(this.orderedMenuIds, reorderSysMenuRequest.orderedMenuIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderedMenuIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReorderSysMenuRequest {\n");
    sb.append("    orderedMenuIds: ").append(toIndentedString(orderedMenuIds)).append("\n");
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

