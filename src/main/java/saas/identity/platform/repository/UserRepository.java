package saas.identity.platform.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.enums.UserStatus;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  Page<UserEntity> findByTenantId(UUID tenantId, Pageable pageable);

  Page<UserEntity> findByTenantIdAndStatus(UUID tenantId, UserStatus status, Pageable pageable);

  Optional<UserEntity> findByTenantIdAndUsername(UUID tenantId, String username);

  Optional<UserEntity> findByTenantIdAndId(UUID tenantId, UUID id);

  long countByTenantId(UUID tenantId);

  boolean existsByTenantIdAndEmail(UUID tenantId, String email);

  boolean existsByTenantIdAndUsername(UUID tenantId, String username);

  List<UserEntity> findByIdIn(List<UUID> ids);
}
