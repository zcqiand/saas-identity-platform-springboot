package saas.identity.platform.repository;

// @impl M04.F03.I01 — book anchor (xr-know-007)

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.Generated.OauthCode;

public interface OauthCodeRepository extends JpaRepository<OauthCode, UUID> {
  Optional<OauthCode> findByCode(String code);

  // 5.123①：谓词 claim——返回影响行数（0 = 已被并发赢家消费），禁 read-then-delete 裸删。
  // derived delete 必须自带事务（控制器外层无 @Transactional）
  @Transactional
  long deleteByCode(String code);
}
