package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.ApiKeyEntity;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {

  Page<ApiKeyEntity> findByTenantId(UUID tenantId, Pageable pageable);

  Optional<ApiKeyEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<ApiKeyEntity> findByTenantIdAndPrefix(UUID tenantId, String prefix);

  boolean existsByTenantIdAndPrefix(UUID tenantId, String prefix);
}
