package saas.identity.platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.TenantApplication;

public interface TenantApplicationRepository extends JpaRepository<TenantApplication, UUID> {
  List<TenantApplication> findByTenantId(UUID tenantId);

  Page<TenantApplication> findByTenantId(UUID tenantId, Pageable pageable);

  Optional<TenantApplication> findByTenantIdAndClientId(UUID tenantId, String clientId);
}
