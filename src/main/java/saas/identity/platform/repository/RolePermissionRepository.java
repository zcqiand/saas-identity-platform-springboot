package saas.identity.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.RolePermissionEntity;

public interface RolePermissionRepository
    extends JpaRepository<RolePermissionEntity, RolePermissionEntity.PK> {

  List<RolePermissionEntity> findByRoleId(UUID roleId);

  /** 批量：避免 list() 的 N+1。 */
  List<RolePermissionEntity> findByRoleIdIn(Collection<UUID> roleIds);

  void deleteByRoleId(UUID roleId);
}
