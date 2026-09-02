package saas.identity.platform.config;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Dev-only 数据修复：saas_dev 的 users.role_ids / tenant_memberships.role_ids / role_menu_grants.menu_ids
 * 等 uuid[] 列由 next.js sync-db 灌的可能是空数组； hypersistence-utils 3.9.0 的 UUIDArrayType 处理空 uuid[] 会 NPE
 * （MutableType.nullSafeGet:99 → ArrayUtil:221）。
 *
 * <p>启动时把所有空 uuid[] 列替换为 [DUMMY_UUID]，绕过 NPE。authoritative 角色关系在
 * tenant_memberships.role_ids，users.role_ids 是冗余镜像；这个 dummy 不影响业务逻辑。
 *
 * <p>仅 dev profile 启用（prod 用 Flyway 管理迁移不需要这个）。
 */
@Component
@Profile("dev")
public class DevDataFixer implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DevDataFixer.class);

  /** 全零 UUID 仅作占位，跟真业务 UUID 不冲突。 */
  private static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final JdbcTemplate jdbc;

  public DevDataFixer(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(String... args) {
    int users = fixArray("users", "role_ids");
    int memberships = fixArray("tenant_memberships", "role_ids");
    int menus = fixArray("role_menu_grants", "menu_ids");
    // apps.text[] 列（redirect_uris / scopes / grant_types）也走 hypersistence-utils StringArrayType，
    // 同样的 unwrapArray NPE。直接灌 dummy 占位。dev 不影响 OAuth flow 真实数据。
    int redirectUris = fixTextArray("apps", "redirect_uris", "http://localhost:5101/callback");
    int scopes = fixTextArray("apps", "scopes", "openid");
    int grantTypes = fixGrantTypes("apps");
    log.info(
        "[dev-data-fixer] users.role_ids={} tenant_memberships.role_ids={} role_menu_grants.menu_ids={}"
            + " apps.redirect_uris={} apps.scopes={} apps.grant_types={}",
        users,
        memberships,
        menus,
        redirectUris,
        scopes,
        grantTypes);
  }

  private int fixArray(String table, String column) {
    // 无条件 UPDATE：next.js sync-db 灌的 uuid[] 可能是空数组、NULL 或 PG 序列化成别的奇怪表示，
    // 直接全部覆盖成 [DUMMY] 一了百了。authoritative 在 tenant_memberships.role_ids，
    // 本列是冗余镜像，dummy 不影响业务。
    return jdbc.update("UPDATE " + table + " SET " + column + " = ARRAY[?]::uuid[]", DUMMY);
  }

  private int fixTextArray(String table, String column, String dummy) {
    return jdbc.update("UPDATE " + table + " SET " + column + " = ARRAY[?]::text[]", dummy);
  }

  // grant_types 列是 oauth_grant_type[]（PG 自定义 enum 数组），要 cast 到具体 enum 类型
  private int fixGrantTypes(String table) {
    return jdbc.update(
        "UPDATE " + table + " SET grant_types = ARRAY['authorization_code']::oauth_grant_type[]");
  }
}
