package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

// PG tenant_status 是 lowercase enum（'active' / 'suspended' / 'archived'），
// Java TenantStatus 用 UPPER_SNAKE 命名（ACTIVE / SUSPENDED / ARCHIVED）。
// Hibernate PostgreSQLEnumJdbcType 直接 Enum.valueOf() 找不到 lowercase label，必须 AttributeConverter 桥接。
@Converter(autoApply = false)
public class TenantStatusConverter implements AttributeConverter<TenantStatus, String> {
  @Override
  public String convertToDatabaseColumn(TenantStatus attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public TenantStatus convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return TenantStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
