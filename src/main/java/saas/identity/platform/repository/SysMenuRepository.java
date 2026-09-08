package saas.identity.platform.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.SysMenu;

public interface SysMenuRepository extends JpaRepository<SysMenu, UUID> {
  List<SysMenu> findByClientId(String clientId);
}
