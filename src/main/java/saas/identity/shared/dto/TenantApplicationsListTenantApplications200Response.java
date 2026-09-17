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
import saas.identity.shared.dto.TenantApplication;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TenantApplicationsListTenantApplications200Response
 */

@JsonTypeName("TenantApplications_listTenantApplications_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-09-17T22:53:30.131396900+08:00[Asia/Shanghai]", comments = "Generator version: 7.24.0")
public class TenantApplicationsListTenantApplications200Response {

  private List<@Valid TenantApplication> items = new ArrayList<>();

  private Integer page;

  private Integer pageSize;

  private Long total;

  public TenantApplicationsListTenantApplications200Response() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TenantApplicationsListTenantApplications200Response(List<@Valid TenantApplication> items, Integer page, Integer pageSize, Long total) {
    this.items = items;
    this.page = page;
    this.pageSize = pageSize;
    this.total = total;
  }

  public TenantApplicationsListTenantApplications200Response items(List<@Valid TenantApplication> items) {
    this.items = items;
    return this;
  }

  public TenantApplicationsListTenantApplications200Response addItemsItem(TenantApplication itemsItem) {
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
  public List<@Valid TenantApplication> getItems() {
    return items;
  }

  @JsonProperty("items")
  public void setItems(List<@Valid TenantApplication> items) {
    this.items = items;
  }

  public TenantApplicationsListTenantApplications200Response page(Integer page) {
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

  public TenantApplicationsListTenantApplications200Response pageSize(Integer pageSize) {
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

  public TenantApplicationsListTenantApplications200Response total(Long total) {
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
    TenantApplicationsListTenantApplications200Response tenantApplicationsListTenantApplications200Response = (TenantApplicationsListTenantApplications200Response) o;
    return Objects.equals(this.items, tenantApplicationsListTenantApplications200Response.items) &&
        Objects.equals(this.page, tenantApplicationsListTenantApplications200Response.page) &&
        Objects.equals(this.pageSize, tenantApplicationsListTenantApplications200Response.pageSize) &&
        Objects.equals(this.total, tenantApplicationsListTenantApplications200Response.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, page, pageSize, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantApplicationsListTenantApplications200Response {\n");
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

