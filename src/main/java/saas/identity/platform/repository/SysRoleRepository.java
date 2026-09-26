package saas.identity.platform.repository;

// @impl M00.F03.I01 — book anchor (xr-know-007)

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import saas.identity.platform.entity.Generated.SysRole;

public interface SysRoleRepository extends JpaRepository<SysRole, UUID> {

  /**
   * 租户维度过滤（schema 有 idx_sys_role_tenant_client）。此前列表走 findAll() 完全忽略 path tenantId →
   * 返回了别的租户的角色（2026-09-12 修复）。
   *
   * <p>2026-09-12 四方 live 修复（roles list 排序）：msw oracle 返回插入序（created_at ASC），无 ORDER BY
   * 的派生查询按堆表物理序近似成立但契约不保证；显式 `created_at ASC, id ASC` 对齐（同 {@link
   * TenantMemberRepository#findByUserId} R1 先例）。
   */
  @Query("select r from SysRole r where r.tenantId = ?1 order by r.createdAt asc, r.id asc")
  Page<SysRole> findByTenantId(UUID tenantId, Pageable pageable);

  /** ADR-0032 成员视图 roleIds join 用：非分页全量（tenant 维度过滤）。 */
  List<SysRole> findByTenantId(UUID tenantId);
}
