package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {
  @Override
  public String convertToDatabaseColumn(UserStatus attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public UserStatus convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return UserStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
