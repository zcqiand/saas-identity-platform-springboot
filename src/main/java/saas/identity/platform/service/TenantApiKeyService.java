package saas.identity.platform.service;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.ApiKeyEntity;
import saas.identity.platform.mapper.ApiKeyMapper;
import saas.identity.platform.repository.ApiKeyRepository;
import saas.identity.shared.dto.ApiKey;
import saas.identity.shared.dto.CreateApiKeyRequest;
import saas.identity.shared.dto.CreateApiKeyResponse;

/**
 * M05.F01 — 租户内 API Key 生命周期（list / create / revoke / rotate）。 v0.4.0：从 InMemoryStore 迁到
 * ApiKeyRepository。
 */
@Service
public class TenantApiKeyService {

  private final ApiKeyRepository apiKeyRepository;

  public TenantApiKeyService(ApiKeyRepository apiKeyRepository) {
    this.apiKeyRepository = apiKeyRepository;
  }

  @Transactional(readOnly = true)
  public Page<ApiKey> list(UUID tenantId, int page, int pageSize) {
    Pageable p = PageRequest.of(page, pageSize);
    return apiKeyRepository.findByTenantId(tenantId, p).map(ApiKeyMapper::toDto);
  }

  @Transactional
  public CreateApiKeyResponse create(UUID tenantId, CreateApiKeyRequest body) {
    String prefix = ApiKeyMapper.generatePrefix();
    String secret = ApiKeyMapper.generateSecret();
    ApiKeyEntity e = new ApiKeyEntity();
    e.setId(UUID.randomUUID());
    e.setTenantId(tenantId);
    e.setName(body.getName());
    e.setPrefix(prefix);
    e.setSecretHash("plain:" + secret);
    e.setStatus(saas.identity.platform.enums.ApiKeyStatus.ACTIVE);
    e.setScopes(body.getScopes() != null ? body.getScopes() : java.util.List.of());
    e.setExpiresAt(body.getExpiresAt());
    return ApiKeyMapper.toCreateResponse(apiKeyRepository.save(e), secret);
  }

  @Transactional
  public ApiKey revoke(UUID tenantId, UUID keyId) {
    ApiKeyEntity e =
        apiKeyRepository
            .findByTenantIdAndId(tenantId, keyId)
            .orElseThrow(() -> new NoSuchElementException("api key not found"));
    e.setStatus(saas.identity.platform.enums.ApiKeyStatus.REVOKED);
    e.setRevokedAt(OffsetDateTime.now());
    return ApiKeyMapper.toDto(apiKeyRepository.save(e));
  }

  @Transactional
  public CreateApiKeyResponse rotate(UUID tenantId, UUID keyId) {
    ApiKeyEntity old =
        apiKeyRepository
            .findByTenantIdAndId(tenantId, keyId)
            .orElseThrow(() -> new NoSuchElementException("api key not found"));
    old.setStatus(saas.identity.platform.enums.ApiKeyStatus.REVOKED);
    old.setRevokedAt(OffsetDateTime.now());
    apiKeyRepository.save(old);

    String prefix = ApiKeyMapper.generatePrefix();
    String secret = ApiKeyMapper.generateSecret();
    ApiKeyEntity fresh = new ApiKeyEntity();
    fresh.setId(UUID.randomUUID());
    fresh.setTenantId(old.getTenantId());
    fresh.setName(old.getName());
    fresh.setPrefix(prefix);
    fresh.setSecretHash("plain:" + secret);
    fresh.setStatus(saas.identity.platform.enums.ApiKeyStatus.ACTIVE);
    fresh.setScopes(old.getScopes());
    fresh.setExpiresAt(old.getExpiresAt());
    return ApiKeyMapper.toCreateResponse(apiKeyRepository.save(fresh), secret);
  }
}
