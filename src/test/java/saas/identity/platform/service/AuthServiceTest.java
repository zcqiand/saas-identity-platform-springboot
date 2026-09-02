package saas.identity.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;
import saas.identity.shared.dto.OidcCallbackRequest;
import saas.identity.shared.dto.TokenRequest;

/** AuthService 单测（M03.F01 登录 + M03.F02 OIDC/refresh + M03.F03 登出）。 */
class AuthServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final TenantRepository tenantRepository = mock(TenantRepository.class);
  private final AuditWriter auditWriter = mock(AuditWriter.class);
  // 与 SecurityConfig.jwtDecoder 同 key：login 签出的 token 必须能被本仓 decoder 验过
  private final JwtIssuer jwt =
      new JwtIssuer("unit-test-signing-key-0123456789abcdef0123", "ut-issuer", "ut-aud", 3600);
  private final AuthService service =
      new AuthService(userRepository, tenantRepository, jwt, auditWriter);

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
    // 回归 2026-08-28 线上 401：login 必须发 HS256 真签 token（alg=none + .dev-placeholder
    // 会被 SecurityConfig.jwtDecoder 拒 → 前端拿到假 token 调业务接口全 401）
    String token = resp.getAccessToken();
    assertNotNull(token);
    assertEquals(3, token.split("\\.").length, "HS256 JWT 应为三段");
    String headerJson =
        new String(
            java.util.Base64.getUrlDecoder().decode(token.split("\\.")[0]),
            java.nio.charset.StandardCharsets.UTF_8);
    assertTrue(headerJson.contains("HS256"), "alg 必须是 HS256，实际 header=" + headerJson);
    assertFalse(token.endsWith("dev-placeholder"), "不得再发 dev-placeholder 假签");
    assertEquals(userId, resp.getUserId());
    assertEquals(tenantId, resp.getCurrentTenantId());
  }

  // 2026-09-02 contract-test M96 audit 覆盖对齐：login 成功必须写 login_success 审计事件
  // （msw/nextjs 已写，本仓此前缺失 → audit 列表 4 后端对前端不可区分破裂）。
  // 形状对齐 nextjs app/api/v1/auth/login/route.ts：actor=target=登录用户，metadata={username}。
  @Test
  @Fn({"M03.F01.I01"})
  void login_success_writesAuditEvent() {
    UUID tenantId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    TenantEntity t = new TenantEntity();
    t.setId(tenantId);
    t.setCode("ACME");
    t.setStatus(TenantStatus.ACTIVE);
    when(tenantRepository.findByCode(tenantId.toString())).thenReturn(Optional.of(t));
    when(userRepository.findByTenantIdAndUsername(tenantId, "alice"))
        .thenReturn(Optional.of(activeUser(tenantId, userId)));

    service.login(new LoginRequest().tenantCode(tenantId).username("alice").password("secret"));

    verify(auditWriter)
        .write(
            eq(tenantId),
            eq(userId),
            eq("login_success"),
            eq(null),
            eq(java.util.Map.of("username", "alice")));
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
    // 2026-08-31 contract-test M96.F02.I24：refresh 无效 token 家族统一 400（IllegalArgumentException）
    assertThrows(IllegalArgumentException.class, () -> service.refresh(body));
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
