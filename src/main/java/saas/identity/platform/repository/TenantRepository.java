package saas.identity.platform.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {}
