package saas.identity.platform.entity;

import jakarta.persistence.*;
import jakarta.persistence.Convert;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import saas.identity.platform.enums.AuditAction;
import saas.identity.platform.enums.AuditActionConverter;

/** V006 — tenant-scoped 不可变审计事件（应用层 insert-only）。 */
@Entity
@Table(name = "audit_events")
public class AuditEventEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "actor_user_id", columnDefinition = "uuid")
  private UUID actorUserId;

  @Convert(converter = AuditActionConverter.class)
  @Column(name = "action", columnDefinition = "audit_action", nullable = false)
  private AuditAction action;

  @Column(name = "target_user_id", columnDefinition = "uuid")
  private UUID targetUserId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
  private Map<String, Object> metadata = Map.of();

  @Column(
      name = "occurred_at",
      columnDefinition = "timestamptz",
      nullable = false,
      updatable = false)
  private OffsetDateTime occurredAt;

  @PrePersist
  void onCreate() {
    if (occurredAt == null) occurredAt = OffsetDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public UUID getActorUserId() {
    return actorUserId;
  }

  public void setActorUserId(UUID actorUserId) {
    this.actorUserId = actorUserId;
  }

  public AuditAction getAction() {
    return action;
  }

  public void setAction(AuditAction action) {
    this.action = action;
  }

  public UUID getTargetUserId() {
    return targetUserId;
  }

  public void setTargetUserId(UUID targetUserId) {
    this.targetUserId = targetUserId;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public OffsetDateTime getOccurredAt() {
    return occurredAt;
  }

  public void setOccurredAt(OffsetDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }
}
