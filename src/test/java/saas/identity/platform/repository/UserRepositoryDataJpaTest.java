package saas.identity.platform.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import saas.identity.platform.entity.TenantEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.harness.Fn;

/**
 * M09.F03 — UserRepository @DataJpaTest 切片测试。
 *
 * <p>当前 @Disabled：H2 方言不支持 PG-native uuid[] 数组 / JSONB / CREATE TYPE enum， @DataJpaTest + H2 跑不起
 * JPA Entity 镜像。 实跑验证留 Phase 5 的 @SpringBootTest + Testcontainers PG（共享 plan §D 风险 #2 + #5）。 期间
 * TenantUsersServiceTest（M01.F01.*）+ 手测 Flyway migrate against 真 PG 是 DB 层的实际验证。
 */
@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Disabled("Phase 5: Testcontainers PG; H2 cannot mirror PG uuid[]/JSONB/native enum")
class UserRepositoryDataJpaTest {

  @Autowired private UserRepository userRepository;
  @Autowired private TenantRepository tenantRepository;

  private UUID seedTenant() {
    TenantEntity t = new TenantEntity();
    t.setCode("test-" + UUID.randomUUID().toString().substring(0, 8));
    t.setName("Test Tenant");
    t.setStatus(saas.identity.platform.enums.TenantStatus.ACTIVE);
    return tenantRepository.save(t).getId();
  }

  private UserEntity seedUser(UUID tenantId, String username, String email) {
    UserEntity u = new UserEntity();
    u.setTenantId(tenantId);
    u.setUsername(username);
    u.setEmail(email);
    u.setStatus(saas.identity.platform.enums.UserStatus.ACTIVE);
    u.setRoleIds(List.of());
    return userRepository.save(u);
  }

  @Test
  @Fn({"M09.F03.I01"})
  void saveAndFindByTenantId() {
    UUID tid = seedTenant();
    seedUser(tid, "alice", "[email protected]");
    seedUser(tid, "bob", "[email protected]");

    var page = userRepository.findByTenantId(tid, PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent())
        .extracting(UserEntity::getUsername)
        .containsExactlyInAnyOrder("alice", "bob");
  }

  @Test
  @Fn({"M09.F03.I02"})
  void filterByStatus() {
    UUID tid = seedTenant();
    UserEntity active = seedUser(tid, "alice", "[email protected]");
    UserEntity invited = seedUser(tid, "bob", "[email protected]");
    invited.setStatus(saas.identity.platform.enums.UserStatus.INVITED);
    userRepository.save(invited);

    var page =
        userRepository.findByTenantIdAndStatus(
            tid, saas.identity.platform.enums.UserStatus.ACTIVE, PageRequest.of(0, 10));
    assertThat(page.getContent()).extracting(UserEntity::getId).containsExactly(active.getId());
  }

  @Test
  @Fn({"M09.F03.I03"})
  void uniqueByTenantEmail() {
    UUID tid = seedTenant();
    seedUser(tid, "alice", "[email protected]");

    UserEntity dup = new UserEntity();
    dup.setTenantId(tid);
    dup.setUsername("alice2");
    dup.setEmail("[email protected]"); // same email in same tenant
    dup.setStatus(saas.identity.platform.enums.UserStatus.ACTIVE);

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> userRepository.saveAndFlush(dup))
        .isInstanceOfAny(
            org.springframework.dao.DataIntegrityViolationException.class,
            org.hibernate.exception.ConstraintViolationException.class);
  }
}
