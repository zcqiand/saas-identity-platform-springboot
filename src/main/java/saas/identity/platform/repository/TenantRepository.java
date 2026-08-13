package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.TenantEntity;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {

  Optional<TenantEntity> findByCode(String code);

  boolean existsByCode(String code);
}
