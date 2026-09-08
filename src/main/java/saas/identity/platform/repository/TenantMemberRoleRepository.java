package saas.identity.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import saas.identity.platform.entity.Generated.TenantMemberRole;

public interface TenantMemberRoleRepository extends JpaRepository<TenantMemberRole, UUID> {
  List<TenantMemberRole> findByMemberId(UUID memberId);

  void deleteByMemberId(UUID memberId);

  /** M04.F04.I08 — 一次查一批 member 的所有 role bindings */
  @Query("SELECT r FROM TenantMemberRole r WHERE r.memberId IN :memberIds")
  List<TenantMemberRole> findByMemberIds(@Param("memberIds") Collection<UUID> memberIds);
}
