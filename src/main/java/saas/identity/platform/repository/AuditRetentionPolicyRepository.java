package saas.identity.platform.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import saas.identity.platform.entity.AuditRetentionPolicyEntity;

public interface AuditRetentionPolicyRepository
    extends JpaRepository<AuditRetentionPolicyEntity, UUID> {}
