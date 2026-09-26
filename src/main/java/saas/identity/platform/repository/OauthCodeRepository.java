package saas.identity.platform.repository;

// @impl M04.F03.I01 — book anchor (xr-know-007)

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.OauthCode;

public interface OauthCodeRepository extends JpaRepository<OauthCode, UUID> {
  Optional<OauthCode> findByCode(String code);
}
