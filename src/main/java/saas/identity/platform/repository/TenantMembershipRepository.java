package saas.identity.platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.TenantMembershipEntity;

public interface TenantMembershipRepository extends JpaRepository<TenantMembershipEntity, UUID> {

  List<TenantMembershipEntity> findByUserId(UUID userId);

  Optional<TenantMembershipEntity> findByUserIdAndTenantId(UUID userId, UUID tenantId);

  boolean existsByUserIdAndTenantId(UUID userId, UUID tenantId);
}
