package saas.identity.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import saas.identity.platform.harness.Fn;
import saas.identity.platform.repository.SysRoleRepository;
import saas.identity.platform.repository.SysUserRepository;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.platform.repository.TenantMemberRoleRepository;
import saas.identity.platform.security.TenantGuard;

/**
 * 2026-09-18 排序显式化收口：成员列表家族约定 created_at DESC（同 audit occurred_at DESC 家族约定）。
 *
 * <p>同 created_at 种子（家族 seed 多行 createdAt 相同）下顺序必须稳定 —— tiebreak = id ASC，
 * 对齐本仓 findByUserId 的「created_at ASC, id ASC 与 msw 种子序一致」先例（TenantMemberRepository）。
 * 无 tiebreak 时 PG 返回堆序，与 nextjs/aspnetcore/msw 镜像在运行期必分叉。
 */
class TenantMembersControllerSortTest {

  @Test
  @Fn({"M00.F02.I01"})
  void membersList_sortsCreatedAtDesc_withDeterministicIdTiebreak() {
    TenantMemberRepository members = mock(TenantMemberRepository.class);
    TenantMemberRoleRepository memberRoles = mock(TenantMemberRoleRepository.class);
    SysUserRepository users = mock(SysUserRepository.class);
    SysRoleRepository sysRoles = mock(SysRoleRepository.class);
    TenantGuard tenantGuard = mock(TenantGuard.class);

    TenantMembersController ctrl =
        new TenantMembersController(
            members, memberRoles, users, sysRoles, tenantGuard, new MemberViewAssembler(memberRoles, sysRoles));
    when(members.findByTenantId(any(UUID.class), any(Pageable.class))).thenReturn(Page.empty());

    ctrl.tenantMembersListTenantUsers(
        "11111111-1111-1111-1111-111111111111", null, null, null);

    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(members).findByTenantId(any(UUID.class), pageable.capture());
    Sort sort = pageable.getValue().getSort();

    Sort.Order createdAt = sort.getOrderFor("createdAt");
    assertNotNull(createdAt, "list 必须显式按 createdAt 排序");
    assertEquals(Sort.Direction.DESC, createdAt.getDirection(), "家族约定：created_at DESC");
    Sort.Order id = sort.getOrderFor("id");
    assertNotNull(id, "同 created_at 必须有确定性 tiebreak（id）");
    assertEquals(Sort.Direction.ASC, id.getDirection(), "tiebreak 方向 = id ASC（对齐 findByUserId 先例）");
  }
}
