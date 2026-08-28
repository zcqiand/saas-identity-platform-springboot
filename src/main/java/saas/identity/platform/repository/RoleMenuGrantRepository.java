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
   */
  @Query(
      value =
          "SELECT DISTINCT unnest(menu_ids) "
              + "FROM role_menu_grants "
              + "WHERE role_id = ANY(:roleIds)",
      nativeQuery = true)
  List<UUID> findMenuIdsByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
