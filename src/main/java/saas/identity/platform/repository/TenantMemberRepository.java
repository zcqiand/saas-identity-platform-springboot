package saas.identity.platform.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.TenantMember;

public interface TenantMemberRepository extends JpaRepository<TenantMember, UUID> {
  List<TenantMember> findByTenantId(UUID tenantId);
  Page<TenantMember> findByTenantId(UUID tenantId, Pageable pageable);
  List<TenantMember> findByUserId(UUID userId);
}
