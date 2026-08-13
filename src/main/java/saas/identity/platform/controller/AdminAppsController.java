package saas.identity.platform.controller;

import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.enums.AppStatus;
import saas.identity.platform.mapper.AppMapper;
import saas.identity.platform.repository.AppRepository;
import saas.identity.shared.api.AdminAppsApi;
import saas.identity.shared.dto.AdminAppsListApps200Response;
import saas.identity.shared.dto.AdminAppsSetAppStatusRequest;
import saas.identity.shared.dto.App;
import saas.identity.shared.dto.CreateAppRequest;
import saas.identity.shared.dto.UpdateAppRequest;

/**
 * M04.F01 — 平台 admin 应用 CRUD。 平台级（非 tenant-scoped），不走 TenantGuard。 业务 inline（dev unblock），手写
 * CRUD：list / get / create / update / delete / setStatus。
 */
@RestController
public class AdminAppsController implements AdminAppsApi {

  private static final int PAGE_DEFAULT = 0;
  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final AppRepository repo;

  public AdminAppsController(AppRepository repo) {
    this.repo = repo;
  }

  private int normPage(Integer page) {
    return page == null ? PAGE_DEFAULT : Math.max(0, page);
  }

  private int normPageSize(Integer pageSize) {
    return pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
  }

  @Override
  public ResponseEntity<AdminAppsListApps200Response> adminAppsListApps(
      Integer page, Integer pageSize) {
    int p = normPage(page);
    int ps = normPageSize(pageSize);
    var pageReq = PageRequest.of(p, ps, Sort.by(Sort.Direction.DESC, "createdAt"));
    var result = repo.findAll(pageReq);
    var body =
        new AdminAppsListApps200Response()
            .items(result.getContent().stream().map(AppMapper::toDto).toList())
            .page(p)
            .pageSize(ps)
            .total(result.getTotalElements());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<App> adminAppsCreateApp(CreateAppRequest createAppRequest) {
    var e = AppMapper.fromCreateRequest(createAppRequest);
    var saved = repo.save(e);
    return ResponseEntity.ok(AppMapper.toDto(saved));
  }

  @Override
  public ResponseEntity<App> adminAppsGetApp(String appId) {
    // appId 路径参数兼容 Guid 或 code slug（AdminAppMenusController 同款约定）
    var e = resolveApp(appId);
    return ResponseEntity.ok(AppMapper.toDto(e));
  }

  @Override
  public ResponseEntity<App> adminAppsUpdateApp(String appId, UpdateAppRequest updateAppRequest) {
    var e = resolveApp(appId);
    AppMapper.applyUpdate(e, updateAppRequest);
    var saved = repo.save(e);
    return ResponseEntity.ok(AppMapper.toDto(saved));
  }

  @Override
  public ResponseEntity<Void> adminAppsDeleteApp(String appId) {
    var e = resolveApp(appId);
    repo.delete(e);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<App> adminAppsSetAppStatus(
      String appId, AdminAppsSetAppStatusRequest adminAppsSetAppStatusRequest) {
    var e = resolveApp(appId);
    var shared = adminAppsSetAppStatusRequest.getStatus();
    if (shared == null) {
      // null body — 视为 active 占位（与 AdminTenantController.setStatus 行为对齐）
      e.setStatus(AppStatus.ACTIVE);
    } else {
      e.setStatus(AppStatus.valueOf(shared.name()));
    }
    var saved = repo.save(e);
    return ResponseEntity.ok(AppMapper.toDto(saved));
  }

  /**
   * appId 路径参数兼容 Guid 或 code slug（"lab-management"）。 与 AdminAppMenusController 同款约定——前端
   * MenuTreePage 混用 MSW fixtures（语义键）与 API。
   */
  private saas.identity.platform.entity.AppEntity resolveApp(String appIdOrCode) {
    try {
      var gid = UUID.fromString(appIdOrCode);
      var byId = repo.findById(gid);
      if (byId.isPresent()) return byId.get();
    } catch (IllegalArgumentException ignored) {
      // 不是 UUID，按 code 查
    }
    return repo.findAll().stream()
        .filter(a -> appIdOrCode.equals(a.getCode()))
        .findFirst()
        .orElseThrow(
            () -> new java.util.NoSuchElementException("app '" + appIdOrCode + "' not found"));
  }
}
