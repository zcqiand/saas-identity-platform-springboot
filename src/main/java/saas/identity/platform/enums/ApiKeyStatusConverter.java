package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class ApiKeyStatusConverter implements AttributeConverter<ApiKeyStatus, String> {
  @Override
  public String convertToDatabaseColumn(ApiKeyStatus attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public ApiKeyStatus convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return ApiKeyStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
