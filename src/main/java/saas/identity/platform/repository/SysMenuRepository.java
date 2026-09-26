package saas.identity.platform.repository;

// @impl M04.F04.I01 — book anchor (xr-know-007)

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.SysMenu;

public interface SysMenuRepository extends JpaRepository<SysMenu, UUID> {
  List<SysMenu> findByClientId(String clientId);
}
