package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.SysUser;
import saas.identity.platform.entity.Generated.TenantMember;
import saas.identity.platform.entity.Generated.TenantMemberRole;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.platform.repository.TenantMemberRoleRepository;
import saas.identity.platform.security.TenantGuard;
import saas.identity.shared.api.TenantMembersApi;
import saas.identity.shared.dto.CreateSysUserRequest;
import saas.identity.shared.dto.SetTenantMemberRolesRequest;
import saas.identity.shared.dto.SysUserStatus;
import saas.identity.shared.dto.TenantMemberStatus;
import saas.identity.shared.dto.TenantMemberUserView;
import saas.identity.shared.dto.TenantMemberView;
import saas.identity.shared.dto.TenantMembersChangeTenantUserStatusRequest;
import saas.identity.shared.dto.TenantMembersInviteTenantUserRequest;
import saas.identity.shared.dto.TenantMembersListTenantUsers200Response;
import saas.identity.shared.dto.UpdateSysUserRequest;

/**
 * M00.F02 租户成员 CRUD + 邀请 + 状态切换 + M01.F02 角色绑定。
 *
 * <p>ADR-0032（2026-09-12）：成员 list/create/get/patch/put-roles/patch-status 六端点改扁平 {@link
 * TenantMemberUserView}，路径参数 {userId} 语义 = sys_user.id（经 tenant_member.tenant_id + user_id 寻址，不再是
 * tenant_member.id）。invitations 端点保持嵌套 TenantMemberView 不动（同家族裁决）。
 */
@RestController
@Transactional
public class TenantMembersController implements TenantMembersApi {

  private final TenantMemberRepository members;
  private final TenantMemberRoleRepository memberRoles;
  private final SysUserRepository users;
  private final saas.identity.platform.repository.SysRoleRepository sysRoles;
  private final TenantGuard tenantGuard;
  private final MemberViewAssembler assembler;

  public TenantMembersController(
      TenantMemberRepository members,
      TenantMemberRoleRepository memberRoles,
      SysUserRepository users,
      saas.identity.platform.repository.SysRoleRepository sysRoles,
      TenantGuard tenantGuard,
      MemberViewAssembler assembler) {
    this.members = members;
    this.memberRoles = memberRoles;
    this.users = users;
    this.sysRoles = sysRoles;
    this.tenantGuard = tenantGuard;
    this.assembler = assembler;
  }

  @Override
  public ResponseEntity<TenantMembersListTenantUsers200Response> tenantMembersListTenantUsers(
      String tenantId, Integer page, Integer pageSize, TenantMemberStatus status) {
    tenantGuard.verifyPathTenant(tenantId);
    int p = page == null ? 0 : page;
    int ps = pageSize == null ? 20 : pageSize;
    UUID tenantUuid = UUID.fromString(tenantId);
    // status query 参数过滤（ADR-0032）：分页前 DB 级执行（S2 修复，对齐 aspnetcore），
    // total = 过滤后计数；视图 status 现读 member 行（S1），与过滤键天然一致。
    // 2026-09-13 排序对齐（积压清偿）：家族约定 list = created_at DESC
    // （nextjs ORDER BY created_at DESC / aspnetcore OrderByDescending(CreatedAt)
    // 早已实现，本仓 PageRequest 无 Sort 是漏网 —— 无排序时 PG 返回堆序，
    // 与两兄弟及 msw 镜像在运行期新建成员后必分叉）。
    // 2026-09-18 tiebreak 显式化：家族 seed 多行 created_at 相同，仅按 created_at 排序时
    // PG 返回堆序 → 顺序不稳定；tiebreak = id ASC（对齐 findByUserId 的 id ASC 先例）。
    var sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id"));
    var pg =
        (status == null)
            ? members.findByTenantId(tenantUuid, PageRequest.of(p, ps, sort))
            : members.findByTenantIdAndStatus(
                tenantUuid, MemberStatusMapper.toDb(status), PageRequest.of(p, ps, sort));
    // 扁平视图顶层是 user 行：一页 member 对应一批 user，批量取避免 N+1。
    var userMap =
        users.findAllById(pg.getContent().stream().map(TenantMember::getUserId).toList()).stream()
            .collect(java.util.stream.Collectors.toMap(SysUser::getId, u -> u));
    List<TenantMemberUserView> items =
        pg.getContent().stream()
            .filter(m -> userMap.containsKey(m.getUserId()))
            .map(m -> toView(m, userMap.get(m.getUserId())))
            .toList();
    TenantMembersListTenantUsers200Response resp = new TenantMembersListTenantUsers200Response();
    resp.setItems(items);
    resp.setTotal(pg.getTotalElements());
    resp.setPage(p);
    resp.setPageSize(ps);
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<TenantMemberUserView> tenantMembersCreateTenantUser(
      String tenantId, CreateSysUserRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    // 9/7 SSOT pivot：POST member 建 sys_user + tenant_member（对齐 nextjs 参照实现）。
    // ADR-0032：响应换扁平 TenantMemberUserView（顶层 id = user.id）。
    if (body == null || body.getUsername() == null || body.getEmail() == null) {
      throw new IllegalArgumentException("username and email are required");
    }
    OffsetDateTime now = OffsetDateTime.now();
    SysUser u = new SysUser();
    u.setUsername(body.getUsername());
    u.setEmail(body.getEmail());
    u.setMobile(body.getMobile());
    // 家族 dev 种子约定（同 nextjs）：password 列存 "plain:{password}" 占位。
    u.setPassword("plain:" + (body.getPassword() == null ? "" : body.getPassword()));
    u.setStatus(MemberStatusMapper.DB_ACTIVE);
    u.setFailedAttempts(0);
    u.setCreatedAt(now);
    u.setUpdatedAt(now);
    u = users.save(u);

    TenantMember e = new TenantMember();
    e.setTenantId(UUID.fromString(tenantId));
    e.setUserId(u.getId());
    e.setMemberName(body.getUsername());
    e.setIsOwner(false);
    e.setStatus(MemberStatusMapper.DB_ACTIVE);
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    return ResponseEntity.ok(toView(members.save(e), u));
  }

  @Override
  public ResponseEntity<TenantMemberUserView> tenantMembersGetTenantUser(
      String tenantId, String userId) {
    tenantGuard.verifyPathTenant(tenantId);
    TenantMember e = resolveMember(tenantId, userId);
    return ResponseEntity.ok(toView(e, loadUser(e)));
  }

  @Override
  public ResponseEntity<TenantMemberUserView> tenantMembersUpdateTenantUser(
      String tenantId, String userId, UpdateSysUserRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    TenantMember e = resolveMember(tenantId, userId);
    SysUser u = loadUser(e);
    if (body != null) {
      if (body.getEmail() != null) {
        u.setEmail(body.getEmail());
      }
      if (body.getMobile() != null) {
        u.setMobile(body.getMobile());
      }
      // 5.13-①（2026-09-20 人裁）：契约 UpdateSysUserRequest 已删 status 字段 ——
      // 状态变更唯一通道 = 专职 /status 端点（tenantMembersChangeTenantUserStatus）。
      // 旧实现把 body.status 落 sys_user.status（用户级 3 值），与扁平视图读
      // tenant_member.status 的 S1 语义（见 toView 注释）静默分叉，随契约收紧一并删除。
      u.setUpdatedAt(OffsetDateTime.now());
      u = users.save(u);
    }
    return ResponseEntity.ok(toView(e, u));
  }

  @Override
  public ResponseEntity<Void> tenantMembersDeleteTenantUser(String tenantId, String userId) {
    tenantGuard.verifyPathTenant(tenantId);
    TenantMember e = resolveMember(tenantId, userId);
    memberRoles.deleteByMemberId(e.getId());
    members.delete(e);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TenantMemberUserView> tenantMembersChangeTenantUserStatus(
      String tenantId, String userId, TenantMembersChangeTenantUserStatusRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    TenantMember e = resolveMember(tenantId, userId);
    if (body.getStatus() != null) {
      Short db = MemberStatusMapper.toDb(body.getStatus());
      // member 行与 user 行同写字典（MemberStatusMapper 四值）：switch 门槛读 member.status
      // != 0（S5），扁平视图 status 也读 member 行（S1），此端点双写保证两处一致。
      e.setStatus(db);
      e.setUpdatedAt(OffsetDateTime.now());
      e = members.save(e);
      SysUser u = loadUser(e);
      u.setStatus(db);
      u.setUpdatedAt(OffsetDateTime.now());
      users.save(u);
    }
    return ResponseEntity.ok(toView(e, loadUser(e)));
  }

  @Override
  public ResponseEntity<TenantMemberView> tenantMembersInviteTenantUser(
      String tenantId, TenantMembersInviteTenantUserRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    // I42 方案 C：邀请建真 sys_user（status=2 invited）+ member（status=1 active）。
    // email 缺失 fail-fast 400（ADR-0019：禁止兜底字面量）。
    // ADR-0032：invitations 保持嵌套 TenantMemberView 不动。
    String email = body == null || body.getEmail() == null ? "" : body.getEmail().trim();
    if (email.isEmpty()) {
      throw new IllegalArgumentException("email is required");
    }
    OffsetDateTime now = OffsetDateTime.now();
    SysUser u = new SysUser();
    // 禁止手动 setId（@GeneratedValue UUID）—— merge 会当 detached 走乐观锁
    // （ObjectOptimisticLockingFailureException，memory: springboot-write-path-double-bug）。
    u.setUsername(email); // 家族约定：invitation 的 username = email（memberName 同源）
    u.setPassword(""); // notNull 列；受邀用户尚无凭据
    u.setEmail(email);
    u.setStatus(MemberStatusMapper.DB_INVITED);
    u.setFailedAttempts(0);
    u.setCreatedAt(now);
    u.setUpdatedAt(now);
    u = users.save(u);

    TenantMember e = new TenantMember();
    e.setTenantId(UUID.fromString(tenantId));
    e.setUserId(u.getId());
    e.setMemberName(email); // 家族约定：invitation 响应 memberName = username（= email）
    e.setIsOwner(false);
    e.setStatus(
        MemberStatusMapper.DB_ACTIVE); // member 立即 active（I42 oracle：user=invited, member=active）
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    return ResponseEntity.ok(toNestedView(members.save(e), u));
  }

  @Override
  public ResponseEntity<TenantMemberUserView> tenantMembersAssignTenantMemberRoles(
      String tenantId, String userId, SetTenantMemberRolesRequest body) {
    tenantGuard.verifyPathTenant(tenantId);
    TenantMember e = resolveMember(tenantId, userId);
    memberRoles.deleteByMemberId(e.getId());
    if (body != null && body.getRoleIds() != null) {
      // S4 修复（2026-09-12 四方一致）：只接受本租户的 sys_role，外来/未知 roleId 静默忽略
      // （对齐 aspnetcore Roles() 按 r.TenantId == tid 过滤；此前跨租户 role 照单全收）。
      java.util.Set<UUID> tenantRoleIds =
          sysRoles.findByTenantId(UUID.fromString(tenantId)).stream()
              .map(saas.identity.platform.entity.Generated.SysRole::getId)
              .collect(java.util.stream.Collectors.toSet());
      for (String roleId : body.getRoleIds()) {
        UUID rid = UUID.fromString(roleId);
        if (!tenantRoleIds.contains(rid)) {
          continue;
        }
        TenantMemberRole r = new TenantMemberRole();
        r.setMemberId(e.getId());
        r.setRoleId(rid);
        memberRoles.save(r);
      }
    }
    return ResponseEntity.ok(toView(e, loadUser(e)));
  }

  /** ADR-0032 寻址：{userId} = sys_user.id，经 (tenant_id, user_id) 找 member 行；寻不到 → 404。 */
  private TenantMember resolveMember(String tenantId, String userId) {
    return members
        .findByTenantIdAndUserId(UUID.fromString(tenantId), UUID.fromString(userId))
        .orElseThrow(
            () -> new NoSuchElementException("member user " + userId + " in tenant " + tenantId));
  }

  private SysUser loadUser(TenantMember member) {
    return users
        .findById(member.getUserId())
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    "sys_user " + member.getUserId() + " (member " + member.getId() + ")"));
  }

  /**
   * 扁平视图（ADR-0032）：顶层 id = user.id，roleIds = 真 join。
   *
   * <p>S1 修复（2026-09-12 四方一致）：status 读 <b>tenant_member.status</b>（成员域语义 SSOT，对齐 aspnetcore
   * MembershipViews / msw membership 行）。此前读 sys_user.status， invitation 场景（user=invited/2,
   * member=active/1）同一资源两仓返回不同值。
   */
  private TenantMemberUserView toView(TenantMember m, SysUser u) {
    TenantMemberUserView v = new TenantMemberUserView();
    v.setId(u.getId());
    v.setTenantId(m.getTenantId());
    v.setUsername(u.getUsername());
    v.setEmail(u.getEmail());
    v.setStatus(MemberStatusMapper.fromDb(m.getStatus()));
    v.setRoleIds(assembler.roleIdsOf(m));
    v.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt() : m.getCreatedAt());
    v.setUpdatedAt(u.getUpdatedAt() != null ? u.getUpdatedAt() : m.getUpdatedAt());
    return v;
  }

  // 5.13-①：mapSysUserStatus（sys_user.status 三值字典）随 PATCH body.status 改写逻辑一并删除。

  // ==== invitations 专用：嵌套 TenantMemberView（ADR-0032 保持不动） ====

  private TenantMemberView toNestedView(TenantMember e, SysUser u) {
    TenantMemberView v = new TenantMemberView();
    saas.identity.shared.dto.TenantMember tm = new saas.identity.shared.dto.TenantMember();
    tm.setId(e.getId());
    tm.setTenantId(e.getTenantId());
    tm.setUserId(e.getUserId());
    tm.setMemberName(e.getMemberName());
    tm.setIsOwner(e.getIsOwner());
    tm.setStatus(MemberStatusMapper.fromDb(e.getStatus()));
    tm.setCreatedAt(e.getCreatedAt());
    tm.setUpdatedAt(e.getUpdatedAt());
    v.setMember(tm);
    saas.identity.shared.dto.SysUser du = new saas.identity.shared.dto.SysUser();
    du.setId(u.getId());
    du.setUsername(u.getUsername());
    du.setEmail(u.getEmail());
    du.setMobile(u.getMobile());
    du.setStatus(mapSysUserStatusEnum(u.getStatus()));
    du.setFailedAttempts(u.getFailedAttempts());
    du.setLockedUntil(u.getLockedUntil());
    du.setCreatedAt(u.getCreatedAt());
    du.setUpdatedAt(u.getUpdatedAt());
    v.setUser(du);
    v.setRoles(List.of());
    return v;
  }

  private static SysUserStatus mapSysUserStatusEnum(Short db) {
    if (db == null) {
      return SysUserStatus.DISABLED;
    }
    return switch (db.intValue()) {
      case 1 -> SysUserStatus.ACTIVE;
      case 2 -> SysUserStatus.INVITED;
      default -> SysUserStatus.DISABLED;
    };
  }
}
