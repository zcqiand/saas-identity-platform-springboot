package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class MenuStatusConverter implements AttributeConverter<MenuStatus, String> {
  @Override
  public String convertToDatabaseColumn(MenuStatus attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public MenuStatus convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return MenuStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
