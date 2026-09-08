package saas.identity.platform.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.TenantMemberRole;

public interface TenantMemberRoleRepository extends JpaRepository<TenantMemberRole, UUID> {
  List<TenantMemberRole> findByMemberId(UUID memberId);
  void deleteByMemberId(UUID memberId);
}
