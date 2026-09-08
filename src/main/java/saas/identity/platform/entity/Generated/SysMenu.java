package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DB-First scaffold：sys_menu（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends SysMenu 叠加在 src/main/java/.../entity/SysMenu.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts sysMenu。

 */
@Entity
@Table(name = "sys_menu")
public class SysMenu {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "client_id", columnDefinition = "character varying", nullable = false)
  private String clientId;

  @Column(name = "parent_id", columnDefinition = "uuid", nullable = false)
  private UUID parentId;

  @Column(name = "title", columnDefinition = "character varying", nullable = false)
  private String title;

  @Column(name = "type", columnDefinition = "smallint", nullable = false)
  private Short type;

  @Column(name = "path", columnDefinition = "character varying")
  private String path;

  @Column(name = "component", columnDefinition = "character varying")
  private String component;

  @Column(name = "perms", columnDefinition = "character varying")
  private String perms;

  @Column(name = "icon", columnDefinition = "character varying")
  private String icon;

  @Column(name = "sort_order", columnDefinition = "integer", nullable = false)
  private Integer sortOrder;

  @Column(name = "status", columnDefinition = "smallint", nullable = false)
  private Short status;

  @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Short getType() {
    return type;
  }

  public void setType(Short type) {
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public String getPerms() {
    return perms;
  }

  public void setPerms(String perms) {
    this.perms = perms;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Short getStatus() {
    return status;
  }

  public void setStatus(Short status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
