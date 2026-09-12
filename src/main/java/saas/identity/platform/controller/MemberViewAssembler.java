package saas.identity.platform.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import saas.identity.platform.entity.Generated.SysRole;
import saas.identity.platform.entity.Generated.TenantMember;
import saas.identity.platform.entity.Generated.TenantMemberRole;
import saas.identity.platform.repository.SysRoleRepository;
import saas.identity.platform.repository.TenantMemberRoleRepository;
import saas.identity.shared.dto.TenantMembership;

/**
 * ADR-0032 扁平成员视图装配共享件：roleIds join + TenantMembership 组装。
 *
 * <p>TenantMembersController（成员六端点）、AuthController（login.availableTenants）、
 * MeController（/me、/me/tenants）三处都要「member 行 → roleIds / TenantMembership」， 提成公共组件防三处漂移。roleIds 真值
 * join：tenant_member_role ⨝ sys_role，<b>不</b>按 sys_role.tenant_id 过滤（2026-09-12 I03/I04 修复：member
 * 绑定即真值，租户归属由赋权端点负责校验——见 TenantMembersController S4；视图层再过滤会把合法绑定吞成空 roleIds）。
 */
@Component
public class MemberViewAssembler {

  private final TenantMemberRoleRepository memberRoles;
  private final SysRoleRepository roles;

  public MemberViewAssembler(TenantMemberRoleRepository memberRoles, SysRoleRepository roles) {
    this.memberRoles = memberRoles;
    this.roles = roles;
  }

  /**
   * member 的角色 ID 列表（字符串形态，与契约 roleIds items: string 一致）。绑定行为空 → 空列表。
   *
   * <p>roleIds = tenant_member_role join 行**原样**返回，不按 sys_role.tenant_id 过滤。 2026-09-12 四方
   * live（I03/I04）根因修复：msw oracle（seed tenant_member.json 逐字）与 nextjs 实测都把跨租户 assignment 原样吐出（seed 里
   * member c00000000006 @tenant2 挂 role a00000000002， 而该角色 tenant_id=tenant1），此前按 tenant_id 过滤把它吞成
   * []。对齐 aspnetcore MembershipViews 同日同修。
   */
  public List<String> roleIdsOf(TenantMember member) {
    return memberRoles.findByMemberId(member.getId()).stream()
        .map(TenantMemberRole::getRoleId)
        .map(UUID::toString)
        .toList();
  }

  /**
   * tenant_member 行 → TenantMembership（契约：id/userId/tenantId/roleIds/status/joinedAt）。 joinedAt =
   * tenant_member.created_at（家族约定，见 msw oracle seeds/tenant_member.json）。
   */
  public TenantMembership toMembership(TenantMember member) {
    TenantMembership m = new TenantMembership();
    m.setId(member.getId());
    m.setUserId(member.getUserId());
    m.setTenantId(member.getTenantId());
    m.setRoleIds(roleIdsOf(member));
    m.setStatus(MemberStatusMapper.fromDb(member.getStatus()));
    m.setJoinedAt(member.getCreatedAt());
    return m;
  }

  /** sys_role 查询口径复用（login availableTenants 之外的调用方不需要）。 */
  public List<SysRole> rolesOfTenant(UUID tenantId) {
    return roles.findByTenantId(tenantId);
  }
}
