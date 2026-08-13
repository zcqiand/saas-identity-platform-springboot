package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class AuditActionConverter implements AttributeConverter<AuditAction, String> {
  @Override
  public String convertToDatabaseColumn(AuditAction attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public AuditAction convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return AuditAction.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
