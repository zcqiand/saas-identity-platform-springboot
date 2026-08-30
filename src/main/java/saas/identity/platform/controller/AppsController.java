package saas.identity.platform.controller;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.repository.AppRepository;
import saas.identity.shared.api.AppsApi;
import saas.identity.shared.dto.AppPublicInfo;
import saas.identity.shared.dto.AppStatus;

/**
 * /api/v1/apps/{code} - M04.F01 公共读侧：按 appCode 返回应用公开信息.
 *
 * 2026-08-30 contract-test M96.F02.I06: 字节对齐 msw/aspnetcore/nextjs. 字段:
 * id/code/name/description/icon/status (不带 OAuth 集成字段).
 */
@RestController
public class AppsController implements AppsApi {

  private final AppRepository appRepository;

  public AppsController(AppRepository appRepository) {
    this.appRepository = appRepository;
  }

  @Override
  public ResponseEntity<AppPublicInfo> appsGetApp(String code) {
    Optional<AppEntity> opt = appRepository.findByCode(code);
    if (opt.isEmpty() || opt.get().getStatus() != saas.identity.platform.enums.AppStatus.ACTIVE) {
      return ResponseEntity.status(404)
          .body(new AppPublicInfo().code("NOT_FOUND").name("App not found"));
    }
    return ResponseEntity.ok(toDto(opt.get()));
  }

  private AppPublicInfo toDto(AppEntity e) {
    AppPublicInfo dto = new AppPublicInfo();
    dto.setId(e.getId());
    dto.setCode(e.getCode());
    dto.setName(e.getName());
    dto.setDescription(e.getDescription());
    dto.setIcon(e.getIcon());
    dto.setStatus(AppStatus.ACTIVE);
    return dto;
  }
}