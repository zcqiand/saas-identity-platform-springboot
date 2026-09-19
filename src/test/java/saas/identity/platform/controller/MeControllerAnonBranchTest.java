package saas.identity.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import saas.identity.shared.dto.CurrentUser;

/**
 * 项 5.36（2026-09-18 family-leftovers remediation）—— MeController.meWhoami 防御分支序列化形状。
 *
 * <p>userId == null 防御分支（Security 前置挡匿名的不可达性未实证，分支保留）此前直接 {@code new CurrentUser()}，其 memberships 为
 * null；契约（shared TSP CurrentUser）里 memberships 是 required —— null 序列化违反契约形状。裁定：补
 * setMemberships(List.of())，不删分支。本测试直调该分支路径（无 SecurityContext → currentUserId() 返回 null），断言
 * memberships 为空数组而非 null（DTO 层 + JSON 序列化双层）。
 */
class MeControllerAnonBranchTest {

  private MeController controller;

  private final ObjectMapper json = new ObjectMapper();

  @BeforeEach
  void setup() {
    SecurityContextHolder.clearContext();
    // 防御分支不触达任何 repository/jwt —— mock 注入仅为满足构造器签名（构造器注入，非字段注入）
    controller =
        new MeController(
            mock(saas.identity.platform.repository.TenantMemberRepository.class),
            mock(saas.identity.platform.repository.TenantMemberRoleRepository.class),
            mock(saas.identity.platform.repository.SysRoleMenuRepository.class),
            mock(saas.identity.platform.repository.SysMenuRepository.class),
            mock(saas.identity.platform.repository.TenantRepository.class),
            mock(saas.identity.platform.repository.SysUserRepository.class),
            mock(MemberViewAssembler.class),
            mock(saas.identity.platform.security.JwtIssuer.class));
  }

  @AfterEach
  void teardown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void anonBranchMembershipsIsEmptyArrayNotContractNull() throws Exception {
    ResponseEntity<CurrentUser> res = controller.meWhoami();
    assertEquals(200, res.getStatusCode().value());
    CurrentUser body = res.getBody();
    assertNotNull(body, "防御分支也必须返回 CurrentUser body");
    assertNotNull(body.getMemberships(), "契约 required 字段 memberships 不得为 null");
    assertFalse(!body.getMemberships().isEmpty(), "无认证上下文 memberships 应为空数组而非非空");
    JsonNode node = json.readTree(json.writeValueAsString(body));
    org.junit.jupiter.api.Assertions.assertTrue(
        node.has("memberships") && node.get("memberships").isArray(),
        "JSON 序列化必须含 memberships 数组（契约 required）");
    assertEquals(0, node.get("memberships").size(), "memberships 应为空数组");
  }
}
