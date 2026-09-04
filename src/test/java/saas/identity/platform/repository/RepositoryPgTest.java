package saas.identity.platform.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.RoleMenuGrantEntity;
import saas.identity.platform.entity.TenantEntity;
import saas.identity.platform.entity.TenantMembershipEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.enums.TenantStatus;
import saas.identity.platform.enums.UserStatus;
import saas.identity.platform.harness.Fn;

/**
 * Repository 真 PG 切片测试（saas_test）。硬依赖共享 PG —— 连不上即失败，不 skip。
 *
 * <p>前身 UserRepositoryDataJpaTest @Disabled 三个月（"H2 cannot mirror PG uuid[]/JSONB/native enum;
 * Phase 5 Testcontainers"）—— 期间 2 个 native SQL + unnest() 查询零验证。 Testcontainers 需要 docker
 * daemon；这里直接用家族共享的 saas_test 库（结构 = shared SQL SSOT），H2 镜像不了的三件事（uuid[] / jsonb / native
 * enum）全走真方言。
 *
 * <p>重点回归面：findRoleIdsByUserId / findMenuIdsByRoleIds 的 unnest + ANY(:uuid[])
 * （jdbctemplate-unnest-list-uuid 教训的 repository 版 —— mock 全绿 prod 500 的同款风险）。
 *
 * <p>CI 分层：@Tag("pg")，ci.yml -DexcludedGroups=pg 排除（runner 够不到内网 PG）； suite gate L4 全量跑（本机可达）。
 */
@Tag("pg")
@DataJpaTest
// JPA 切片最小配置（不复用全量 Application —— 主类在 harness 包，且全量会拉起
// ADR-0019 fail-fast bean，测试 JVM 无 env 即崩）
@Import(RepositoryPgTest.JpaSliceConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(
    properties = {
      // env 可覆盖；连不上即测试失败 —— 没有兜底，没有 skip。
      // stringtype=unspecified 与 prod 同款（application.yml）：AttributeConverter 输出
      // 的 enum 字符串以 unknown 绑定，PG 按目标列 native enum 解析。
      "spring.datasource.url=${SAAS_TEST_DATABASE_URL:jdbc:postgresql://100.79.128.25:5432/saas_test}",
      "spring.datasource.username=${SAAS_TEST_DATABASE_USER:postgres}",
      "spring.datasource.password=${SAAS_TEST_DATABASE_PASSWORD:qiand68+++}",
      "spring.datasource.hikari.data-source-properties.stringtype=unspecified",
      // saas_test 结构 = shared SQL SSOT 已建；flyway 关闭（不写 baseline 行污染）。
      // ddl-auto=none 对齐 prod（application.yml）：hypersistence ListArrayType 的
      // columnDefinition "text[]" 与 PG udt _text 在 validate 下必炸（prod 即为此用 none）。
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=none",
    })
@Transactional
class RepositoryPgTest {

  @org.springframework.context.annotation.Configuration
  @EntityScan(basePackages = "saas.identity.platform.entity")
  @EnableJpaRepositories(basePackages = "saas.identity.platform.repository")
  @Import({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
  static class JpaSliceConfig {}

  @Autowired private UserRepository userRepository;
  @Autowired private TenantRepository tenantRepository;
  @Autowired private TenantMembershipRepository membershipRepository;
  @Autowired private RoleMenuGrantRepository grantRepository;
  @Autowired private RoleRepository roleRepository;

  private UUID seedTenant() {
    TenantEntity t = new TenantEntity();
    t.setCode("pg-t-" + UUID.randomUUID().toString().substring(0, 8));
    t.setName("PG Test Tenant");
    t.setStatus(TenantStatus.ACTIVE);
    return tenantRepository.save(t).getId();
  }

  private UserEntity seedUser(UUID tenantId, String username, String email) {
    UserEntity u = new UserEntity();
    u.setTenantId(tenantId);
    u.setUsername("pg-" + username + "-" + UUID.randomUUID().toString().substring(0, 8));
    u.setEmail(email);
    u.setStatus(UserStatus.ACTIVE);
    u.setRoleIds(List.of());
    return userRepository.save(u);
  }

  // === 用户分页（原 @Disabled 用例的真库版）。旧测试挂 M09.F03.I01 —— 该 ID 不在
  // function-tree（I02 起），@Disabled 不产 trace 所以 L5 一直没报；这里挂 I02 对齐。 ===

  @Test
  @Fn({"M09.F03.I02"})
  void saveAndFindByTenantId() {
    UUID tid = seedTenant();
    seedUser(tid, "alice", UUID.randomUUID() + "@pg-test.local");
    seedUser(tid, "bob", UUID.randomUUID() + "@pg-test.local");

    var page = userRepository.findByTenantId(tid, PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(2);
  }

  @Test
  @Fn({"M09.F03.I02"})
  void filterByStatus() {
    UUID tid = seedTenant();
    UserEntity active = seedUser(tid, "alice", UUID.randomUUID() + "@pg-test.local");
    UserEntity invited = seedUser(tid, "bob", UUID.randomUUID() + "@pg-test.local");
    invited.setStatus(UserStatus.INVITED);
    userRepository.save(invited);

    var page =
        userRepository.findByTenantIdAndStatus(tid, UserStatus.ACTIVE, PageRequest.of(0, 10));
    assertThat(page.getContent()).extracting(UserEntity::getId).containsExactly(active.getId());
  }

  @Test
  @Fn({"M09.F03.I03"})
  void uniqueByTenantEmail() {
    UUID tid = seedTenant();
    String email = UUID.randomUUID() + "@pg-test.local";
    seedUser(tid, "alice", email);

    UserEntity dup = new UserEntity();
    dup.setTenantId(tid);
    dup.setUsername("pg-dup-" + UUID.randomUUID().toString().substring(0, 8));
    dup.setEmail(email); // 同租户同 email
    dup.setStatus(UserStatus.ACTIVE);
    dup.setRoleIds(List.of());

    assertThatThrownBy(() -> userRepository.saveAndFlush(dup))
        .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
  }

  // === native SQL + unnest 回归面（unnest-list-uuid 教训的 repository 版） ===

  @Test
  @Fn({"M09.F03.I02"})
  void findRoleIdsByUserId_unnestsUuidArrayOnRealPg() {
    UUID tid = seedTenant();
    UserEntity u = seedUser(tid, "carol", UUID.randomUUID() + "@pg-test.local");
    var roleA = UUID.randomUUID();
    var roleB = UUID.randomUUID();

    TenantMembershipEntity m = new TenantMembershipEntity();
    m.setUserId(u.getId());
    m.setTenantId(tid);
    m.setRoleIds(List.of(roleA, roleB));
    m.setStatus(saas.identity.platform.enums.MembershipStatus.ACTIVE);
    membershipRepository.save(m);

    var out = membershipRepository.findRoleIdsByUserId(u.getId());

    assertThat(out).containsExactlyInAnyOrder(roleA, roleB); // unnest 平铺
  }

  @Test
  @Fn({"M09.F03.I02"})
  void findMenuIdsByRoleIds_anyAndUnnestOnRealPg() {
    var menu1 = UUID.randomUUID();
    var menu2 = UUID.randomUUID();
    UUID tid = seedTenant();
    // rmg_role_fk：grants 须有真 roles 父行。RoleEntity @GeneratedValue(UUID)：
    // 手动 setId 再 save 会被当 detached 走 merge → StaleObjectState（同
    // springboot-write-path-double-bug 教训），由 DB 生成后取回。
    var roleA = seedRole(tid).getId();
    var roleB = seedRole(tid).getId();

    grantRepository.save(grant(roleA, tid, List.of(menu1, menu2)));
    grantRepository.save(grant(roleB, tid, List.of(menu1))); // 去重面

    var out = grantRepository.findMenuIdsByRoleIds(List.of(roleA, roleB));

    assertThat(out).containsExactlyInAnyOrder(menu1, menu2); // ANY + unnest + DISTINCT
  }

  private saas.identity.platform.entity.RoleEntity seedRole(UUID tenantId) {
    saas.identity.platform.entity.RoleEntity r = new saas.identity.platform.entity.RoleEntity();
    r.setTenantId(tenantId);
    r.setCode("pg-r-" + UUID.randomUUID().toString().substring(0, 8));
    r.setName("PG Test Role");
    return roleRepository.save(r);
  }

  private RoleMenuGrantEntity grant(UUID roleId, UUID tenantId, List<UUID> menuIds) {
    RoleMenuGrantEntity g = new RoleMenuGrantEntity();
    g.setRoleId(roleId);
    g.setTenantId(tenantId);
    g.setMenuIds(menuIds);
    return g;
  }
}
