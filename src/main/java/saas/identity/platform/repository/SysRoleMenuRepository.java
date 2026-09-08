package saas.identity.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import saas.identity.platform.entity.Generated.SysRoleMenu;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, UUID> {
  List<SysRoleMenu> findByRoleId(UUID roleId);

  void deleteByRoleId(UUID roleId);

  /** M04.F04.I08 — 一次查一批 role 的所有 menu grants（M04.F04 渲染 me/menus 用） */
  @Query("SELECT r FROM SysRoleMenu r WHERE r.roleId IN :roleIds")
  List<SysRoleMenu> findByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
