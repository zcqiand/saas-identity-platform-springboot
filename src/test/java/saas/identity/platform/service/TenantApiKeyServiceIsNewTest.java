package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import saas.identity.platform.entity.ApiKeyEntity;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.ApiKeyRepository;
import saas.identity.shared.dto.CreateApiKeyRequest;

/**
 * 新建 entity 的 isNew 语义守卫（回归 2026-08-28 线上 POST api-keys 500）。
 *
 * <p>ApiKeyEntity 的主键是 {@code @GeneratedValue(UUID)}；Spring Data 的 save() 按 「id 非空 = 已存在」走
 * merge()。Service 若手动 setId(UUID.randomUUID()) 再 save()， 会被误判为 detached → UPDATE 0 行 →
 * StaleObjectStateException → 500。 新建路径不得手动赋 id，让 @GeneratedValue 在 persist 时生成。
 *
 * <p>同族守卫（映射层）：ArrayUserTypeMappingGuardTest。
 */
class TenantApiKeyServiceIsNewTest {

  @Test
  @Fn({"M05.F01.I02"})
  void create_mustNotPresetId() {
    ApiKeyRepository repo = mock(ApiKeyRepository.class);
    AuditWriter auditWriter = mock(AuditWriter.class);
    TenantApiKeyService service = new TenantApiKeyService(repo, auditWriter);
    when(repo.save(any(ApiKeyEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    service.create(UUID.randomUUID(), new CreateApiKeyRequest().name("k"));

    ArgumentCaptor<ApiKeyEntity> cap = ArgumentCaptor.forClass(ApiKeyEntity.class);
    verify(repo).save(cap.capture());
    assertNull(
        cap.getValue().getId(),
        "create() 不得预置 id：id 非空会被 Spring Data 判为 detached → merge → StaleObjectStateException（线上 POST 500）");
  }

  @Test
  @Fn({"M05.F01.I04"})
  void rotate_mustNotPresetIdOnFreshKey() {
    ApiKeyRepository repo = mock(ApiKeyRepository.class);
    AuditWriter auditWriter = mock(AuditWriter.class);
    TenantApiKeyService service = new TenantApiKeyService(repo, auditWriter);
    UUID tid = UUID.randomUUID();
    UUID kid = UUID.randomUUID();
    ApiKeyEntity old = new ApiKeyEntity();
    old.setId(kid);
    old.setTenantId(tid);
    old.setName("k");
    old.setPrefix("sk_x");
    old.setSecretHash("plain:s");
    old.setScopes(java.util.List.of());
    when(repo.findByTenantIdAndId(tid, kid)).thenReturn(Optional.of(old));
    when(repo.save(any(ApiKeyEntity.class)))
        .thenAnswer(
            inv -> {
              // 模拟真 save：第一次（revoke 旧 key，id 已存在）原样返回；
              // 第二次（fresh key）由 @GeneratedValue 赋 id —— 这里只验证入参。
              return inv.getArgument(0);
            });

    service.rotate(tid, kid);

    ArgumentCaptor<ApiKeyEntity> cap = ArgumentCaptor.forClass(ApiKeyEntity.class);
    verify(repo, org.mockito.Mockito.times(2)).save(cap.capture());
    ApiKeyEntity fresh = cap.getAllValues().get(1);
    assertNull(
        fresh.getId(), "rotate() 的新 key 不得预置 id（同 create：merge 误判 → StaleObjectStateException）");
    assertEquals(kid, cap.getAllValues().get(0).getId(), "旧 key 的 id 不变（是 update 不是 insert）");
  }

  @Test
  @Fn({"M05.F01.I02"})
  void entityMustUseGeneratedId() throws Exception {
    // 实体层断言：ApiKeyEntity 主键必须是 @GeneratedValue（UUID 策略）。
    // 若改成手动赋 id 模式，上面两条 Service 守卫就失去前提。
    var idField = ApiKeyEntity.class.getDeclaredField("id");
    var gen = idField.getAnnotation(jakarta.persistence.GeneratedValue.class);
    var idAnn = idField.getAnnotation(jakarta.persistence.Id.class);
    org.junit.jupiter.api.Assertions.assertNotNull(idAnn, "id 必须有 @Id");
    org.junit.jupiter.api.Assertions.assertNotNull(
        gen, "id 必须有 @GeneratedValue（手动 setId 会破坏 isNew 判定）");
    assertEquals(jakarta.persistence.GenerationType.class, gen.strategy().getDeclaringClass());
    // 不直接比较枚举值（GenerationType.UUID 在 jakarta.persistence 3.1+），
    // 断言 strategy 不是 IDENTITY/TABLE/SEQUENCE 的兜底即可。
    org.junit.jupiter.api.Assertions.assertNotEquals(
        jakarta.persistence.GenerationType.IDENTITY, gen.strategy());
  }
}
