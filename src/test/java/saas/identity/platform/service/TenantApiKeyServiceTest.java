package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import saas.identity.platform.entity.ApiKeyEntity;
import saas.identity.platform.enums.ApiKeyStatus;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.ApiKeyRepository;
import saas.identity.shared.dto.ApiKey;
import saas.identity.shared.dto.CreateApiKeyRequest;
import saas.identity.shared.dto.CreateApiKeyResponse;

/** TenantApiKeyService 单测（M05.F01 API Key 生命周期）。 */
class TenantApiKeyServiceTest {

  private final ApiKeyRepository apiKeyRepository = mock(ApiKeyRepository.class);
  private final TenantApiKeyService service = new TenantApiKeyService(apiKeyRepository);

  private ApiKeyEntity entity(UUID tenantId, UUID keyId) {
    ApiKeyEntity e = new ApiKeyEntity();
    e.setId(keyId);
    e.setTenantId(tenantId);
    e.setName("ci-key");
    e.setPrefix("sk_ci_");
    e.setSecretHash("plain:secret");
    e.setStatus(ApiKeyStatus.ACTIVE);
    e.setScopes(List.of());
    e.setCreatedAt(OffsetDateTime.now());
    return e;
  }

  @Test
  @Fn({"M05.F01.I01"})
  void list_returnsPage() {
    UUID tid = UUID.randomUUID();
    Page<ApiKeyEntity> page = new PageImpl<>(List.of(entity(tid, UUID.randomUUID())));
    when(apiKeyRepository.findByTenantId(eq(tid), any(Pageable.class))).thenReturn(page);
    Page<ApiKey> result = service.list(tid, 0, 20);
    assertEquals(1, result.getContent().size());
    assertEquals("ci-key", result.getContent().get(0).getName());
  }

  @Test
  @Fn({"M05.F01.I02"})
  void create_returnsResponseWithSecret() {
    UUID tid = UUID.randomUUID();
    when(apiKeyRepository.save(any(ApiKeyEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    CreateApiKeyRequest body = new CreateApiKeyRequest().name("ci-key");
    CreateApiKeyResponse resp = service.create(tid, body);
    assertNotNull(resp.getSecret());
    assertNotNull(resp.getApiKey());
    assertEquals("ci-key", resp.getApiKey().getName());
  }

  @Test
  @Fn({"M05.F01.I03"})
  void revoke_setsStatusRevoked() {
    UUID tid = UUID.randomUUID();
    UUID kid = UUID.randomUUID();
    when(apiKeyRepository.findByTenantIdAndId(tid, kid)).thenReturn(Optional.of(entity(tid, kid)));
    when(apiKeyRepository.save(any(ApiKeyEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    ApiKey k = service.revoke(tid, kid);
    assertEquals(saas.identity.shared.dto.ApiKeyStatus.REVOKED, k.getStatus());
    verify(apiKeyRepository).save(any(ApiKeyEntity.class));
  }

  @Test
  @Fn({"M05.F01.I03"})
  void revoke_throwsIfMissing() {
    UUID tid = UUID.randomUUID();
    UUID kid = UUID.randomUUID();
    when(apiKeyRepository.findByTenantIdAndId(tid, kid)).thenReturn(Optional.empty());
    assertThrows(java.util.NoSuchElementException.class, () -> service.revoke(tid, kid));
  }

  @Test
  @Fn({"M05.F01.I04"})
  void rotate_revokesOldAndCreatesNew() {
    UUID tid = UUID.randomUUID();
    UUID kid = UUID.randomUUID();
    when(apiKeyRepository.findByTenantIdAndId(tid, kid)).thenReturn(Optional.of(entity(tid, kid)));
    when(apiKeyRepository.save(any(ApiKeyEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    CreateApiKeyResponse resp = service.rotate(tid, kid);
    assertNotNull(resp.getSecret());
    // 两次 save：旧 key revoke + 新 key create
    verify(apiKeyRepository, org.mockito.Mockito.times(2)).save(any(ApiKeyEntity.class));
  }
}
