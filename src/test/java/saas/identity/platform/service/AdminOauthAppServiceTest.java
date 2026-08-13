package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.enums.AppStatus;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.AppRepository;
import saas.identity.shared.dto.CreateOAuthAppRequest;
import saas.identity.shared.dto.OAuthApp;
import saas.identity.shared.dto.UpdateOAuthAppRequest;

/** AdminOauthAppService 单测（M04.F01 OAuth 应用 CRUD）。authorize/token 是 Controller 层 mock，不在此测。 */
class AdminOauthAppServiceTest {

  private final AppRepository appRepository = mock(AppRepository.class);
  private final AdminOauthAppService service = new AdminOauthAppService(appRepository);

  private AppEntity entity(UUID id) {
    AppEntity e = new AppEntity();
    e.setId(id);
    e.setCode("portal");
    e.setName("Portal");
    e.setClientId("client-1");
    e.setStatus(AppStatus.ACTIVE);
    e.setRedirectUris(List.of());
    e.setScopes(List.of());
    e.setGrantTypes(List.of());
    return e;
  }

  @Test
  @Fn({"M04.F01.I01"})
  void list_returnsPage() {
    Page<AppEntity> page = new PageImpl<>(List.of(entity(UUID.randomUUID())));
    when(appRepository.findAll(any(Pageable.class))).thenReturn(page);
    Page<OAuthApp> result = service.list(0, 20);
    assertEquals(1, result.getContent().size());
    assertEquals("Portal", result.getContent().get(0).getName());
  }

  @Test
  @Fn({"M04.F01.I02"})
  void create_returnsApp() {
    when(appRepository.save(any(AppEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    CreateOAuthAppRequest body =
        new CreateOAuthAppRequest().name("Portal").redirectUris(List.of("https://x/cb"));
    OAuthApp app = service.create(body);
    assertEquals("Portal", app.getName());
  }

  @Test
  @Fn({"M04.F01.I03"})
  void get_returnsApp() {
    UUID id = UUID.randomUUID();
    when(appRepository.findById(id)).thenReturn(Optional.of(entity(id)));
    OAuthApp app = service.get(id);
    assertEquals(id, app.getId());
    assertEquals("Portal", app.getName());
  }

  @Test
  @Fn({"M04.F01.I03"})
  void get_throwsIfMissing() {
    UUID id = UUID.randomUUID();
    when(appRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(java.util.NoSuchElementException.class, () -> service.get(id));
  }

  @Test
  @Fn({"M04.F01.I04"})
  void update_appliesAndSaves() {
    UUID id = UUID.randomUUID();
    when(appRepository.findById(id)).thenReturn(Optional.of(entity(id)));
    when(appRepository.save(any(AppEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    UpdateOAuthAppRequest body = new UpdateOAuthAppRequest().name("Portal II");
    OAuthApp app = service.update(id, body);
    assertEquals("Portal II", app.getName());
  }

  @Test
  @Fn({"M04.F01.I05"})
  void delete_removesWhenPresent() {
    UUID id = UUID.randomUUID();
    AppEntity e = entity(id);
    when(appRepository.findById(id)).thenReturn(Optional.of(e));
    service.delete(id);
    verify(appRepository).delete(e);
  }

  @Test
  @Fn({"M04.F01.I05"})
  void setStatus_toggles() {
    UUID id = UUID.randomUUID();
    when(appRepository.findById(id)).thenReturn(Optional.of(entity(id)));
    when(appRepository.save(any(AppEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    OAuthApp app = service.setStatus(id, AppStatus.DISABLED);
    assertEquals(id, app.getId());
    verify(appRepository).save(any(AppEntity.class));
  }
}
