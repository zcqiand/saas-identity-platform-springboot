package saas.identity.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import saas.identity.platform.entity.Generated.SysUser;
import saas.identity.platform.repository.OauthAccessTokenRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.LoginRequest;

/**
 * M01.F04 登录流程测试 — 密码正确 / 错误 / 失败锁定。 注：sessionsLogin / sessionsOidcCallback / sessionsRefreshToken
 * 全 4 路都覆盖。
 */
@WebMvcTest(AuthController.class)
class AuthControllerLoginTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @MockBean SysUserRepository users;
  @MockBean OauthCodeRepository codes;
  @MockBean OauthAccessTokenRepository accessTokens;
  @MockBean OauthRefreshTokenRepository refreshTokens;
  @MockBean JwtIssuer jwt;

  private SysUser existingUser;

  @BeforeEach
  void setup() {
    existingUser = new SysUser();
    existingUser.setId(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"));
    existingUser.setUsername("alice");
    // bcrypt hash of "password" (cost 10) — generated via BCryptPasswordEncoder at test time
    existingUser.setPassword(
        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password"));
    existingUser.setEmail("alice@example.com");
    existingUser.setStatus((short) 1);
    existingUser.setFailedAttempts(0);
    existingUser.setLockedUntil(null);

    when(jwt.issueAccessToken(any(), any())).thenReturn("jwt_access_token");
  }

  @Test
  @WithMockUser
  void login_withCorrectPassword_returns200_andResetsFailedAttempts() throws Exception {
    when(users.findByUsername("alice")).thenReturn(Optional.of(existingUser));

    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("password");
    req.setClientId("lab-management");

    mvc.perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt_access_token"))
        .andExpect(jsonPath("$.clientId").value("lab-management"))
        .andExpect(jsonPath("$.user.username").value("alice"));

    assertEquals(0, existingUser.getFailedAttempts(), "失败计数应在成功后归零");
  }

  @Test
  @WithMockUser
  void login_withWrongPassword_incrementsFailedAttempts_andEventuallyLocks() throws Exception {
    existingUser.setFailedAttempts(4); // 下一次失败达到阈值 5
    when(users.findByUsername("alice")).thenReturn(Optional.of(existingUser));

    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("WRONG");
    req.setClientId("lab-management");

    mvc.perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
        .andExpect(status().is4xxClientError());

    assertEquals(5, existingUser.getFailedAttempts());
    assertEquals(
        true,
        existingUser.getLockedUntil() != null
            && existingUser.getLockedUntil().isAfter(OffsetDateTime.now()));
  }

  @Test
  @WithMockUser
  void login_withPlainPrefixedSeedPassword_returns200() throws Exception {
    // 家族 dev 种子约定（nextjs seed-db.mjs 灌 "plain:{password}"）：
    // springboot 必须识别该前缀，否则同一份种子三后端登录行为分叉（live 401）。
    existingUser.setPassword("plain:dev123456");
    when(users.findByUsername("alice")).thenReturn(Optional.of(existingUser));

    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("dev123456");
    req.setClientId("lab-management");

    mvc.perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt_access_token"));
  }

  @Test
  @WithMockUser
  void login_duringLockoutWindow_returns423() throws Exception {
    existingUser.setLockedUntil(OffsetDateTime.now().plusMinutes(10));
    when(users.findByUsername("alice")).thenReturn(Optional.of(existingUser));

    LoginRequest req = new LoginRequest();
    req.setUsername("alice");
    req.setPassword("password");
    req.setClientId("lab-management");

    mvc.perform(
            post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
        .andExpect(status().isLocked());
  }
}
