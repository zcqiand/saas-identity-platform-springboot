package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.OauthAccessToken;

public interface OauthAccessTokenRepository extends JpaRepository<OauthAccessToken, UUID> {
  Optional<OauthAccessToken> findByTokenId(String tokenId);
}
