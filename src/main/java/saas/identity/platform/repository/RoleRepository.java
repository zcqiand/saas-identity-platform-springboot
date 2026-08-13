package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

  Page<RoleEntity> findByTenantId(UUID tenantId, Pageable pageable);

  Optional<RoleEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  Optional<RoleEntity> findByTenantIdAndCode(UUID tenantId, String code);

  boolean existsByTenantIdAndCode(UUID tenantId, String code);
}
