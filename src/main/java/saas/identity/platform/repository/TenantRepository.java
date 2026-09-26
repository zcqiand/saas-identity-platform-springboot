package saas.identity.platform.repository;

// @impl M00.F01.I01 — book anchor (xr-know-007)

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {}
