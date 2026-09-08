package saas.identity.platform.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.SysRoleMenu;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, UUID> {
  List<SysRoleMenu> findByRoleId(UUID roleId);
  void deleteByRoleId(UUID roleId);
}
