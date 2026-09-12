package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.OauthCode;
import saas.identity.platform.entity.Generated.OauthRefreshToken;
import saas.identity.platform.entity.Generated.SysUser;
import saas.identity.platform.entity.Generated.Tenant;
import saas.identity.platform.entity.Generated.TenantMember;
import saas.identity.platform.repository.OauthAccessTokenRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.api.AuthApi;
import saas.identity.shared.dto.LockedAccountResponse;
import saas.identity.shared.dto.LoginRequest;
import saas.identity.shared.dto.LoginResponse;
import saas.identity.shared.dto.OidcCallbackRequest;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * M01.F04 SSO 登录 + 失败锁定 + OIDC + 登出（ADR-0020 路线 A）。
 *
 * <p>失败锁定策略（M01.F04.I02）：连续 5 次密码错误 → 锁定 15 分钟； 阈值与窗口在 application.yml 由消费后端配置，可在 DevDataFixer
 * 临时放低。
 */
@RestController
public class AuthController implements AuthApi {

  static final int LOCKOUT_THRESHOLD = 5;
  static final int LOCKOUT_MINUTES = 15;

  private final SysUserRepository users;
  private final OauthCodeRepository codes;
  private final OauthAccessTokenRepository accessTokens;
  private final OauthRefreshTokenRepository refreshTokens;
  private final TenantMemberRepository members;
  private final TenantRepository tenants;
  private final saas.identity.platform.repository.TenantApplicationRepository tenantApplications;
  private final MemberViewAssembler assembler;
  private final JwtIssuer jwt;
  private final TokenIssuer tokenIssuer;
  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

  public AuthController(
      SysUserRepository users,
      OauthCodeRepository codes,
      OauthAccessTokenRepository accessTokens,
      OauthRefreshTokenRepository refreshTokens,
      TenantMemberRepository members,
      TenantRepository tenants,
      saas.identity.platform.repository.TenantApplicationRepository tenantApplications,
      MemberViewAssembler assembler,
      JwtIssuer jwt,
      TokenIssuer tokenIssuer) {
    this.users = users;
    this.codes = codes;
    this.accessTokens = accessTokens;
    this.refreshTokens = refreshTokens;
    this.members = members;
    this.tenants = tenants;
    this.tenantApplications = tenantApplications;
    this.assembler = assembler;
    this.jwt = jwt;
    this.tokenIssuer = tokenIssuer;
  }

  @Override
  public ResponseEntity<LoginResponse> sessionsLogin(LoginRequest body) {
    // 2026-09-12 live 4-way 修复（R3 附带）：未知用户与错密码同语义 401 INVALID_CREDENTIALS
    // （msw oracle 口径，也不泄露用户存在性），此前 NSEE → 404 分叉。
    SysUser user =
        users
            .findByUsername(body.getUsername())
            .orElseThrow(saas.identity.platform.security.InvalidCredentialsException::new);

    // M01.F04.I02 — 锁定窗口检查
    if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
      LockedAccountResponse locked = new LockedAccountResponse();
      locked.setCode("ACCOUNT_LOCKED");
      locked.setMessage("连续失败次数过多，请稍后再试");
      locked.setLockedUntil(user.getLockedUntil());
      return ResponseEntity.status(HttpStatus.LOCKED).body(null);
    }

    // 家族 dev 种子约定（nextjs seed-db.mjs）：password 列可存 "plain:{password}"
    // 占位（Phase 5；prod 换 argon2/bcrypt）。aspnetcore/nextjs 两侧已识别该前缀，
    // springboot 对齐，否则同一份种子三后端登录行为分叉（contract-test live 401）。
    boolean plainOk =
        user.getPassword() != null && user.getPassword().equals("plain:" + body.getPassword());
    if (!plainOk && !bcrypt.matches(body.getPassword(), user.getPassword())) {
      int attempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1;
      user.setFailedAttempts(attempts);
      if (attempts >= LOCKOUT_THRESHOLD) {
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(LOCKOUT_MINUTES));
      }
      users.save(user);
      throw new saas.identity.platform.security.InvalidCredentialsException();
    }

    // 成功：重置失败计数
    user.setFailedAttempts(0);
    user.setLockedUntil(null);
    users.save(user);

    // 家族语义（nextjs login route 为准）：tenantId = 用户首个 active membership 的
    // tenant（status=1），且 tenant 本身 active。sys_user 无 tenantId 列（多租户走
    // tenant_member）。此前这里是 UUID.randomUUID() —— 随机值写 oauth_access_token
    // 违反 tenant_id FK（23503），login 500 级联全后端比对失活。
    List<TenantMember> activeMemberships =
        members.findByUserId(user.getId()).stream()
            .filter(m -> m.getStatus() != null && m.getStatus() == 1)
            .toList();
    UUID tenantId =
        activeMemberships.stream().map(TenantMember::getTenantId).findFirst().orElse(null);
    if (tenantId == null) {
      throw new org.springframework.security.access.AccessDeniedException(
          "user has no active tenant membership");
    }
    Tenant tenant = tenants.findById(tenantId).orElse(null);
    if (tenant == null || tenant.getStatus() == null || tenant.getStatus() != 1) {
      throw new org.springframework.security.access.AccessDeniedException(
          "tenant unavailable: " + tenantId);
    }
    String accessToken = jwt.issueAccessToken(user.getId(), tenantId);
    String refreshToken =
        tokenIssuer.persistTokenPair(user.getId(), tenantId, body.getClientId(), null);

    saas.identity.shared.dto.SysUser dto = new saas.identity.shared.dto.SysUser();
    dto.setId(user.getId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());

    // ADR-0032（2026-09-12）：availableTenants 不再写死 List.of()。
    // 口径：tenant_application(client_id 匹配请求体) ⨝ tenant_member(user 匹配, status=1)
    // → TenantMembership（roleIds 真 join、joinedAt = tenant_member.created_at）。
    // S3 修复（2026-09-12 四方一致）：clientId 无订阅 → 退化为不过滤（返全部 active
    // membership），对齐 aspnetcore AuthApi / msw oracle——空集 filter 会吞掉全部 membership。
    Set<UUID> subscribedTenants =
        tenantApplications.findByClientId(body.getClientId()).stream()
            .map(saas.identity.platform.entity.Generated.TenantApplication::getTenantId)
            .collect(Collectors.toSet());
    LoginResponse resp = new LoginResponse();
    resp.setUser(dto);
    resp.setAvailableTenants(
        activeMemberships.stream()
            .filter(m -> subscribedTenants.isEmpty() || subscribedTenants.contains(m.getTenantId()))
            .map(assembler::toMembership)
            .toList());
    resp.setUserId(user.getId());
    resp.setCurrentTenantId(tenantId);
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setClientId(body.getClientId());
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<Void> sessionsLogout() {
    return ResponseEntity.noContent().build();
  }

  /** OIDC authorization_code → token exchange（ADR-0020 路线 A）。 一次性消费：code 表行用完即删，防 replay。 */
  @Override
  public ResponseEntity<TokenResponse> sessionsOidcCallback(OidcCallbackRequest body) {
    OauthCode code =
        codes
            .findByCode(body.getCode())
            .orElseThrow(() -> new IllegalArgumentException("invalid_grant: unknown code"));
    if (code.getExpiresAt() != null && code.getExpiresAt().isBefore(OffsetDateTime.now())) {
      codes.delete(code);
      throw new IllegalArgumentException("invalid_grant: expired code");
    }

    String accessToken = jwt.issueAccessToken(code.getUserId(), code.getTenantId());
    String refreshToken =
        tokenIssuer.persistTokenPair(
            code.getUserId(), code.getTenantId(), body.getClientId(), code.getScope());

    // 一次性消费：删 code 行
    codes.delete(code);

    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setUserId(code.getUserId().toString());
    resp.setClientId(body.getClientId());
    resp.setTenantId(code.getTenantId());
    return ResponseEntity.ok(resp);
  }

  /** refresh_token → new access_token。rotate：旧 rt 标记 revoked（默认 30 天）。 */
  @Override
  public ResponseEntity<TokenResponse> sessionsRefreshToken(TokenRequest body) {
    // I24「未知 refreshToken → 400」：findByRefreshToken 查不到必须走 400 分支，
    // 对齐 msw oracle（INVALID_GRANT），不许静默重发。
    OauthRefreshToken rt =
        refreshTokens
            .findByRefreshToken(body.getRefreshToken())
            .orElseThrow(
                () -> new IllegalArgumentException("invalid_grant: unknown refresh_token"));
    if (Boolean.TRUE.equals(rt.getRevoked())) {
      throw new IllegalArgumentException("invalid_grant: revoked refresh_token");
    }

    String accessToken = jwt.issueAccessToken(rt.getUserId(), rt.getTenantId());
    // rotate：旧 rt 标 revoked
    rt.setRevoked(true);
    refreshTokens.save(rt);
    // 2026-09-12 修复：新 refresh 行的 client_id 以旧 rt 行上绑定的值为准（登录时已过
    // FK 校验的合法 FK 值），不信任请求体——请求体给 UUID 形 clientId 时 FK 23503 → 500。
    String clientId = rt.getClientId() != null ? rt.getClientId() : body.getClientId();
    String scope =
        accessTokens
            .findById(rt.getAccessTokenId())
            .map(saas.identity.platform.entity.Generated.OauthAccessToken::getScope)
            .orElse(null);
    String newRefresh =
        tokenIssuer.persistTokenPair(rt.getUserId(), rt.getTenantId(), clientId, scope);

    // 2026-09-12 live 4-way 修复：响应 shape 对齐 msw oracle（token 四件套 + scope）。
    // userId/tenantId/clientId 不回显（normalize 剔 ID 后 clientId 仍会与 oracle 分叉）。
    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(newRefresh);
    resp.setTokenType("Bearer");
    resp.setExpiresIn((int) Math.min(Integer.MAX_VALUE, jwt.getTtlSeconds()));
    resp.setScope(scope);
    return ResponseEntity.ok(resp);
  }
}
