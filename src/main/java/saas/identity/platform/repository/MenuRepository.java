package saas.identity.platform.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, UUID> {

  List<MenuEntity> findByAppId(UUID appId);

  List<MenuEntity> findByAppIdAndParentId(UUID appId, UUID parentId);

  List<MenuEntity> findByParentIdIsNull();
}
