package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.security.TenantGuard;
import saas.identity.platform.service.TenantAuditService;
import saas.identity.shared.api.TenantAuditApi;
import saas.identity.shared.dto.AuditAction;
import saas.identity.shared.dto.TenantAuditExportAuditEvents200Response;
import saas.identity.shared.dto.TenantAuditExportAuditEventsRequest;
import saas.identity.shared.dto.TenantAuditGetRetentionPolicy200Response;
import saas.identity.shared.dto.TenantAuditListAuditEvents200Response;

/**
 * M06.F01 + M06.F02 — 审计事件查询 + 留存策略。 tenant-scoped：每个方法首调 tenantGuard.verifyPathTenant。 业务在 {@link
 * TenantAuditService}。
 *
 * <p>byUser 复用 list 过滤（actorUserId=userId）；export 为 Phase 5 mock（与 aspnetcore 对齐）。
 */
@RestController
public class TenantAuditController implements TenantAuditApi {

  private static final int PAGE_DEFAULT = 0;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final TenantAuditService service;
  private final TenantGuard tenantGuard;

  public TenantAuditController(TenantAuditService service, TenantGuard tenantGuard) {
    this.service = service;
    this.tenantGuard = tenantGuard;
  }

  private int normPage(Integer page) {
    return page == null ? PAGE_DEFAULT : Math.max(0, page);
  }

  private int normPageSize(Integer pageSize) {
    return pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
  }

  @Override
  public ResponseEntity<TenantAuditListAuditEvents200Response> tenantAuditListAuditEvents(
      String tenantId,
      Integer page,
      Integer pageSize,
      String actorUserId,
      AuditAction action,
      OffsetDateTime from,
      OffsetDateTime to) {
    tenantGuard.verifyPathTenant(tenantId);
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    UUID tid = UUID.fromString(tenantId);
    UUID actor = actorUserId == null ? null : UUID.fromString(actorUserId);
    var result = service.list(tid, p, ps, actor, action, from, to);
    var body =
        new TenantAuditListAuditEvents200Response()
            .items(result.getContent())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<TenantAuditListAuditEvents200Response> tenantAuditListAuditEventsByUser(
      String tenantId, String userId, Integer page, Integer pageSize) {
    tenantGuard.verifyPathTenant(tenantId);
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    UUID tid = UUID.fromString(tenantId);
    UUID uid = UUID.fromString(userId);
    // 复用 list：按 actorUserId 过滤
    var result = service.list(tid, p, ps, uid, null, null, null);
    var body =
        new TenantAuditListAuditEvents200Response()
            .items(result.getContent())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  // M06.F01.I03 导出（CSV/JSON URL）— Phase 5 占位
  @Override
  public ResponseEntity<TenantAuditExportAuditEvents200Response> tenantAuditExportAuditEvents(
      String tenantId, TenantAuditExportAuditEventsRequest tenantAuditExportAuditEventsRequest) {
    tenantGuard.verifyPathTenant(tenantId);
    String format =
        tenantAuditExportAuditEventsRequest.getFormat() == null
            ? "json"
            : tenantAuditExportAuditEventsRequest
                .getFormat()
                .toString()
                .toLowerCase(java.util.Locale.ROOT);
    String url =
        "https://example.com/audit-export-"
            + tenantId
            + "-"
            + OffsetDateTime.now().toEpochSecond()
            + "."
            + format;
    return ResponseEntity.ok(new TenantAuditExportAuditEvents200Response().downloadUrl(url));
  }

  @Override
  public ResponseEntity<TenantAuditGetRetentionPolicy200Response> tenantAuditGetRetentionPolicy(
      String tenantId) {
    tenantGuard.verifyPathTenant(tenantId);
    int days = service.getRetention(UUID.fromString(tenantId));
    return ResponseEntity.ok(new TenantAuditGetRetentionPolicy200Response().retentionDays(days));
  }

  @Override
  public ResponseEntity<TenantAuditGetRetentionPolicy200Response> tenantAuditSetRetentionPolicy(
      String tenantId,
      TenantAuditGetRetentionPolicy200Response tenantAuditGetRetentionPolicy200Response) {
    tenantGuard.verifyPathTenant(tenantId);
    int days =
        tenantAuditGetRetentionPolicy200Response.getRetentionDays() == null
            ? 90
            : tenantAuditGetRetentionPolicy200Response.getRetentionDays();
    service.setRetention(UUID.fromString(tenantId), days);
    return ResponseEntity.ok(new TenantAuditGetRetentionPolicy200Response().retentionDays(days));
  }
}
