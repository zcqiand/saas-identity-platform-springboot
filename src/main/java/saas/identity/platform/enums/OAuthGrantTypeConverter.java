package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class OAuthGrantTypeConverter implements AttributeConverter<OAuthGrantType, String> {
  @Override
  public String convertToDatabaseColumn(OAuthGrantType attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public OAuthGrantType convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return OAuthGrantType.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
