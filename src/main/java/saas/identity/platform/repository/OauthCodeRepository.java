package saas.identity.platform.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.OauthCodeEntity;

/**
 * V009 — OAuth 2.0 authorization_code + refresh_token JPA 仓库。
 *
 * <p>findByCode: /token endpoint 用,authorization_code 一次性消费 + refresh_token 旋转换发都走它。
 */
public interface OauthCodeRepository extends JpaRepository<OauthCodeEntity, UUID> {

  Optional<OauthCodeEntity> findByCode(String code);
}
