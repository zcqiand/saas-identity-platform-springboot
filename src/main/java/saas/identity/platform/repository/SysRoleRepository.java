package saas.identity.platform.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.SysRole;

public interface SysRoleRepository extends JpaRepository<SysRole, UUID> {}
