package saas.identity.platform.controller;

import saas.identity.shared.dto.TenantMemberStatus;

/**
 * 成员/会员 status 双形态映射（ADR-0032 扁平成员视图 + 全家族四值约定）。
 *
 * <p>DB smallint（tenant_member.status / sys_user.status 共用一套字典）：
 *
 * <ul>
 *   <li>1 = active
 *   <li>2 = invited
 *   <li>3 = suspended（2026-09-12 ADR-0032 扩；此前 1/2/else 三档）
 *   <li>0 = disabled（0/未知/NULL 读路径一律归 disabled）
 * </ul>
 */
public final class MemberStatusMapper {

  public static final short DB_ACTIVE = 1;
  public static final short DB_INVITED = 2;
  public static final short DB_SUSPENDED = 3;
  public static final short DB_DISABLED = 0;

  private MemberStatusMapper() {}

  /** DB smallint → API enum（读路径：3 → "suspended"，0/未知 → "disabled"）。 */
  public static TenantMemberStatus fromDb(Short db) {
    if (db == null) {
      return TenantMemberStatus.DISABLED;
    }
    return switch (db.intValue()) {
      case 1 -> TenantMemberStatus.ACTIVE;
      case 2 -> TenantMemberStatus.INVITED;
      case 3 -> TenantMemberStatus.SUSPENDED;
      default -> TenantMemberStatus.DISABLED;
    };
  }

  /** API enum → DB smallint（写路径："suspended" → 3）。 */
  public static Short toDb(TenantMemberStatus status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case ACTIVE -> DB_ACTIVE;
      case INVITED -> DB_INVITED;
      case SUSPENDED -> DB_SUSPENDED;
      case DISABLED -> DB_DISABLED;
    };
  }
}
