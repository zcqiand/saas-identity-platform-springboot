package saas.identity.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import saas.identity.platform.entity.RoleMenuGrantEntity;

public interface RoleMenuGrantRepository extends JpaRepository<RoleMenuGrantEntity, UUID> {

  Optional<RoleMenuGrantEntity> findByRoleId(UUID roleId);

  /**
   * M09.F03.I02 — 给定一组 roleId，平铺去重取出全部授权 menu_id。 menu_ids 列是 @Transient 数组列 （hypersistence
   * StringArrayType 误诊未解），走原生 SQL 直接展开数组，绕开 entity 映射。
   *
   * <p>2026-09-04 真库测试抓出：Hibernate 把 Collection&lt;UUID&gt; 展开成多占位符（ANY(?,?)）PG 语法错 ——
   * 多角色必炸（单角色恰好合法，冒烟测不出）。RepositoryPgTest 锁回归。 修法改行内 values join（每 roleId 一行 unnest），无数组绑定。
   */
  @Query(
      value =
          "SELECT DISTINCT unnest(menu_ids) "
              + "FROM role_menu_grants "
              + "WHERE role_id IN (:roleIds)",
      nativeQuery = true)
  List<UUID> findMenuIdsByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
