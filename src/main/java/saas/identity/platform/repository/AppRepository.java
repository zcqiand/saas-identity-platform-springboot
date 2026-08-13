package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.AppEntity;

public interface AppRepository extends JpaRepository<AppEntity, UUID> {

  Page<AppEntity> findAll(Pageable pageable);

  Optional<AppEntity> findByCode(String code);

  Optional<AppEntity> findByClientId(String clientId);
}
