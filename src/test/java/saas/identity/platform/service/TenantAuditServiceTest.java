package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import saas.identity.platform.entity.AuditEventEntity;
import saas.identity.platform.entity.AuditRetentionPolicyEntity;
import saas.identity.platform.enums.AuditAction;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.AuditEventRepository;
import saas.identity.platform.repository.AuditRetentionPolicyRepository;
import saas.identity.shared.dto.AuditEvent;

/** TenantAuditService 单测（M06.F01 审计事件查询 + M06.F02 留存策略）。 */
class TenantAuditServiceTest {

  private final AuditEventRepository auditEventRepository = mock(AuditEventRepository.class);
  private final AuditRetentionPolicyRepository retentionRepository =
      mock(AuditRetentionPolicyRepository.class);
  private final TenantAuditService service =
      new TenantAuditService(auditEventRepository, retentionRepository);

  private AuditEventEntity event(UUID tenantId) {
    AuditEventEntity e = new AuditEventEntity();
    e.setId(UUID.randomUUID());
    e.setTenantId(tenantId);
    e.setActorUserId(UUID.randomUUID());
    e.setAction(AuditAction.USER_CREATED);
    e.setOccurredAt(OffsetDateTime.now());
    return e;
  }

  @Test
  @Fn({"M06.F01.I01"})
  void list_returnsPage() {
    UUID tid = UUID.randomUUID();
    Page<AuditEventEntity> page = new PageImpl<>(List.of(event(tid)));
    when(auditEventRepository.findByTenantId(eq(tid), any(Pageable.class))).thenReturn(page);

    Page<AuditEvent> result = service.list(tid, 0, 20, null, null, null, null);
    assertEquals(1, result.getContent().size());
  }

  @Test
  @Fn({"M06.F01.I02"})
  void list_byActorUsesActorQuery() {
    UUID tid = UUID.randomUUID();
    UUID actor = UUID.randomUUID();
    Page<AuditEventEntity> page = new PageImpl<>(List.of());
    when(auditEventRepository.findByTenantIdAndActorUserId(eq(tid), eq(actor), any(Pageable.class)))
        .thenReturn(page);
    service.list(tid, 0, 20, actor, null, null, null);
    verify(auditEventRepository)
        .findByTenantIdAndActorUserId(eq(tid), eq(actor), any(Pageable.class));
  }

  @Test
  @Fn({"M06.F01.I03"})
  void list_byTimeRangeUsesBetweenQuery() {
    UUID tid = UUID.randomUUID();
    OffsetDateTime from = OffsetDateTime.now().minusDays(1);
    OffsetDateTime to = OffsetDateTime.now();
    Page<AuditEventEntity> page = new PageImpl<>(List.of());
    when(auditEventRepository.findByTenantIdAndOccurredAtBetween(
            eq(tid), eq(from), eq(to), any(Pageable.class)))
        .thenReturn(page);
    service.list(tid, 0, 20, null, null, from, to);
    verify(auditEventRepository)
        .findByTenantIdAndOccurredAtBetween(eq(tid), eq(from), eq(to), any(Pageable.class));
  }

  @Test
  @Fn({"M06.F02.I04"})
  void getRetention_defaultsTo90() {
    UUID tid = UUID.randomUUID();
    when(retentionRepository.findById(tid)).thenReturn(Optional.empty());
    assertEquals(90, service.getRetention(tid));
  }

  @Test
  @Fn({"M06.F02.I04"})
  void getRetention_returnsStored() {
    UUID tid = UUID.randomUUID();
    AuditRetentionPolicyEntity e = new AuditRetentionPolicyEntity();
    e.setTenantId(tid);
    e.setRetentionDays(30);
    when(retentionRepository.findById(tid)).thenReturn(Optional.of(e));
    assertEquals(30, service.getRetention(tid));
  }

  @Test
  @Fn({"M06.F02.I04"})
  void setRetention_createsIfMissing() {
    UUID tid = UUID.randomUUID();
    when(retentionRepository.findById(tid)).thenReturn(Optional.empty());
    when(retentionRepository.save(any(AuditRetentionPolicyEntity.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    int days = service.setRetention(tid, 60);
    assertEquals(60, days);
    verify(retentionRepository).save(any(AuditRetentionPolicyEntity.class));
    assertNotNull(days);
  }
}
