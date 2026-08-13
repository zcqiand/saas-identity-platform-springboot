package saas.identity.platform.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.AuditEventEntity;
import saas.identity.platform.enums.AuditAction;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

  Page<AuditEventEntity> findByTenantId(UUID tenantId, Pageable pageable);

  Page<AuditEventEntity> findByTenantIdAndActorUserId(
      UUID tenantId, UUID actorUserId, Pageable pageable);

  Page<AuditEventEntity> findByTenantIdAndAction(
      UUID tenantId, AuditAction action, Pageable pageable);

  Page<AuditEventEntity> findByTenantIdAndOccurredAtBetween(
      UUID tenantId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
