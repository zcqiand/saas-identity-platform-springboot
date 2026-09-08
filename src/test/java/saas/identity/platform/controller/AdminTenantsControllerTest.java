package saas.identity.platform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import saas.identity.platform.entity.Generated.Tenant;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.shared.api.AdminTenantsApi;
import saas.identity.shared.api.TenantRolesApi;
import saas.identity.shared.dto.CreateTenantRequest;
import saas.identity.shared.dto.UpdateTenantRequest;

/**
 * M00.F01 平台 admin 租户 CRUD 测试。
 */
@WebMvcTest(controllers = {AdminTenantsController.class})
class AdminTenantsControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @MockBean TenantRepository tenants;
  // Required for context wiring
  @MockBean TenantRolesApi tenantRolesApi;

  @Test
  @WithMockUser(roles = "platform_admin")
  void listTenants_returns200_withPaginatedItems() throws Exception {
    Tenant t = new Tenant();
    t.setId(UUID.randomUUID());
    t.setTenantKey("acme");
    t.setName("ACME Inc");
    t.setStatus((short) 1);

    when(tenants.findAll(any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 20), 1));

    mvc.perform(get("/api/v1/admin/tenants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.total").value(1));
  }

  @Test
  @WithMockUser(roles = "platform_admin")
  void createTenant_returns201_withCreatedBody() throws Exception {
    Tenant saved = new Tenant();
    saved.setId(UUID.randomUUID());
    saved.setTenantKey("acme");
    saved.setName("ACME Inc");
    saved.setStatus((short) 1);

    when(tenants.save(any(Tenant.class))).thenReturn(saved);

    CreateTenantRequest req = new CreateTenantRequest();
    req.setTenantKey("acme");
    req.setName("ACME Inc");

    mvc.perform(post("/api/v1/admin/tenants")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tenantKey").value("acme"));
  }

  @Test
  @WithMockUser(roles = "platform_admin")
  void getTenant_returns404_whenMissing() throws Exception {
    UUID id = UUID.randomUUID();
    when(tenants.findById(id)).thenReturn(Optional.empty());

    mvc.perform(get("/api/v1/admin/tenants/" + id))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser(roles = "platform_admin")
  void updateTenant_returns200_withUpdatedFields() throws Exception {
    UUID id = UUID.randomUUID();
    Tenant existing = new Tenant();
    existing.setId(id);
    existing.setTenantKey("acme");
    existing.setName("Old Name");
    existing.setStatus((short) 1);
    when(tenants.findById(id)).thenReturn(Optional.of(existing));
    when(tenants.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

    UpdateTenantRequest req = new UpdateTenantRequest();
    req.setName("New Name");
    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .patch("/api/v1/admin/tenants/" + id)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("New Name"));
  }
}
