package saas.identity.platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import saas.identity.platform.entity.Generated.TenantMember;

public interface TenantMemberRepository extends JpaRepository<TenantMember, UUID> {
  List<TenantMember> findByTenantId(UUID tenantId);

  Page<TenantMember> findByTenantId(UUID tenantId, Pageable pageable);

  /**
   * 2026-09-12 四方一致修复（S2）：status 过滤在分页前 DB 级执行（对齐 aspnetcore DB 级过滤 + 过滤后 Count）——内存后滤会让 total
   * 是未过滤总数、命中行散在其他页。
   */
  Page<TenantMember> findByTenantIdAndStatus(UUID tenantId, Short status, Pageable pageable);

  /**
   * 2026-09-12 live 4-way 修复（R1）：无 ORDER BY 的派生查询让 alice 的「首个 active membership」落在
   * globex（…0002），login 签出的 JWT tenant_id 与测试路径 acme（…0001） 不一致 → 全家族 acme 路径 403 `tenant
   * mismatch`。家族种子 acme 最早插入，故 `created_at ASC, id ASC` 与 msw 种子序一致、跨后端稳定。
   */
  @Query("select m from TenantMember m where m.userId = ?1 order by m.createdAt asc, m.id asc")
  List<TenantMember> findByUserId(UUID userId);

  /**
   * ADR-0032 扁平成员视图寻址：成员端点路径参数 {userId} 语义 = sys_user.id（不再是 tenant_member.id）， 同一 user 在同一 tenant
   * 只有一行 member；寻不到由调用方抛 NSEE → 404。
   */
  Optional<TenantMember> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
