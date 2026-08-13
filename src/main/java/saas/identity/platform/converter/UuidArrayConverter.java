package saas.identity.platform.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * JPA AttributeConverter: List<UUID> ↔ UUID[] (PG native array).
 *
 * <p>shared/sql/migrations/V002/V005 等列 role_ids UUID[] / menu_ids UUID[] / scopes TEXT[] / 等数组列。
 * Hibernate 6 没有内置 UUID[] 类型支持，必须自定义 converter。
 */
@Converter
public class UuidArrayConverter implements AttributeConverter<List<UUID>, UUID[]> {

  @Override
  public UUID[] convertToDatabaseColumn(List<UUID> attribute) {
    if (attribute == null) return new UUID[0];
    return attribute.toArray(new UUID[0]);
  }

  @Override
  public List<UUID> convertToEntityAttribute(UUID[] dbData) {
    if (dbData == null) return null;
    return Arrays.asList(dbData);
  }
}
