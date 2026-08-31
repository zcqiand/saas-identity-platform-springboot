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
  private final AuditWriter auditWriter;

  public TenantApiKeyService(ApiKeyRepository apiKeyRepository, AuditWriter auditWriter) {
    this.apiKeyRepository = apiKeyRepository;
    this.auditWriter = auditWriter;
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
    // 不预置 id：id 非空会被 Spring Data 判为 detached → merge → UPDATE 0 行
    // → StaleObjectStateException（线上 POST 500）。@GeneratedValue 在 persist 时生成。
    e.setTenantId(tenantId);
    e.setName(body.getName());
    e.setPrefix(prefix);
    e.setSecretHash("plain:" + secret);
    e.setStatus(saas.identity.platform.enums.ApiKeyStatus.ACTIVE);
    e.setScopes(body.getScopes() != null ? body.getScopes() : java.util.List.of());
    e.setExpiresAt(body.getExpiresAt());
    CreateApiKeyResponse resp = ApiKeyMapper.toCreateResponse(apiKeyRepository.save(e), secret);
    // M06.F03.I01 副作用：发 api_key_created 事件（独立事务，best-effort）
    if (resp.getApiKey().getId() != null) {
      auditWriter.write(
          tenantId,
          AuditWriter.currentActorUserId(),
          "api_key_created",
          null,
          java.util.Map.of("apiKeyId", resp.getApiKey().getId().toString()));
    }
    return resp;
  }

  @Transactional
  public ApiKey revoke(UUID tenantId, UUID keyId) {
    ApiKeyEntity e =
        apiKeyRepository
            .findByTenantIdAndId(tenantId, keyId)
            .orElseThrow(() -> new NoSuchElementException("api key not found"));
    e.setStatus(saas.identity.platform.enums.ApiKeyStatus.REVOKED);
    e.setRevokedAt(OffsetDateTime.now());
    ApiKey dto = ApiKeyMapper.toDto(apiKeyRepository.save(e));
    if (e.getId() != null) {
      auditWriter.write(
          tenantId,
          AuditWriter.currentActorUserId(),
          "api_key_revoked",
          null,
          java.util.Map.of("apiKeyId", e.getId().toString()));
    }
    return dto;
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
    // 同 create：不预置 id，走 @GeneratedValue
    fresh.setTenantId(old.getTenantId());
    fresh.setName(old.getName());
    fresh.setPrefix(prefix);
    fresh.setSecretHash("plain:" + secret);
    fresh.setStatus(saas.identity.platform.enums.ApiKeyStatus.ACTIVE);
    fresh.setScopes(old.getScopes());
    fresh.setExpiresAt(old.getExpiresAt());
    CreateApiKeyResponse resp = ApiKeyMapper.toCreateResponse(apiKeyRepository.save(fresh), secret);
    if (old.getId() != null) {
      auditWriter.write(
          tenantId,
          AuditWriter.currentActorUserId(),
          "api_key_revoked",
          null,
          java.util.Map.of("apiKeyId", old.getId().toString()));
    }
    if (fresh.getId() != null) {
      auditWriter.write(
          tenantId,
          AuditWriter.currentActorUserId(),
          "api_key_created",
          null,
          java.util.Map.of("apiKeyId", fresh.getId().toString()));
    }
    return resp;
  }

  /**
   * M05.F01.I05 物理删除（区别于 I03 revoke 软删：直接删 DB 行，无审计事件）。 与 I03 revoke 并存：revoke 保留行（status=revoked +
   * revokedAt）；本 op 行消失。 幂等：重复删已不存在的 keyId 抛 NoSuchElementException，GlobalExceptionHandler → 404。
   */
  // @entry M05.F01.I05
  @Transactional
  @SuppressWarnings("null") // e 非空（orElseThrow 已保证）；Spring Data delete 签名标注 @NonNull 是历史包袱
  public void delete(UUID tenantId, UUID keyId) {
    ApiKeyEntity e =
        apiKeyRepository
            .findByTenantIdAndId(tenantId, keyId)
            .orElseThrow(() -> new NoSuchElementException("api key not found"));
    apiKeyRepository.delete(e);
    // 不写 audit event（物理删不留痕；与 revoke 写 api_key_revoked 形成对照）
  }
}
