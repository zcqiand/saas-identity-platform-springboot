package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.Generated.OauthCode;

public interface OauthCodeRepository extends JpaRepository<OauthCode, UUID> {
  Optional<OauthCode> findByCode(String code);
}
