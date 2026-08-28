package saas.identity.platform.mapper;

import java.util.HashMap;
import java.util.Map;
import saas.identity.platform.entity.TenantEntity;
import saas.identity.shared.dto.CreateTenantRequest;
import saas.identity.shared.dto.Tenant;
import saas.identity.shared.dto.TenantSettings;
import saas.identity.shared.dto.TenantStatus;
import saas.identity.shared.dto.UpdateTenantRequest;

/** Tenant DTO ↔ Entity 转换。 边界规则：Repository 不接触 DTO；DTO 不接触 Entity；Service 持两者。 */
public final class TenantMapper {

  private TenantMapper() {}

  public static Tenant toDto(TenantEntity e) {
    Tenant t = new Tenant();
    t.setId(e.getId());
    t.setCode(e.getCode());
    t.setName(e.getName());
    t.setStatus(toDtoStatus(e.getStatus()));
    t.setSettings(toDtoSettings(e.getSettings()));
    t.setCreatedAt(e.getCreatedAt());
    t.setUpdatedAt(e.getUpdatedAt());
    return t;
  }

  public static TenantEntity fromCreateRequest(CreateTenantRequest req) {
    TenantEntity e = new TenantEntity();
    // 不预置 id：id 非空被 Spring Data 判为 detached → merge → StaleObjectStateException
    e.setCode(req.getCode());
    e.setName(req.getName());
    e.setStatus(saas.identity.platform.enums.TenantStatus.ACTIVE);
    e.setSettings(fromDtoSettings(req.getSettings()));
    return e;
  }

  public static TenantStatus toDtoStatus(saas.identity.platform.enums.TenantStatus s) {
    if (s == null) return TenantStatus.ACTIVE;
    return switch (s) {
      case ACTIVE -> TenantStatus.ACTIVE;
      case SUSPENDED -> TenantStatus.SUSPENDED;
      case ARCHIVED -> TenantStatus.ARCHIVED;
    };
  }

  public static saas.identity.platform.enums.TenantStatus toDbStatus(TenantStatus s) {
    if (s == null) return saas.identity.platform.enums.TenantStatus.ACTIVE;
    return switch (s) {
      case ACTIVE -> saas.identity.platform.enums.TenantStatus.ACTIVE;
      case SUSPENDED -> saas.identity.platform.enums.TenantStatus.SUSPENDED;
      case ARCHIVED -> saas.identity.platform.enums.TenantStatus.ARCHIVED;
    };
  }

  public static void applyUpdate(TenantEntity e, UpdateTenantRequest req) {
    if (req.getName() != null) e.setName(req.getName());
    if (req.getCode() != null) e.setCode(req.getCode());
    if (req.getStatus() != null) e.setStatus(toDbStatus(req.getStatus()));
    if (req.getSettings() != null) e.setSettings(fromDtoSettings(req.getSettings()));
  }

  // === Settings DTO ↔ Map 转换 ===

  public static Map<String, Object> fromDtoSettings(TenantSettings s) {
    Map<String, Object> m = new HashMap<>();
    if (s == null) return m;
    if (s.getThemeColor() != null) m.put("themeColor", s.getThemeColor());
    if (s.getLocale() != null) m.put("locale", s.getLocale());
    m.put("maxUsers", s.getMaxUsers());
    return m;
  }

  public static TenantSettings toDtoSettings(Map<String, Object> src) {
    TenantSettings s = new TenantSettings();
    if (src == null) return s;
    if (src.get("themeColor") != null) s.setThemeColor(src.get("themeColor").toString());
    if (src.get("locale") != null) s.setLocale(src.get("locale").toString());
    if (src.get("maxUsers") != null) s.setMaxUsers(((Number) src.get("maxUsers")).intValue());
    return s;
  }
}
