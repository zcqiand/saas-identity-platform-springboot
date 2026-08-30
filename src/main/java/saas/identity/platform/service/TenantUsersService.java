package saas.identity.platform.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.UserEntity;
import saas.identity.platform.mapper.TenantUserMapper;
import saas.identity.platform.repository.TenantMembershipRepository;
import saas.identity.platform.repository.UserRepository;
import saas.identity.shared.dto.CreateUserRequest;
import saas.identity.shared.dto.TenantUsersListUsers200Response;
import saas.identity.shared.dto.User;
import saas.identity.shared.dto.UserStatus;

/**
 * M01.F01 — 租户内用户 CRUD（DB-backed，ADR-0007）。
 *
 * <p>构造器注入（CLAUDE.md §2）；TenantGuard 由 Controller 层负责，本 service 信任 tenantId。
 */
@Service
public class TenantUsersService {

  private static final int PAGE_SIZE_DEFAULT = 20;
  private static final int PAGE_SIZE_MAX = 100;

  private final UserRepository userRepository;
  private final TenantMembershipRepository membershipRepository;

  public TenantUsersService(
      UserRepository userRepository, TenantMembershipRepository membershipRepository) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
  }

  // M01.F01.I01
  @Transactional(readOnly = true)
  public TenantUsersListUsers200Response listUsers(
      UUID tenantId, Integer page, Integer pageSize, UserStatus status) {
    int p = page == null ? 0 : Math.max(0, page);
    int ps = pageSize == null ? PAGE_SIZE_DEFAULT : Math.min(PAGE_SIZE_MAX, Math.max(1, pageSize));
    Pageable pageable = PageRequest.of(p, ps);

    Page<UserEntity> result =
        (status == null)
            ? userRepository.findByTenantId(tenantId, pageable)
            : userRepository.findByTenantIdAndStatus(
                tenantId, TenantUserMapper.toEntityStatus(status), pageable);

    // 2026-08-30 contract-test：UserEntity.roleIds 是 @Transient，authoritative 在 memberships
    // 这里为每个 user 单查 membership（page size 20 默认 → 21 次查询）；Phase 5 与 UserEntity 一起优化批量取。
    List<User> items =
        result.getContent().stream()
            .map(
                user ->
                    TenantUserMapper.toDto(
                        user,
                        membershipRepository
                            .findByUserIdAndTenantId(user.getId(), tenantId)
                            .map(m -> m.getRoleIds())
                            .orElse(List.of())))
            .toList();
    return new TenantUsersListUsers200Response(items, p, ps, result.getTotalElements());
  }

  // M01.F01.I02
  @Transactional
  public User createUser(UUID tenantId, CreateUserRequest body) {
    UserEntity entity = TenantUserMapper.fromCreateRequest(tenantId, body);
    UserEntity saved = userRepository.save(entity);
    return TenantUserMapper.toDto(saved);
  }

  // M01.F01.I04 — GET /tenants/{t}/users/{u}
  @Transactional(readOnly = true)
  public User getUser(UUID tenantId, UUID userId) {
    UserEntity entity =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    List<UUID> roleIds =
        membershipRepository
            .findByUserIdAndTenantId(userId, tenantId)
            .map(m -> m.getRoleIds())
            .orElse(List.of());
    return TenantUserMapper.toDto(entity, roleIds);
  }

  // M01.F01.I05
  @Transactional
  public void deleteUser(UUID tenantId, UUID userId) {
    UserEntity existing =
        userRepository
            .findByTenantIdAndId(tenantId, userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
    userRepository.delete(existing);
  }
}
