package saas.identity.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import saas.identity.platform.entity.Generated.OauthAccessToken;
import saas.identity.platform.entity.Generated.OauthClient;
import saas.identity.platform.entity.Generated.OauthCode;
import saas.identity.platform.entity.Generated.OauthRefreshToken;
import saas.identity.platform.entity.Generated.SysUser;
import saas.identity.platform.entity.Generated.Tenant;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.OauthAccessTokenRepository;
import saas.identity.platform.repository.OauthClientRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.repository.OauthRefreshTokenRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/**
 * 5.123①② 竞态机制钉（tracker：rails 先行定形状，springboot 同形镜像）。
 *
 * <p>② rotate 必须原子：撤销旧行与签发新对两笔写同事务——签发段失败回滚恢复旧行， 同一 refresh 可重试（修前 revoke 先行落库，签发段故障留「旧废新未落」半态）。 ①
 * code claim 影响行数判定：并发输家删 0 行必须 INVALID_GRANT 拒签；H2 切片下毫秒级 交错不可达，此处钉顺序重放语义（并发面由 deleteByCode 谓词
 * claim 承接）。
 *
 * <p>@DataJpaTest + NOT_SUPPORTED：控制器写走真事务（外层测试事务会吞内层回滚语义）。
 *
 * <p>@Tag("pg")：真 PG 切片（DATABASE_URL fail-fast），按 ci.yml 分层「CI=编译+mock 单测 / gate=真库全量」 在 CI 排除——本仓
 * excludedGroups=pg 通道既有（lab-aspnetcore v0.2.27 同款）；本地 gate L4 全量照跑。
 */
@Tag("pg")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OauthTokenRaceTest {

  private static final String CLIENT_ID = "saas-console";

  /**
   * .env.test 的 DATABASE_URL 是 libpq 形（postgresql://user:pass@host:port/db，gate 注入）， JDBC 驱动只认
   * jdbc: 形——此处归一并接 DATABASE_USER/PASSWORD；缺 env 直接炸（ADR-0019， 禁兜底）。凭据不带进 URL（凭据形 URL 会把 user:pass@
   * 当 host → UnknownHostException）。
   */
  @DynamicPropertySource
  static void datasourceProps(DynamicPropertyRegistry registry) {
    String url = System.getenv("DATABASE_URL");
    if (url == null || url.isBlank()) {
      throw new IllegalStateException("DATABASE_URL 未设置——本切片测试要求真 PG（.env.test 提供）");
    }
    registry.add("spring.datasource.url", () -> toJdbcUrl(url));
    registry.add("spring.datasource.username", () -> System.getenv("DATABASE_USER"));
    registry.add("spring.datasource.password", () -> System.getenv("DATABASE_PASSWORD"));
  }

  static String toJdbcUrl(String url) {
    // postgresql://user:pass@host:port/db → jdbc:postgresql://host:port/db（只剥 userinfo）
    return url.startsWith("jdbc:") ? url : "jdbc:" + url.replaceFirst("//[^@/]*@", "//");
  }

  // 手动 setId 会被 Hibernate 当 detached 走 merge → StaleObjectState（springboot 写路径在册坑），
  // user/tenant id 由 @GeneratedValue 生成、seed() 后引用 getId()
  private UUID userId;
  private UUID tenantId;

  @Autowired OauthClientRepository clients;
  @Autowired OauthCodeRepository codes;
  @Autowired OauthAccessTokenRepository accessTokens;
  @Autowired OauthRefreshTokenRepository refreshTokens;
  @Autowired PlatformTransactionManager txm;

  private OauthController ctrl(TokenIssuer issuer) {
    JwtIssuer jwt = mock(JwtIssuer.class);
    when(jwt.issueAccessToken(any(), any())).thenReturn("fake.jwt.token");
    when(jwt.getTtlSeconds()).thenReturn(3600L);
    return new OauthController(
        clients,
        codes,
        accessTokens,
        refreshTokens,
        jwt,
        issuer,
        mock(JwtDecoder.class),
        new TransactionTemplate(txm));
  }

  @Autowired saas.identity.platform.repository.SysUserRepository users;
  @Autowired saas.identity.platform.repository.TenantRepository tenants;

  @BeforeEach
  void seed() {
    SysUser u = new SysUser();
    u.setUsername("race-alice");
    u.setPassword("n/a");
    u.setStatus((short) 1);
    u.setFailedAttempts(0);
    u.setCreatedAt(OffsetDateTime.now());
    u.setUpdatedAt(OffsetDateTime.now());
    userId = users.save(u).getId();

    Tenant t = new Tenant();
    t.setTenantKey("race-tenant");
    t.setName("race-tenant");
    t.setStatus((short) 1);
    t.setCreatedAt(OffsetDateTime.now());
    t.setUpdatedAt(OffsetDateTime.now());
    tenantId = tenants.save(t).getId();

    // 共享 saas_test 里其他仓的种子常已写 saas-console（uk_oauth_client_id 唯一键）——先清自己的键再种
    clients.findByClientId(CLIENT_ID).ifPresent(clients::delete);

    OauthClient c = new OauthClient();
    c.setClientId(CLIENT_ID);
    c.setClientSecret("n/a");
    c.setClientName("race-test");
    c.setGrantTypes("authorization_code,refresh_token");
    c.setRedirectUris("http://localhost/callback");
    c.setScopes("read,write");
    c.setAutoApprove(Boolean.FALSE);
    c.setAccessTokenValidity(3600);
    c.setRefreshTokenValidity(604800);
    c.setStatus((short) 1);
    c.setCreatedAt(OffsetDateTime.now());
    c.setUpdatedAt(OffsetDateTime.now());
    clients.save(c);
  }

  @AfterEach
  void clean() {
    refreshTokens.deleteAll();
    accessTokens.deleteAll();
    codes.deleteAll();
    clients.deleteAll();
    users.deleteAll();
    tenants.deleteAll();
  }

  private void seedRefreshPair(String refreshToken) {
    OauthAccessToken at = new OauthAccessToken();
    at.setTokenId("at_" + UUID.randomUUID());
    at.setAccessToken("n/a");
    at.setUserId(userId);
    at.setTenantId(tenantId);
    at.setClientId(CLIENT_ID);
    at.setTokenType("Bearer");
    at.setScope("read");
    at.setExpiresAt(OffsetDateTime.now().plusHours(1));
    at.setRevoked(false);
    at.setCreatedAt(OffsetDateTime.now());
    accessTokens.save(at);

    OauthRefreshToken rt = new OauthRefreshToken();
    rt.setRefreshToken(refreshToken);
    rt.setAccessTokenId(at.getId());
    rt.setUserId(userId);
    rt.setTenantId(tenantId);
    rt.setClientId(CLIENT_ID);
    rt.setExpiresAt(OffsetDateTime.now().plusDays(30));
    rt.setRevoked(false);
    rt.setCreatedAt(OffsetDateTime.now());
    refreshTokens.save(rt);
  }

  private static TokenRequest refreshReq(String rt) {
    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.REFRESH_TOKEN);
    req.setClientId(CLIENT_ID);
    req.setRefreshToken(rt);
    return req;
  }

  @Test
  @Fn({"M04.F03.I03"})
  void rotateIsAtomic_failedIssuanceRestoresOldToken() {
    seedRefreshPair("rt-old");
    TokenRequest req = refreshReq("rt-old");

    // 注错：签发段故障（真仓库 + 真事务，仅 TokenIssuer 子类抛出）
    TokenIssuer failing =
        new TokenIssuer(clients, accessTokens, refreshTokens) {
          @Override
          public String persistTokenPair(
              UUID userId, UUID tenantId, String clientId, String scope) {
            throw new IllegalStateException("db fault mid-rotation");
          }
        };
    assertThrows(IllegalStateException.class, () -> ctrl(failing).oAuthToken(req));

    // 轮换段事务回滚：旧行 revoked 已还原 → 同一 refresh 可再次轮换
    ResponseEntity<TokenResponse> ok =
        ctrl(new TokenIssuer(clients, accessTokens, refreshTokens)).oAuthToken(req);
    assertEquals(HttpStatus.OK, ok.getStatusCode());
  }

  @Test
  @Fn({"M04.F03.I02"})
  void exchange_replayAfterClaimIsRejected() {
    OauthCode row = new OauthCode();
    row.setCode("ac-once");
    row.setClientId(CLIENT_ID);
    row.setUserId(userId);
    row.setTenantId(tenantId);
    row.setRedirectUri("http://localhost/callback");
    row.setScope("read");
    row.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
    row.setCreatedAt(OffsetDateTime.now());
    codes.save(row);

    TokenRequest req = new TokenRequest();
    req.setGrantType(TokenRequest.GrantTypeEnum.AUTHORIZATION_CODE);
    req.setClientId(CLIENT_ID);
    req.setCode("ac-once");
    req.setRedirectUri("http://localhost/callback");

    assertEquals(
        HttpStatus.OK,
        ctrl(new TokenIssuer(clients, accessTokens, refreshTokens))
            .oAuthToken(req)
            .getStatusCode());
    // 重放 = claim 已被赢家消费（deleted=0 形态的顺序投影）→ INVALID_GRANT 拒签
    assertThrows(
        IllegalArgumentException.class,
        () -> ctrl(new TokenIssuer(clients, accessTokens, refreshTokens)).oAuthToken(req));
  }
}
