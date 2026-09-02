package saas.identity.platform.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.AuditEventEntity;
import saas.identity.platform.enums.AuditAction;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

  // 2026-09-01 contract-test I18：家族约定 audit 列表按 occurred_at DESC
  // （aspnetcore/nextjs 均如此）。不排序时 PG 返回物理顺序，旧行挤占分页窗口，
  // 共库累积事件 > 翻页上限后新事件永远翻不到。

  Page<AuditEventEntity> findByTenantIdOrderByOccurredAtDesc(UUID tenantId, Pageable pageable);

  Page<AuditEventEntity> findByTenantIdAndActorUserIdOrderByOccurredAtDesc(
      UUID tenantId, UUID actorUserId, Pageable pageable);

  Page<AuditEventEntity> findByTenantIdAndActionOrderByOccurredAtDesc(
      UUID tenantId, AuditAction action, Pageable pageable);

  Page<AuditEventEntity> findByTenantIdAndOccurredAtBetweenOrderByOccurredAtDesc(
      UUID tenantId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
