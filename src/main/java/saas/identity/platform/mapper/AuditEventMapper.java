package saas.identity.platform.mapper;

import java.util.List;
import java.util.Map;
import saas.identity.platform.entity.AuditEventEntity;
import saas.identity.shared.dto.AuditEvent;

public final class AuditEventMapper {

  private AuditEventMapper() {}

  public static AuditEvent toDto(AuditEventEntity e) {
    AuditEvent a = new AuditEvent();
    a.setId(e.getId());
    a.setTenantId(e.getTenantId());
    a.setActorUserId(e.getActorUserId());
    a.setAction(toDtoAction(e.getAction()));
    a.setTargetUserId(e.getTargetUserId());
    a.setMetadata(e.getMetadata() != null ? e.getMetadata() : Map.of());
    a.setOccurredAt(e.getOccurredAt());
    return a;
  }

  private static saas.identity.shared.dto.AuditAction toDtoAction(
      saas.identity.platform.enums.AuditAction s) {
    if (s == null) return saas.identity.shared.dto.AuditAction.USER_CREATED;
    try {
      return saas.identity.shared.dto.AuditAction.valueOf(s.name());
    } catch (IllegalArgumentException e) {
      return saas.identity.shared.dto.AuditAction.USER_CREATED;
    }
  }

  public static saas.identity.platform.enums.AuditAction toDbAction(
      saas.identity.shared.dto.AuditAction a) {
    if (a == null) return saas.identity.platform.enums.AuditAction.USER_CREATED;
    try {
      return saas.identity.platform.enums.AuditAction.valueOf(a.name());
    } catch (IllegalArgumentException e) {
      return saas.identity.platform.enums.AuditAction.USER_CREATED;
    }
  }

  public static List<AuditEvent> toDtoList(List<AuditEventEntity> entities) {
    return entities.stream().map(AuditEventMapper::toDto).toList();
  }
}
