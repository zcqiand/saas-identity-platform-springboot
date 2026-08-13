package saas.identity.platform.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;

/**
 * JPA AttributeConverter: List<String> ↔ String[] (PG native array).
 *
 * <p>用于 api_keys.scopes / apps.redirect_uris / apps.scopes / apps.grant_types（enum 字符串）等 TEXT[] 列。
 */
@Converter
public class StringArrayConverter implements AttributeConverter<List<String>, String[]> {

  @Override
  public String[] convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null) return new String[0];
    return attribute.toArray(new String[0]);
  }

  @Override
  public List<String> convertToEntityAttribute(String[] dbData) {
    if (dbData == null) return null;
    return Arrays.asList(dbData);
  }
}
