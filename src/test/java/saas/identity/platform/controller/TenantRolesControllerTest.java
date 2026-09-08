package saas.identity.platform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import saas.identity.platform.entity.Generated.SysRole;
import saas.identity.platform.repository.SysRoleRepository;
import saas.identity.shared.dto.CreateSysRoleRequest;

/**
 * M00.F03 租户角色 CRUD 测试。
 */
@WebMvcTest(controllers = {TenantRolesController.class})
class TenantRolesControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @MockBean SysRoleRepository roles;

  @Test
  @WithMockUser(roles = "tenant_admin")
  void createRole_returns200_withBody() throws Exception {
    UUID tenantId = UUID.randomUUID();
    SysRole saved = new SysRole();
    saved.setId(UUID.randomUUID());
    saved.setTenantId(tenantId);
    saved.setRoleCode("viewer");
    saved.setRoleName("查看者");
    saved.setStatus((short) 1);

    when(roles.save(any(SysRole.class))).thenReturn(saved);

    CreateSysRoleRequest req = new CreateSysRoleRequest();
    req.setClientId("lab-management");
    req.setRoleCode("viewer");
    req.setRoleName("查看者");

    mvc.perform(post("/api/v1/tenants/" + tenantId + "/roles")
            .param("clientId", "lab-management")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roleCode").value("viewer"))
        .andExpect(jsonPath("$.roleName").value("查看者"));
  }

  @Test
  @WithMockUser(roles = "tenant_admin")
  void listRoles_returns200_paginated() throws Exception {
    SysRole r = new SysRole();
    r.setId(UUID.randomUUID());
    r.setRoleCode("admin");
    r.setRoleName("管理员");
    r.setStatus((short) 1);
    when(roles.findAll(any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(r), PageRequest.of(0, 20), 1));

    mvc.perform(get("/api/v1/tenants/" + UUID.randomUUID() + "/roles")
            .param("clientId", "lab-management"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[0].roleCode").value("admin"));
  }

  @Test
  @WithMockUser(roles = "tenant_admin")
  void deleteRole_returns204() throws Exception {
    UUID tenantId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();

    mvc.perform(delete("/api/v1/tenants/" + tenantId + "/roles/" + roleId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }
}
