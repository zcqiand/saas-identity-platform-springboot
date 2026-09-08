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
 * DB-First scaffold：sys_user（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends SysUser 叠加在 src/main/java/.../entity/SysUser.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts sysUser。

 */
@Entity
@Table(name = "sys_user")
public class SysUser {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", columnDefinition = "uuid", nullable = false)
  private UUID id;

  @Column(name = "username", columnDefinition = "character varying", nullable = false)
  private String username;

  @Column(name = "password", columnDefinition = "character varying", nullable = false)
  private String password;

  @Column(name = "email", columnDefinition = "character varying")
  private String email;

  @Column(name = "mobile", columnDefinition = "character varying")
  private String mobile;

  @Column(name = "status", columnDefinition = "smallint", nullable = false)
  private Short status;

  @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "failed_attempts", columnDefinition = "integer", nullable = false)
  private Integer failedAttempts;

  @Column(name = "locked_until", columnDefinition = "timestamptz")
  private OffsetDateTime lockedUntil;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public Short getStatus() {
    return status;
  }

  public void setStatus(Short status) {
    this.status = status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Integer getFailedAttempts() {
    return failedAttempts;
  }

  public void setFailedAttempts(Integer failedAttempts) {
    this.failedAttempts = failedAttempts;
  }

  public OffsetDateTime getLockedUntil() {
    return lockedUntil;
  }

  public void setLockedUntil(OffsetDateTime lockedUntil) {
    this.lockedUntil = lockedUntil;
  }
}
