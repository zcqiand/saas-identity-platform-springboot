package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class AppStatusConverter implements AttributeConverter<AppStatus, String> {
  @Override
  public String convertToDatabaseColumn(AppStatus attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public AppStatus convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return AppStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
