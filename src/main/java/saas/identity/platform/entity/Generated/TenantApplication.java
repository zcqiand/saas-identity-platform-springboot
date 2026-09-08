package saas.identity.platform.entity.Generated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DB-First scaffold：tenant_application（ADR-0025）。 由 scripts/scaffold-entities.mjs 从 saas_dev
 * 真库反推生成。 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等） 通过 extends TenantApplication 叠加在
 * src/main/java/.../entity/TenantApplication.java。
 *
 * <p>字段含义见 saas-identity-platform-shared/src/db/schema.ts tenantApplication。
 */
@Entity
@Table(name = "tenant_application")
public class TenantApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid", nullable = false)
  private UUID tenantId;

  @Column(name = "client_id", columnDefinition = "character varying", nullable = false)
  private String clientId;

  @Column(name = "status", columnDefinition = "smallint", nullable = false)
  private Short status;

  @Column(name = "expire_time", columnDefinition = "timestamptz")
  private OffsetDateTime expireTime;

  @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime createdAt;

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

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Short getStatus() {
    return status;
  }

  public void setStatus(Short status) {
    this.status = status;
  }

  public OffsetDateTime getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(OffsetDateTime expireTime) {
    this.expireTime = expireTime;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
