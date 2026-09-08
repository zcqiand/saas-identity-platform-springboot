package saas.identity.shared.dto;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.lang.Nullable;
import saas.identity.shared.dto.SysRole;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantRolesListSysRoles200Response
 */

@JsonTypeName("TenantRoles_listSysRoles_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-08T17:42:04.049127+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantRolesListSysRoles200Response {

  private List<@Valid SysRole> items = new ArrayList<>();

  private Integer page;

  private Integer pageSize;

  private Long total;

  public TenantRolesListSysRoles200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantRolesListSysRoles200Response(List<@Valid SysRole> items, Integer page, Integer pageSize, Long total) {
    this.items = items;
    this.page = page;
    this.pageSize = pageSize;
    this.total = total;
  }

  public TenantRolesListSysRoles200Response items(List<@Valid SysRole> items) {
    this.items = items;
    return this;
  }

  public TenantRolesListSysRoles200Response addItemsItem(SysRole itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
   */
  @NotNull @Valid 
  @Schema(name = "items", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("items")
  public List<@Valid SysRole> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<@Valid SysRole> items) {
    this.items = items;
  }

  public TenantRolesListSysRoles200Response page(Integer page) {
    this.page = page;
    return this;
  }

  /**
   * Get page
   * @return page
   */
  @NotNull 
  @Schema(name = "page", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("page")
  public Integer getPage() {
    return page;
  }

  @JsonProperty("page")
  public void setPage(Integer page) {
    this.page = page;
  }

  public TenantRolesListSysRoles200Response pageSize(Integer pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * Get pageSize
   * @return pageSize
   */
  @NotNull 
  @Schema(name = "pageSize", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("pageSize")
  public Integer getPageSize() {
    return pageSize;
  }

  @JsonProperty("pageSize")
  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public TenantRolesListSysRoles200Response total(Long total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
   */
  @NotNull 
  @Schema(name = "total", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("total")
  public Long getTotal() {
    return total;
  }

  @JsonProperty("total")
  public void setTotal(Long total) {
    this.total = total;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TenantRolesListSysRoles200Response tenantRolesListSysRoles200Response = (TenantRolesListSysRoles200Response) o;
    return Objects.equals(this.items, tenantRolesListSysRoles200Response.items) &&
        Objects.equals(this.page, tenantRolesListSysRoles200Response.page) &&
        Objects.equals(this.pageSize, tenantRolesListSysRoles200Response.pageSize) &&
        Objects.equals(this.total, tenantRolesListSysRoles200Response.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, page, pageSize, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantRolesListSysRoles200Response {\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
    sb.append("    page: ").append(toIndentedString(page)).append("\n");
    sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

