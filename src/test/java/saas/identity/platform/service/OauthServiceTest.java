package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.entity.OauthCodeEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.enums.AppStatus;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.AppRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.AuthorizeCodeRequest;
import saas.identity.shared.dto.OAuthAuthorize200Response;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/** M04.F02 OAuth Phase 6 — 授权码签发 + 令牌交换 / 刷新 单测。 */
@ExtendWith(MockitoExtension.class)
class OauthServiceTest {

  private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
  private static final UUID APP_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Mock AppRepository appRepository;
  @Mock OauthCodeRepository oauthCodeRepository;
  @Mock UserRepository userRepository;
  @Mock JdbcTemplate jdbcTemplate;
  // real JwtIssuer with test signing key (≥32 bytes)
  JwtIssuer jwtIssuer;

  OauthService service;

  @BeforeEach
  void setup() {
    jwtIssuer =
        new JwtIssuer(
            "test-key-32-bytes-minimum-length-xyz12345",
            "saas-identity-platform",
            "saas-identity-platform-clients",
            3600L);
    service =
        new OauthService(
            appRepository, oauthCodeRepository, userRepository, jwtIssuer, jdbcTemplate);
    // 默认 stub: redirect_uris + scopes 都各返回 1 个值（覆盖 happy path）
    lenient()
        .when(
            jdbcTemplate.queryForList(
                org.mockito.ArgumentMatchers.contains("redirect_uris"),
                eq(String.class),
                any(UUID.class)))
        .thenReturn(java.util.List.of("https://lab-vue.xiangru.uk/login"));
    lenient()
        .when(
            jdbcTemplate.queryForList(
                org.mockito.ArgumentMatchers.contains("scopes"), eq(String.class), any(UUID.class)))
        .thenReturn(java.util.List.of("lab.read", "lab.write"));
  }

  private AppEntity mockApp() {
    AppEntity app = new AppEntity();
    app.setId(APP_ID);
    app.setCode("lab-management");
    app.setClientId(APP_ID.toString()); // V014: client_id = app.id UUID
    app.setStatus(AppStatus.ACTIVE);
    return app;
  }

  private UserEntity mockUser() {
    UserEntity user = new UserEntity();
    user.setId(USER_ID);
    user.setTenantId(TENANT_ID);
    user.setUsername("test-user");
    user.setEmail("test@lab.local");
    return user;
  }

  // ===== M04.F02.I06 Authorize =====

  @Test
  @Fn({"M04.F02.I06"})
  void authorize_happyPath_returnsCode() {
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    when(oauthCodeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    AuthorizeCodeRequest req = new AuthorizeCodeRequest();
    req.setClientId(APP_ID);
    req.setRedirectUri("https://lab-vue.xiangru.uk/login");
    req.setResponseType(AuthorizeCodeRequest.ResponseTypeEnum.CODE);
    req.setScope("lab.read");
    req.setState("test-state");
    req.setTenantId(TENANT_ID);

    OAuthAuthorize200Response res = service.authorize(req);
    assertNotNull(res.getCode());
    assertTrue(res.getCode().startsWith("saas-code-"));
    assertEquals("test-state", res.getState());
  }

  @Test
  @Fn({"M04.F02.I06"})
  void authorize_invalidClient_throws() {
    when(appRepository.findByClientId(any())).thenReturn(Optional.empty());

    AuthorizeCodeRequest req = new AuthorizeCodeRequest();
    req.setClientId(UUID.randomUUID());
    req.setRedirectUri("https://lab-vue.xiangru.uk/login");
    req.setResponseType(AuthorizeCodeRequest.ResponseTypeEnum.CODE);
    req.setScope("lab.read");
    req.setState("x");
    req.setTenantId(TENANT_ID);

    assertThrows(OauthService.InvalidClientException.class, () -> service.authorize(req));
  }

  @Test
  @Fn({"M04.F02.I06"})
  void authorize_inactiveApp_throws() {
    AppEntity app = mockApp();
    app.setStatus(AppStatus.DISABLED);
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(app));

    AuthorizeCodeRequest req = new AuthorizeCodeRequest();
    req.setClientId(APP_ID);
    req.setRedirectUri("https://lab-vue.xiangru.uk/login");
    req.setResponseType(AuthorizeCodeRequest.ResponseTypeEnum.CODE);
    req.setScope("lab.read");
    req.setState("x");
    req.setTenantId(TENANT_ID);

    assertThrows(OauthService.InvalidClientException.class, () -> service.authorize(req));
  }

  // ===== M04.F02.I07 Token authorization_code =====

  @Test
  @Fn({"M04.F02.I07"})
  void token_authorizationCode_happyPath() {
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    Page<UserEntity> userPage = new PageImpl<>(List.of(mockUser()));
    when(userRepository.findByTenantId(eq(TENANT_ID), any(Pageable.class))).thenReturn(userPage);

    OauthCodeEntity existing = new OauthCodeEntity();
    existing.setCode("test-code");
    existing.setGrantType("authorization_code");
    existing.setAppId(APP_ID);
    existing.setTenantId(TENANT_ID);
    existing.setRedirectUri("https://lab-vue.xiangru.uk/login");
    existing.setScope("lab.read");
    existing.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
    when(oauthCodeRepository.findByCode("test-code")).thenReturn(Optional.of(existing));
    when(oauthCodeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.AUTHORIZATION_CODE);
    req.setCode("test-code");
    req.setClientId(APP_ID);
    req.setTenantId(TENANT_ID);
    req.setRedirectUri("https://lab-vue.xiangru.uk/login");

    TokenResponse res = service.token(req);
    assertNotNull(res.getAccessToken());
    assertTrue(res.getAccessToken().startsWith("ey"));
    assertNotNull(res.getRefreshToken());
    assertTrue(res.getRefreshToken().startsWith("saas-rt-"));
    assertEquals("Bearer", res.getTokenType());
    assertEquals("lab.read", res.getScope());
  }

  @Test
  @Fn({"M04.F02.I07"})
  void token_alreadyConsumed_throws() {
    OauthCodeEntity consumed = new OauthCodeEntity();
    consumed.setCode("c1");
    consumed.setGrantType("authorization_code");
    consumed.setAppId(APP_ID);
    consumed.setTenantId(TENANT_ID);
    consumed.setRedirectUri("https://lab-vue.xiangru.uk/login");
    consumed.setScope("lab.read");
    consumed.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
    consumed.setConsumedAt(OffsetDateTime.now().minusSeconds(5));
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    when(oauthCodeRepository.findByCode("c1")).thenReturn(Optional.of(consumed));

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.AUTHORIZATION_CODE);
    req.setCode("c1");
    req.setClientId(APP_ID);
    req.setTenantId(TENANT_ID);
    req.setRedirectUri("https://lab-vue.xiangru.uk/login");

    assertThrows(OauthService.InvalidGrantException.class, () -> service.token(req));
  }

  @Test
  @Fn({"M04.F02.I07"})
  void token_expired_throws() {
    OauthCodeEntity expired = new OauthCodeEntity();
    expired.setCode("e1");
    expired.setGrantType("authorization_code");
    expired.setAppId(APP_ID);
    expired.setTenantId(TENANT_ID);
    expired.setRedirectUri("https://lab-vue.xiangru.uk/login");
    expired.setScope("lab.read");
    expired.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    when(oauthCodeRepository.findByCode("e1")).thenReturn(Optional.of(expired));

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.AUTHORIZATION_CODE);
    req.setCode("e1");
    req.setClientId(APP_ID);
    req.setTenantId(TENANT_ID);
    req.setRedirectUri("https://lab-vue.xiangru.uk/login");

    assertThrows(OauthService.InvalidGrantException.class, () -> service.token(req));
  }

  @Test
  @Fn({"M04.F02.I07"})
  void token_redirectUriMismatch_throws() {
    OauthCodeEntity code = new OauthCodeEntity();
    code.setCode("m1");
    code.setGrantType("authorization_code");
    code.setAppId(APP_ID);
    code.setTenantId(TENANT_ID);
    code.setRedirectUri("https://lab-vue.xiangru.uk/login");
    code.setScope("lab.read");
    code.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    when(oauthCodeRepository.findByCode("m1")).thenReturn(Optional.of(code));

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.AUTHORIZATION_CODE);
    req.setCode("m1");
    req.setClientId(APP_ID);
    req.setTenantId(TENANT_ID);
    req.setRedirectUri("https://attacker.example.com/callback");

    assertThrows(OauthService.InvalidGrantException.class, () -> service.token(req));
  }

  // ===== M04.F02.I08 Token refresh_token =====

  @Test
  @Fn({"M04.F02.I08"})
  void token_refreshToken_happyPath() {
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    OauthCodeEntity refresh = new OauthCodeEntity();
    refresh.setCode("rt1");
    refresh.setGrantType("refresh_token");
    refresh.setAppId(APP_ID);
    refresh.setUserId(USER_ID);
    refresh.setTenantId(TENANT_ID);
    refresh.setScope("lab.read");
    refresh.setExpiresAt(OffsetDateTime.now().plusDays(7));
    when(oauthCodeRepository.findByCode("rt1")).thenReturn(Optional.of(refresh));
    when(oauthCodeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.REFRESH_TOKEN);
    req.setRefreshToken("rt1");
    req.setClientId(APP_ID);
    req.setTenantId(TENANT_ID);

    TokenResponse res = service.token(req);
    assertNotNull(res.getAccessToken());
    assertNotEquals("rt1", res.getRefreshToken());
  }

  @Test
  @Fn({"M04.F02.I08"})
  void token_refreshTokenReuse_throws() {
    OauthCodeEntity refresh = new OauthCodeEntity();
    refresh.setCode("rt2");
    refresh.setGrantType("refresh_token");
    refresh.setAppId(APP_ID);
    refresh.setUserId(USER_ID);
    refresh.setTenantId(TENANT_ID);
    refresh.setScope("lab.read");
    refresh.setExpiresAt(OffsetDateTime.now().plusDays(7));
    refresh.setConsumedAt(OffsetDateTime.now().minusSeconds(5));
    when(appRepository.findByClientId(APP_ID.toString())).thenReturn(Optional.of(mockApp()));
    when(oauthCodeRepository.findByCode("rt2")).thenReturn(Optional.of(refresh));

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.REFRESH_TOKEN);
    req.setRefreshToken("rt2");
    req.setClientId(APP_ID);
    req.setTenantId(TENANT_ID);

    assertThrows(OauthService.InvalidGrantException.class, () -> service.token(req));
  }
}
