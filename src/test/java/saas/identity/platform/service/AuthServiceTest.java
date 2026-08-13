package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import saas.identity.platform.entity.TenantEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.enums.TenantStatus;
import saas.identity.platform.enums.UserStatus;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;
import saas.identity.shared.dto.OidcCallbackRequest;
import saas.identity.shared.dto.TokenRequest;

/** AuthService 单测（M03.F01 登录 + M03.F02 OIDC/refresh + M03.F03 登出）。 */
class AuthServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final TenantRepository tenantRepository = mock(TenantRepository.class);
  private final AuthService service = new AuthService(userRepository, tenantRepository);

  private UserEntity activeUser(UUID tenantId, UUID userId) {
    UserEntity u = new UserEntity();
    u.setId(userId);
    u.setTenantId(tenantId);
    u.setUsername("alice");
    u.setPasswordHash("plain:secret");
    u.setStatus(UserStatus.ACTIVE);
    return u;
  }

  @Test
  @Fn({"M03.F01.I01"})
  void login_success_returnsToken() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    TenantEntity t = new TenantEntity();
    t.setId(tenantId);
    t.setCode("ACME");
    t.setStatus(TenantStatus.ACTIVE);
    // login 用 tenantCode.toString() 作为 code 查库
    when(tenantRepository.findByCode(tenantId.toString())).thenReturn(Optional.of(t));
    when(userRepository.findByTenantIdAndUsername(tenantId, "alice"))
        .thenReturn(Optional.of(activeUser(tenantId, userId)));

    LoginResponse resp =
        service.login(new LoginRequest().tenantCode(tenantId).username("alice").password("secret"));
    assertNotNull(resp.getAccessToken());
    assertEquals(userId, resp.getUserId());
    assertEquals(tenantId, resp.getCurrentTenantId());
  }

  @Test
  @Fn({"M03.F01.I02"})
  void login_badPassword_throws() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    TenantEntity t = new TenantEntity();
    t.setId(tenantId);
    t.setCode("ACME");
    t.setStatus(TenantStatus.ACTIVE);
    when(tenantRepository.findByCode(tenantId.toString())).thenReturn(Optional.of(t));
    when(userRepository.findByTenantIdAndUsername(tenantId, "alice"))
        .thenReturn(Optional.of(activeUser(tenantId, userId)));

    assertThrows(
        SecurityException.class,
        () ->
            service.login(
                new LoginRequest().tenantCode(tenantId).username("alice").password("wrong")));
  }

  @Test
  @Fn({"M03.F01.I02"})
  void login_unknownUser_throws() {
    UUID tenantId = UUID.randomUUID();
    when(tenantRepository.findByCode(tenantId.toString())).thenReturn(Optional.empty());
    when(userRepository.findByTenantIdAndUsername(any(UUID.class), eq("nobody")))
        .thenReturn(Optional.empty());
    assertThrows(
        SecurityException.class,
        () ->
            service.login(
                new LoginRequest().tenantCode(tenantId).username("nobody").password("x")));
  }

  @Test
  @Fn({"M03.F02.I04"})
  void refresh_returnsNewToken() {
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(tenantId, userId)));
    TokenRequest body = new TokenRequest().refreshToken("refresh-" + userId + "-123");
    var resp = service.refresh(body);
    assertNotNull(resp.getAccessToken());
    assertEquals("Bearer", resp.getTokenType());
  }

  @Test
  @Fn({"M03.F02.I04"})
  void refresh_badToken_throws() {
    TokenRequest body = new TokenRequest().refreshToken("garbage");
    assertThrows(SecurityException.class, () -> service.refresh(body));
  }

  @Test
  @Fn({"M03.F02.I03"})
  void oidcCallback_returnsMockToken() {
    var resp = service.oidcCallback(new OidcCallbackRequest().code("c").state("s"));
    assertNotNull(resp.getAccessToken());
    assertEquals("Bearer", resp.getTokenType());
  }

  @Test
  @Fn({"M03.F03.I05"})
  void logout_isNoop() {
    service.logout(); // 不抛异常即通过
  }
}
