package saas.identity.platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import saas.identity.platform.entity.TenantMembershipEntity;

public interface TenantMembershipRepository extends JpaRepository<TenantMembershipEntity, UUID> {

  List<TenantMembershipEntity> findByUserId(UUID userId);

  Optional<TenantMembershipEntity> findByUserIdAndTenantId(UUID userId, UUID tenantId);

  boolean existsByUserIdAndTenantId(UUID userId, UUID tenantId);

  /**
   * M09.F03.I02 — 取 user 在所有 ACTIVE membership 上的 roleIds 平铺去重。 role_ids 列是 @Transient
   * 数组列（hypersistence StringArrayType 误诊未解），走原生 SQL 直接展开数组，绕开 entity 映射。
   */
  @Query(
      value =
          "SELECT DISTINCT unnest(role_ids) "
              + "FROM tenant_memberships "
              + "WHERE user_id = :userId AND status = 'active'",
      nativeQuery = true)
  List<UUID> findRoleIdsByUserId(@Param("userId") UUID userId);
}
