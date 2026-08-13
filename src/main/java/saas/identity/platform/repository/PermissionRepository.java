package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.PermissionEntity;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

  Optional<PermissionEntity> findByCode(String code);

  boolean existsByCode(String code);
}
