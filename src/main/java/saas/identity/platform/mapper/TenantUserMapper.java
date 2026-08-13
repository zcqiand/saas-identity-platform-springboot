package saas.identity.platform.mapper;

import java.util.List;
import java.util.UUID;
import saas.identity.platform.entity.UserEntity;
import saas.identity.shared.dto.CreateUserRequest;
import saas.identity.shared.dto.User;
import saas.identity.shared.dto.UserStatus;

/**
 * DTO ↔ Entity mapper（手写，不引 MapStruct）。 边界规则： - Service 持有 Repository + Mapper；Repository 不接触
 * DTO；DTO 不接触 Entity - roleIds：DTO 用 List<String>（UUID 字符串形式）；Entity 用 List<UUID>
 */
public final class TenantUserMapper {

  private TenantUserMapper() {}

  public static User toDto(UserEntity e) {
    if (e == null) return null;
    User u = new User();
    u.setId(e.getId());
    u.setTenantId(e.getTenantId());
    u.setUsername(e.getUsername());
    u.setEmail(e.getEmail());
    u.setDisplayName(e.getDisplayName());
    u.setStatus(toDtoStatus(e.getStatus()));
    u.setRoleIds(toRoleIdsString(e.getRoleIds()));
    u.setCreatedAt(e.getCreatedAt());
    u.setUpdatedAt(e.getUpdatedAt());
    return u;
  }

  public static UserEntity fromCreateRequest(UUID tenantId, CreateUserRequest req) {
    UserEntity e = new UserEntity();
    e.setTenantId(tenantId);
    e.setUsername(req.getUsername());
    e.setEmail(req.getEmail());
    e.setDisplayName(req.getDisplayName());
    e.setStatus(saas.identity.platform.enums.UserStatus.INVITED);
    e.setRoleIds(req.getRoleIds() == null ? List.of() : toRoleIdsUuid(req.getRoleIds()));
    // Phase 5：换 argon2.hash(req.getPassword())
    e.setPasswordHash(req.getPassword() == null ? null : "plain:" + req.getPassword());
    return e;
  }

  // === enum 转换 ===

  public static saas.identity.platform.enums.UserStatus toEntityStatus(UserStatus s) {
    if (s == null) return null;
    return switch (s) {
      case ACTIVE -> saas.identity.platform.enums.UserStatus.ACTIVE;
      case INVITED -> saas.identity.platform.enums.UserStatus.INVITED;
      case SUSPENDED -> saas.identity.platform.enums.UserStatus.SUSPENDED;
      case DISABLED -> saas.identity.platform.enums.UserStatus.DISABLED;
    };
  }

  public static UserStatus toDtoStatus(saas.identity.platform.enums.UserStatus s) {
    if (s == null) return null;
    return switch (s) {
      case ACTIVE -> UserStatus.ACTIVE;
      case INVITED -> UserStatus.INVITED;
      case SUSPENDED -> UserStatus.SUSPENDED;
      case DISABLED -> UserStatus.DISABLED;
    };
  }

  // === roleIds 转换 ===

  public static List<String> toRoleIdsString(List<UUID> uuids) {
    if (uuids == null) return List.of();
    return uuids.stream().map(UUID::toString).toList();
  }

  public static List<UUID> toRoleIdsUuid(List<String> strings) {
    if (strings == null) return List.of();
    return strings.stream().map(UUID::fromString).toList();
  }
}
