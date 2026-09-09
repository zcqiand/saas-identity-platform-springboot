package saas.identity.platform.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.TenantMember;
import saas.identity.platform.entity.Generated.TenantMemberRole;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.platform.repository.TenantMemberRoleRepository;
import saas.identity.shared.api.TenantMembersApi;
import saas.identity.shared.dto.CreateSysUserRequest;
import saas.identity.shared.dto.SetTenantMemberRolesRequest;
import saas.identity.shared.dto.SysUser;
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

  public TenantMembersController(
      TenantMemberRepository members, TenantMemberRoleRepository memberRoles) {
    this.members = members;
    this.memberRoles = memberRoles;
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
    e.setId(UUID.randomUUID());
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
    return ResponseEntity.ok(toView(e, null));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersUpdateTenantUser(
      String tenantId, String userId, UpdateSysUserRequest body) {
    UUID memberUuid = UUID.fromString(userId);
    TenantMember e =
        members
            .findById(memberUuid)
            .orElseThrow(() -> new NoSuchElementException("member " + userId));
    return ResponseEntity.ok(toView(members.save(e), null));
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
    return ResponseEntity.ok(toView(members.save(e), null));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersInviteTenantUser(
      String tenantId, TenantMembersInviteTenantUserRequest body) {
    TenantMember e = new TenantMember();
    e.setId(UUID.randomUUID());
    e.setTenantId(UUID.fromString(tenantId));
    e.setUserId(UUID.randomUUID());
    e.setStatus((short) 0);
    return ResponseEntity.ok(toView(members.save(e), null));
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
    return ResponseEntity.ok(toView(e, null));
  }

  private TenantMemberView toView(TenantMember e) {
    return toView(e, null);
  }

  private TenantMemberView toView(TenantMember e, CreateSysUserRequest body) {
    TenantMemberView v = new TenantMemberView();
    saas.identity.shared.dto.TenantMember tm = new saas.identity.shared.dto.TenantMember();
    tm.setId(e.getId());
    tm.setTenantId(e.getTenantId());
    tm.setUserId(e.getUserId());
    tm.setStatus(
        e.getStatus() == null
            ? null
            : (e.getStatus().intValue() == 1
                ? TenantMemberStatus.ACTIVE
                : TenantMemberStatus.DISABLED));
    v.setMember(tm);
    if (body != null && body.getUsername() != null) {
      SysUser u = new SysUser();
      u.setId(e.getUserId());
      u.setUsername(body.getUsername());
      if (body.getEmail() != null) u.setEmail(body.getEmail());
      v.setUser(u);
    }
    v.setRoles(List.of());
    return v;
  }
}
