package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.TenantMember;
import saas.identity.platform.entity.Generated.TenantMemberRole;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.platform.repository.TenantMemberRoleRepository;
import saas.identity.shared.api.TenantMembersApi;
import saas.identity.shared.dto.CreateSysUserRequest;
import saas.identity.shared.dto.SetTenantMemberRolesRequest;
import saas.identity.shared.dto.SysUser;
import saas.identity.shared.dto.SysUserStatus;
import saas.identity.shared.dto.TenantMemberStatus;
import saas.identity.shared.dto.TenantMemberView;
import saas.identity.shared.dto.TenantMembersChangeTenantUserStatusRequest;
import saas.identity.shared.dto.TenantMembersInviteTenantUserRequest;
import saas.identity.shared.dto.TenantMembersListTenantUsers200Response;
import saas.identity.shared.dto.UpdateSysUserRequest;

/** M00.F02 租户成员 CRUD + 邀请 + 状态切换 + M01.F02 角色绑定。skeleton。 */
@RestController
public class TenantMembersController implements TenantMembersApi {

  private final TenantMemberRepository members;
  private final TenantMemberRoleRepository memberRoles;
  private final SysUserRepository users;

  public TenantMembersController(
      TenantMemberRepository members,
      TenantMemberRoleRepository memberRoles,
      SysUserRepository users) {
    this.members = members;
    this.memberRoles = memberRoles;
    this.users = users;
  }

  @Override
  public ResponseEntity<TenantMembersListTenantUsers200Response> tenantMembersListTenantUsers(
      String tenantId, Integer page, Integer pageSize, TenantMemberStatus status) {
    int p = page == null ? 0 : page;
    int ps = pageSize == null ? 20 : pageSize;
    UUID tenantUuid = UUID.fromString(tenantId);
    var pg = members.findByTenantId(tenantUuid, PageRequest.of(p, ps));
    TenantMembersListTenantUsers200Response resp = new TenantMembersListTenantUsers200Response();
    resp.setItems(pg.getContent().stream().map(this::toView).toList());
    resp.setTotal(pg.getTotalElements());
    resp.setPage(p);
    resp.setPageSize(ps);
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersCreateTenantUser(
      String tenantId, CreateSysUserRequest body) {
    TenantMember e = new TenantMember();
    e.setTenantId(UUID.fromString(tenantId));
    e.setUserId(UUID.randomUUID());
    e.setStatus((short) 1);
    TenantMember saved = members.save(e);
    return ResponseEntity.ok(toView(saved, body));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersGetTenantUser(
      String tenantId, String userId) {
    UUID memberUuid = UUID.fromString(userId);
    TenantMember e =
        members
            .findById(memberUuid)
            .orElseThrow(() -> new NoSuchElementException("member " + userId));
    return ResponseEntity.ok(toView(e, (CreateSysUserRequest) null));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersUpdateTenantUser(
      String tenantId, String userId, UpdateSysUserRequest body) {
    UUID memberUuid = UUID.fromString(userId);
    TenantMember e =
        members
            .findById(memberUuid)
            .orElseThrow(() -> new NoSuchElementException("member " + userId));
    return ResponseEntity.ok(toView(members.save(e), (CreateSysUserRequest) null));
  }

  @Override
  public ResponseEntity<Void> tenantMembersDeleteTenantUser(String tenantId, String userId) {
    UUID memberUuid = UUID.fromString(userId);
    members.deleteById(memberUuid);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersChangeTenantUserStatus(
      String tenantId, String userId, TenantMembersChangeTenantUserStatusRequest body) {
    UUID memberUuid = UUID.fromString(userId);
    TenantMember e =
        members
            .findById(memberUuid)
            .orElseThrow(() -> new NoSuchElementException("member " + userId));
    if (body.getStatus() != null) {
      e.setStatus((short) (body.getStatus() == TenantMemberStatus.ACTIVE ? 1 : 0));
    }
    return ResponseEntity.ok(toView(members.save(e), (CreateSysUserRequest) null));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersInviteTenantUser(
      String tenantId, TenantMembersInviteTenantUserRequest body) {
    // I42 方案 C：邀请建真 sys_user（status=2 invited）+ member（status=1 active）。
    // email 缺失 fail-fast 400（ADR-0019：禁止兜底字面量）。
    String email = body == null || body.getEmail() == null ? "" : body.getEmail().trim();
    if (email.isEmpty()) {
      throw new IllegalArgumentException("email is required");
    }
    OffsetDateTime now = OffsetDateTime.now();
    saas.identity.platform.entity.Generated.SysUser u =
        new saas.identity.platform.entity.Generated.SysUser();
    u.setId(UUID.randomUUID());
    u.setUsername(email); // 家族约定：invitation 的 username = email（memberName 同源）
    u.setPassword(""); // notNull 列；受邀用户尚无凭据
    u.setEmail(email);
    u.setStatus((short) 2); // invited（家族约定 2026-09-10：1=active, 2=invited, 0=disabled）
    u.setFailedAttempts(0);
    u.setCreatedAt(now);
    u.setUpdatedAt(now);
    users.save(u);

    TenantMember e = new TenantMember();
    e.setTenantId(UUID.fromString(tenantId));
    e.setUserId(u.getId());
    e.setMemberName(email); // 家族约定：invitation 响应 memberName = username（= email）
    e.setIsOwner(false);
    e.setStatus((short) 1); // member 立即 active（I42 oracle：user=invited, member=active）
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    return ResponseEntity.ok(toView(members.save(e), u));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersAssignTenantMemberRoles(
      String tenantId, String userId, SetTenantMemberRolesRequest body) {
    UUID memberUuid = UUID.fromString(userId);
    memberRoles.deleteByMemberId(memberUuid);
    if (body != null && body.getRoleIds() != null) {
      for (String roleId : body.getRoleIds()) {
        TenantMemberRole r = new TenantMemberRole();
        r.setMemberId(memberUuid);
        r.setRoleId(UUID.fromString(roleId));
        memberRoles.save(r);
      }
    }
    TenantMember e =
        members
            .findById(memberUuid)
            .orElseThrow(() -> new NoSuchElementException("member " + userId));
    return ResponseEntity.ok(toView(e, (CreateSysUserRequest) null));
  }

  private TenantMemberView toView(TenantMember e) {
    return toView(e, (CreateSysUserRequest) null);
  }

  private TenantMemberView toView(TenantMember e, CreateSysUserRequest body) {
    saas.identity.platform.entity.Generated.SysUser u = null;
    if (body != null) {
      u = new saas.identity.platform.entity.Generated.SysUser();
      u.setId(e.getUserId());
      u.setUsername(body.getUsername());
      u.setEmail(body.getEmail());
    }
    return toView(e, u);
  }

  // I42：user 侧 status 三档映射（DB smallint 家族约定：1=active, 2=invited, else=disabled）
  private static SysUserStatus mapUserStatus(Short db) {
    if (db == null) {
      return SysUserStatus.DISABLED;
    }
    return switch (db.intValue()) {
      case 1 -> SysUserStatus.ACTIVE;
      case 2 -> SysUserStatus.INVITED;
      default -> SysUserStatus.DISABLED;
    };
  }

  private TenantMemberView toView(
      TenantMember e, saas.identity.platform.entity.Generated.SysUser u) {
    TenantMemberView v = new TenantMemberView();
    saas.identity.shared.dto.TenantMember tm = new saas.identity.shared.dto.TenantMember();
    tm.setId(e.getId());
    tm.setTenantId(e.getTenantId());
    tm.setUserId(e.getUserId());
    tm.setMemberName(e.getMemberName());
    tm.setIsOwner(e.getIsOwner());
    tm.setStatus(
        e.getStatus() == null
            ? null
            : (e.getStatus().intValue() == 1
                ? TenantMemberStatus.ACTIVE
                : TenantMemberStatus.DISABLED));
    tm.setCreatedAt(e.getCreatedAt());
    tm.setUpdatedAt(e.getUpdatedAt());
    v.setMember(tm);
    if (u != null) {
      SysUser du = new SysUser();
      du.setId(u.getId());
      du.setUsername(u.getUsername());
      du.setEmail(u.getEmail());
      du.setMobile(u.getMobile());
      du.setStatus(mapUserStatus(u.getStatus()));
      du.setFailedAttempts(u.getFailedAttempts());
      du.setLockedUntil(u.getLockedUntil());
      du.setCreatedAt(u.getCreatedAt());
      du.setUpdatedAt(u.getUpdatedAt());
      v.setUser(du);
    }
    v.setRoles(List.of());
    return v;
  }
}
