package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.RoleMenuGrantEntity;

public interface RoleMenuGrantRepository extends JpaRepository<RoleMenuGrantEntity, UUID> {

  Optional<RoleMenuGrantEntity> findByRoleId(UUID roleId);
}
