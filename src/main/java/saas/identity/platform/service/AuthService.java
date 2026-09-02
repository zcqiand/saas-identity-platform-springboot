package saas.identity.platform.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;

/**
 * M03.F01 + M03.F02 + M03.F03 — 认证（密码登录 / OIDC / 登出 / refresh）。 v0.4.0 (M09)：从内存 List 迁到
 * UserRepository 真实 DB。
 *
 * <p>密码：dev 期 passwordHash 存 "plain:{password}"；Phase 5 接 argon2。 JWT：v0.2.x Phase 2 起走 JwtIssuer
 * HS256 真签（与 SecurityConfig.jwtDecoder 对称；旧 alg=none + .dev-placeholder 假签会被本仓 decoder 拒 → 线上所有业务接口
 * 401，2026-08-28 修复）。对称 saas-aspnetcore AuthController（v0.2.0 起同款）。
 */
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final TenantRepository tenantRepository;
  private final JwtIssuer jwtIssuer;
  private final AuditWriter auditWriter;

  public AuthService(
      UserRepository userRepository,
      TenantRepository tenantRepository,
      JwtIssuer jwtIssuer,
      AuditWriter auditWriter) {
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
    this.jwtIssuer = jwtIssuer;
    this.auditWriter = auditWriter;
  }

  @Transactional
  public LoginResponse login(LoginRequest body) {
    if (body == null
        || body.getUsername() == null
        || body.getUsername().isBlank()
        || body.getPassword() == null
        || body.getPassword().isBlank()) {
      throw new SecurityException("invalid credentials");
    }
    UUID tenantId = null;
    UUID tenantCode = body.getTenantCode();
    if (tenantCode != null) {
      // TypeSpec 说 tenantCode 是 code 字符串（人类可读），但 NSwag 误生成 UUID。
      // 兼容两种调用：用 code 在 tenants 表里查。
      String code = tenantCode.toString();
      tenantId = tenantRepository.findByCode(code).map(t -> t.getId()).orElse(null);
    }
    UserEntity user = null;
    if (tenantId != null) {
      user = userRepository.findByTenantIdAndUsername(tenantId, body.getUsername()).orElse(null);
    }
    if (user == null) {
      user =
          userRepository
              .findByTenantIdAndUsername(
                  UUID.fromString("00000000-0000-0000-0000-000000000001"), body.getUsername())
              .orElse(null);
    }
    if (user == null) {
      throw new SecurityException("invalid credentials");
    }
    String hash = user.getPasswordHash() == null ? "" : user.getPasswordHash();
    boolean ok = hash.equals("plain:" + body.getPassword()) || hash.equals(body.getPassword());
    if (!ok) {
      throw new SecurityException("invalid credentials");
    }
    if (user.getStatus() == saas.identity.platform.enums.UserStatus.SUSPENDED
        || user.getStatus() == saas.identity.platform.enums.UserStatus.DISABLED) {
      throw new SecurityException("user " + user.getStatus());
    }
    String accessToken = jwtIssuer.issueAccessToken(user.getId(), user.getTenantId());
    String refreshToken = JwtIssuer.generateRefreshToken(user.getId());
    // M03.F01.I01 写端点副作用 — login_success（2026-09-02 contract-test M96 audit 覆盖对齐，
    // 形状对齐 nextjs/msw：actor=target=登录用户，metadata={username}）
    auditWriter.write(
        user.getTenantId(),
        user.getId(),
        "login_success",
        null,
        java.util.Map.of("username", body.getUsername()));
    return new LoginResponse()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(3600)
        .userId(user.getId())
        .currentTenantId(user.getTenantId());
  }

  public void logout() {
    // M03.F03.I05 — 无状态 JWT 仅前端清 cookie；服务端无 session store
  }

  public saas.identity.shared.dto.TokenResponse refresh(
      saas.identity.shared.dto.TokenRequest body) {
    if (body == null) throw new IllegalArgumentException("invalid body");
    String refreshToken = body.getRefreshToken();
    if (refreshToken == null) {
      throw new IllegalArgumentException("missing refresh_token");
    }
    // refreshToken 形如 "refresh-<uuid>-<epoch>"（旧格式）或 JwtIssuer.generateRefreshToken 的
    // "saas-rt-<uuid>-<ts-ms>-<rand>"（新格式）。UUID 自身含 4 个 '-'，rand(base64url)
    // 也可能含 '-' —— 不能按 lastIndexOf('-') 切（会把 rand 中段当 ts、切坏 UUID）。
    // 2026-08-31 contract-test M96.F02.I24 修复：saas-rt- 剥「末两段」(ts + rand)，
    // refresh- 剥「末一段」(epoch)；UUID 部分用首 5 段重组（36 字符标准形）。
    String tokenBody =
        refreshToken.startsWith("saas-rt-")
            ? refreshToken.substring("saas-rt-".length())
            : refreshToken.startsWith("refresh-")
                ? refreshToken.substring("refresh-".length())
                : null;
    if (tokenBody == null) {
      throw new IllegalArgumentException("invalid refresh_token");
    }
    // UUID = 前 5 段（8-4-4-4-12）；后面全是 ts/rand。逐段拼到凑满 5 段为止。
    String[] parts = tokenBody.split("-");
    if (parts.length < 6) { // 5 段 UUID + 至少 1 段尾缀
      throw new IllegalArgumentException("invalid refresh_token");
    }
    String userIdStr = String.join("-", java.util.Arrays.copyOfRange(parts, 0, 5));
    UUID userId;
    try {
      userId = UUID.fromString(userIdStr);
    } catch (Exception e) {
      throw new IllegalArgumentException("invalid refresh_token");
    }
    UserEntity user = userRepository.findById(userId).orElse(null);
    if (user == null) throw new IllegalArgumentException("invalid refresh_token");
    UUID bodyTenantId = body.getTenantId();
    UUID tenantId = bodyTenantId != null ? bodyTenantId : user.getTenantId();
    return new saas.identity.shared.dto.TokenResponse()
        .accessToken(jwtIssuer.issueAccessToken(user.getId(), tenantId))
        .refreshToken(JwtIssuer.generateRefreshToken(user.getId()))
        .tokenType("Bearer")
        .expiresIn(3600)
        .scope("");
  }

  public saas.identity.shared.dto.TokenResponse oidcCallback(
      saas.identity.shared.dto.OidcCallbackRequest body) {
    // M03.F02.I03 — Phase 5 占位：直接返回 mock token
    return new saas.identity.shared.dto.TokenResponse()
        .accessToken("oidc-access-token-" + UUID.randomUUID())
        .tokenType("Bearer")
        .expiresIn(3600)
        .scope("");
  }
}
