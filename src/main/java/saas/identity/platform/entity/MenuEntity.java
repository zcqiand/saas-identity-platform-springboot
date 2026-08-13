package saas.identity.platform.entity;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import java.time.OffsetDateTime;
import java.util.UUID;
import saas.identity.platform.enums.MenuStatus;
import saas.identity.platform.enums.MenuStatusConverter;
import saas.identity.platform.enums.MenuType;
import saas.identity.platform.enums.MenuTypeConverter;

/** V005 — 树形菜单（parent_id 自引用）。 */
@Entity
@Table(
    name = "menus",
    uniqueConstraints =
        @UniqueConstraint(
            name = "menus_app_code_unique",
            columnNames = {"app_id", "code"}))
public class MenuEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "app_id", columnDefinition = "uuid", nullable = false)
  private UUID appId;

  @Column(name = "parent_id", columnDefinition = "uuid")
  private UUID parentId;

  @Column(name = "code", length = 64, nullable = false)
  private String code;

  @Column(name = "name", length = 255, nullable = false)
  private String name;

  @Column(name = "path", length = 512)
  private String path;

  @Column(name = "icon", length = 64)
  private String icon;

  @Convert(converter = MenuTypeConverter.class)
  @Column(name = "type", columnDefinition = "menu_type", nullable = false)
  private MenuType type = MenuType.PAGE;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;

  @Convert(converter = MenuStatusConverter.class)
  @Column(name = "status", columnDefinition = "menu_status", nullable = false)
  private MenuStatus status = MenuStatus.ACTIVE;

  @Column(
      name = "created_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void onCreate() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (updatedAt == null) updatedAt = createdAt;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getAppId() {
    return appId;
  }

  public void setAppId(UUID appId) {
    this.appId = appId;
  }

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public MenuType getType() {
    return type;
  }

  public void setType(MenuType type) {
    this.type = type;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public MenuStatus getStatus() {
    return status;
  }

  public void setStatus(MenuStatus status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
