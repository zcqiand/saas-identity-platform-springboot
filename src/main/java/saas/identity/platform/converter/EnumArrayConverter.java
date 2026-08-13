package saas.identity.platform.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA AttributeConverter: List<Enum<E>> ↔ String[] (PG native enum[]).
 *
 * <p>用于 apps.grant_types 等 oauth_grant_type[] 列。Hibernate 不直接支持 enum[]，用 String[] 中转。
 */
@Converter
public class EnumArrayConverter implements AttributeConverter<List<String>, String[]> {

  @Override
  public String[] convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null) return new String[0];
    return attribute.toArray(new String[0]);
  }

  @Override
  public List<String> convertToEntityAttribute(String[] dbData) {
    if (dbData == null) return null;
    return Arrays.stream(dbData).collect(Collectors.toList());
  }
}
