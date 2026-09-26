package saas.identity.platform.repository;

// @impl M04.F03.I03 — book anchor (xr-know-007)

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.OauthRefreshToken;

public interface OauthRefreshTokenRepository extends JpaRepository<OauthRefreshToken, UUID> {
  Optional<OauthRefreshToken> findByRefreshToken(String refreshToken);
}
