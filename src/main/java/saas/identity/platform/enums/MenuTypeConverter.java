package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class MenuTypeConverter implements AttributeConverter<MenuType, String> {
  @Override
  public String convertToDatabaseColumn(MenuType attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public MenuType convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return MenuType.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
