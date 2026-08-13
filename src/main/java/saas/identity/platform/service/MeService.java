package saas.identity.platform.service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.TenantMembershipEntity;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.repository.TenantMembershipRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.shared.dto.CurrentUser;
import saas.identity.shared.dto.MembershipStatus;
import saas.identity.shared.dto.SwitchTenantResponse;
import saas.identity.shared.dto.TenantMembership;

/** M00.F02 — 当前用户身份（whoami / 跨租户切换 / 我的租户）。 v0.4.0：从 InMemoryStore 迁到真实 DB。 */
@Service
public class MeService {

  private final UserRepository userRepository;
  private final TenantMembershipRepository membershipRepository;

  public MeService(UserRepository userRepository, TenantMembershipRepository membershipRepository) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
  }

  @Transactional(readOnly = true)
  public CurrentUser whoami(UUID userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found"));
    List<TenantMembershipEntity> memberships = membershipRepository.findByUserId(userId);
    List<TenantMembership> dtos =
        memberships.stream()
            .filter(m -> m.getStatus() != saas.identity.platform.enums.MembershipStatus.REMOVED)
            .map(this::toMembershipDto)
            .toList();
    UUID currentTenantId =
        memberships.stream()
            .filter(m -> m.getStatus() != saas.identity.platform.enums.MembershipStatus.REMOVED)
            .map(TenantMembershipEntity::getTenantId)
            .findFirst()
            .orElse(user.getTenantId());
    return new CurrentUser()
        .id(user.getId())
        .email(user.getEmail())
        .displayName(user.getDisplayName())
        .memberships(dtos)
        .currentTenantId(currentTenantId);
  }

  @Transactional(readOnly = true)
  public List<TenantMembership> listMyTenants(UUID userId) {
    return membershipRepository.findByUserId(userId).stream()
        .filter(m -> m.getStatus() != saas.identity.platform.enums.MembershipStatus.REMOVED)
        .map(this::toMembershipDto)
        .toList();
  }

  @Transactional
  public SwitchTenantResponse switchTenant(UUID userId, UUID tenantId) {
    TenantMembershipEntity m =
        membershipRepository
            .findByUserIdAndTenantId(userId, tenantId)
            .orElseThrow(() -> new SecurityException("not a member of this tenant"));
    if (m.getStatus() == saas.identity.platform.enums.MembershipStatus.REMOVED) {
      throw new SecurityException("not a member of this tenant");
    }
    long now = Instant.now().getEpochSecond();
    String header = b64url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
    String payload =
        b64url(
            "{\"sub\":\""
                + userId
                + "\",\"tenant_id\":\""
                + tenantId
                + "\",\"iat\":"
                + now
                + ",\"exp\":"
                + (now + 3600)
                + "}");
    return new SwitchTenantResponse()
        .accessToken(header + "." + payload + ".dev-placeholder")
        .refreshToken("refresh-" + userId + "-" + now)
        .expiresAt(java.time.OffsetDateTime.now().plusHours(1))
        .tenantId(tenantId);
  }

  private TenantMembership toMembershipDto(TenantMembershipEntity m) {
    return new TenantMembership()
        .id(m.getId())
        .userId(m.getUserId())
        .tenantId(m.getTenantId())
        .roleIds(
            m.getRoleIds() == null
                ? List.of()
                : m.getRoleIds().stream().map(UUID::toString).toList())
        .status(toDtoMembership(m.getStatus()))
        .joinedAt(m.getJoinedAt());
  }

  private saas.identity.shared.dto.MembershipStatus toDtoMembership(
      saas.identity.platform.enums.MembershipStatus s) {
    if (s == null) return MembershipStatus.ACTIVE;
    return MembershipStatus.valueOf(s.name());
  }

  private String b64url(String s) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
