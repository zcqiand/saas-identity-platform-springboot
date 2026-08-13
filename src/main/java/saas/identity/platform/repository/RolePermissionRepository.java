package saas.identity.platform.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.RolePermissionEntity;

public interface RolePermissionRepository
    extends JpaRepository<RolePermissionEntity, RolePermissionEntity.PK> {

  List<RolePermissionEntity> findByRoleId(UUID roleId);

  void deleteByRoleId(UUID roleId);
}
