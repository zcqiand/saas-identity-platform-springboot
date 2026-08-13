package saas.identity.platform.mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import saas.identity.platform.entity.ApiKeyEntity;
import saas.identity.shared.dto.ApiKey;
import saas.identity.shared.dto.CreateApiKeyResponse;

public final class ApiKeyMapper {

  private ApiKeyMapper() {}

  public static ApiKey toDto(ApiKeyEntity e) {
    ApiKey k = new ApiKey();
    k.setId(e.getId());
    k.setTenantId(e.getTenantId());
    k.setName(e.getName());
    k.setPrefix(e.getPrefix());
    k.setStatus(toDtoStatus(e.getStatus()));
    k.setScopes(e.getScopes());
    k.setCreatedAt(e.getCreatedAt());
    k.setLastUsedAt(e.getLastUsedAt());
    k.setExpiresAt(e.getExpiresAt());
    k.setRevokedAt(e.getRevokedAt());
    return k;
  }

  public static saas.identity.shared.dto.ApiKeyStatus toDtoStatus(
      saas.identity.platform.enums.ApiKeyStatus s) {
    if (s == null) return saas.identity.shared.dto.ApiKeyStatus.ACTIVE;
    try {
      return saas.identity.shared.dto.ApiKeyStatus.valueOf(s.name());
    } catch (IllegalArgumentException e) {
      return saas.identity.shared.dto.ApiKeyStatus.EXPIRED;
    }
  }

  public static saas.identity.platform.enums.ApiKeyStatus toDbStatus(
      saas.identity.shared.dto.ApiKeyStatus s) {
    if (s == null) return saas.identity.platform.enums.ApiKeyStatus.ACTIVE;
    try {
      return saas.identity.platform.enums.ApiKeyStatus.valueOf(s.name());
    } catch (IllegalArgumentException e) {
      return saas.identity.platform.enums.ApiKeyStatus.EXPIRED;
    }
  }

  public static CreateApiKeyResponse toCreateResponse(ApiKeyEntity e, String secret) {
    return new CreateApiKeyResponse().apiKey(toDto(e)).secret(secret);
  }

  public static List<ApiKey> toDtoList(List<ApiKeyEntity> entities) {
    return entities.stream().map(ApiKeyMapper::toDto).toList();
  }

  public static String generatePrefix() {
    return "sk_" + UUID.randomUUID().toString().substring(0, 8);
  }

  public static String generateSecret() {
    return "sk_" + UUID.randomUUID().toString().replace("-", "");
  }

  public static OffsetDateTime nowOrLater(OffsetDateTime from) {
    return from == null ? OffsetDateTime.now() : from;
  }
}
