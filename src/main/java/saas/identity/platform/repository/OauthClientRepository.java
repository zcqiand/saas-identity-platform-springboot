package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.OauthClient;

public interface OauthClientRepository extends JpaRepository<OauthClient, UUID> {
  Optional<OauthClient> findByClientId(String clientId);
}
