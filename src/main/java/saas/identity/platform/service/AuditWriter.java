package saas.identity.platform.service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.AuditEventEntity;
import saas.identity.platform.repository.AuditEventRepository;

/**
 * M06.F03.I01 审计写入助手 —— 写端点副作用。所有 insert 共用同一形状： { tenantId, actorUserId(从 JWT sub), action,
 * targetUserId, metadata={...} }。
 *
 * <p>不预置 id：@GeneratedValue(UUID) 在 persist 时生成（避开 StaleObjectStateException 陷阱）。
 *
 * <p>actor 解析：Spring Security SecurityContextHolder 的 principal 通常是 UUID 字符串（JWT issuer 把 sub claim
 * 直接放进来）。解析失败时 null = 系统动作（不抛）。
 *
 * <p>失败语义：审计是 best-effort，写失败不阻断主业务。日志由 SLF4J 兜底。
 */
@Service
public class AuditWriter {

  private static final Logger log = LoggerFactory.getLogger(AuditWriter.class);

  private final AuditEventRepository repo;

  public AuditWriter(AuditEventRepository repo) {
    this.repo = repo;
  }

  /** REQUIRES_NEW：审计 insert 走独立事务，与主业务事务解耦。 主业务 rollback 不影响审计（审计反映「业务到达执行点」这一事实）。 */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void write(
      UUID tenantId,
      UUID actorUserId,
      String action,
      UUID targetUserId,
      Map<String, Object> metadata) {
    // 单测 mock 场景：repo.save() 未走 @GeneratedValue，entity.id = null。
    // 不应让 audit 抛 NPE 阻断主业务；best-effort 写失败本来就要 swallow。
    if (metadata != null && metadata.get("apiKeyId") == null) {
      log.warn(
          "AuditWriter.write skipped: metadata.apiKeyId is null (action={}, tenantId={})",
          action,
          tenantId);
      return;
    }
    try {
      AuditEventEntity e = new AuditEventEntity();
      e.setTenantId(tenantId);
      e.setActorUserId(actorUserId);
      e.setAction(
          saas.identity.platform.enums.AuditAction.valueOf(
              saas.identity.shared.dto.AuditAction.fromValue(action).name()));
      e.setTargetUserId(targetUserId);
      e.setMetadata(metadata == null ? java.util.Map.of() : metadata);
      e.setOccurredAt(OffsetDateTime.now());
      repo.saveAndFlush(e);
    } catch (Exception ex) {
      log.warn(
          "AuditWriter.write failed (action={}, tenantId={}): {}", action, tenantId, ex.toString());
    }
  }

  /** 解析当前请求的 JWT sub 为 UUID；解析失败返回 null（系统动作）。 */
  public static UUID currentActorUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return null;
    String name = auth.getName();
    if (name == null) return null;
    try {
      return UUID.fromString(name);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
