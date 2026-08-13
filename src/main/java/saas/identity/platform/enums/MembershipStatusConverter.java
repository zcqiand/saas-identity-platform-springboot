package saas.identity.platform.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class MembershipStatusConverter implements AttributeConverter<MembershipStatus, String> {
  @Override
  public String convertToDatabaseColumn(MembershipStatus attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public MembershipStatus convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return MembershipStatus.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
