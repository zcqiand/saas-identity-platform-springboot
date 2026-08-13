package saas.identity.platform.service;

import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.mapper.AppMapper;
import saas.identity.platform.repository.AppRepository;
import saas.identity.shared.dto.CreateOAuthAppRequest;
import saas.identity.shared.dto.OAuthApp;
import saas.identity.shared.dto.UpdateOAuthAppRequest;

/** M07.F01 + M07.F02.I06 — 平台 admin 应用 CRUD + 启停用。 v0.4.0：从 InMemoryStore 迁到 AppRepository。 */
@Service
public class AdminOauthAppService {

  private final AppRepository appRepository;

  public AdminOauthAppService(AppRepository appRepository) {
    this.appRepository = appRepository;
  }

  @Transactional(readOnly = true)
  public Page<OAuthApp> list(int page, int pageSize) {
    return appRepository.findAll(PageRequest.of(page, pageSize)).map(AppMapper::toDto);
  }

  @Transactional
  public OAuthApp create(CreateOAuthAppRequest body) {
    AppEntity e = AppMapper.fromCreateRequest(body);
    return AppMapper.toDto(appRepository.save(e));
  }

  @Transactional(readOnly = true)
  public OAuthApp get(UUID id) {
    return AppMapper.toDto(
        appRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("app " + id + " not found")));
  }

  @Transactional
  public OAuthApp update(UUID id, UpdateOAuthAppRequest body) {
    AppEntity e =
        appRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("app " + id + " not found"));
    AppMapper.applyUpdate(e, body);
    return AppMapper.toDto(appRepository.save(e));
  }

  @Transactional
  public void delete(UUID id) {
    appRepository.findById(id).ifPresent(appRepository::delete);
  }

  @Transactional
  public OAuthApp setStatus(UUID id, saas.identity.platform.enums.AppStatus status) {
    AppEntity e =
        appRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("app " + id + " not found"));
    e.setStatus(
        status == saas.identity.platform.enums.AppStatus.ACTIVE
            ? saas.identity.platform.enums.AppStatus.ACTIVE
            : saas.identity.platform.enums.AppStatus.DISABLED);
    return AppMapper.toDto(appRepository.save(e));
  }
}
