package saas.identity.platform.repository;

// @impl M00.F02.I03 — book anchor (xr-know-007)

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.SysUser;

public interface SysUserRepository extends JpaRepository<SysUser, UUID> {
  Optional<SysUser> findByUsername(String username);

  Optional<SysUser> findByEmail(String email);
}
