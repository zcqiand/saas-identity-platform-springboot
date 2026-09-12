package saas.identity.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import saas.identity.platform.entity.Generated.SysRoleMenu;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, UUID> {
  List<SysRoleMenu> findByRoleId(UUID roleId);

  /**
   * I20/I38 整批写路径专用：bulk JPQL delete 单语句执行。
   *
   * <p>2026-09-12 修四方并发超时/死锁（40P01 + StaleObjectState）：旧派生 deleteByRoleId 会先 SELECT 加载全部实体再逐行
   * delete —— 34 grants × ~195ms PG RTT = 13s+，且与并发 PUT 的 全量 insert 撞行锁死锁。对齐家族 oracle（nextjs
   * notInArray delete + onConflictDoNothing） 后只剩单语句差量 delete。
   */
  @Modifying
  @Query("DELETE FROM SysRoleMenu r WHERE r.roleId = :roleId")
  int deleteAllForRole(@Param("roleId") UUID roleId);

  /** 差量语义：只删不在新集合里的 grant（保留未变化的行，缩小锁窗口）。 */
  @Modifying
  @Query("DELETE FROM SysRoleMenu r WHERE r.roleId = :roleId AND r.menuId NOT IN :menuIds")
  int deleteByRoleIdAndMenuIdNotIn(
      @Param("roleId") UUID roleId, @Param("menuIds") Collection<UUID> menuIds);

  /** M04.F04.I08 — 一次查一批 role 的所有 menu grants（M04.F04 渲染 me/menus 用） */
  @Query("SELECT r FROM SysRoleMenu r WHERE r.roleId IN :roleIds")
  List<SysRoleMenu> findByRoleIds(@Param("roleIds") Collection<UUID> roleIds);
}
