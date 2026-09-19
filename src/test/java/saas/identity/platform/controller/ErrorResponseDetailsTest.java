package saas.identity.platform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import saas.identity.platform.entity.Generated.Tenant;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.shared.api.TenantRolesApi;
import saas.identity.shared.dto.CreateTenantRequest;

/**
 * ADR-0032 候选①：ErrorResponse.details 空对象序列化。
 *
 * <p>契约（shared main.tsp）details 为 optional；无 details 的错误响应不得序列化出 `"details":{}` 空键——生成链以
 * containerDefaultToNull=true 让 optional 容器默认 null + @JsonInclude(NON_NULL) 隐藏键。本测试锁该行为：修前红（空 map
 * 序列化 {}），修后绿。
 */
@WebMvcTest(controllers = {AdminTenantsController.class})
class ErrorResponseDetailsTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;

  @MockBean TenantRepository tenants;
  // Required for context wiring
  @MockBean TenantRolesApi tenantRolesApi;

  @Test
  @WithMockUser(roles = "platform_admin")
  void businessError_withoutDetails_omitsDetailsKey() throws Exception {
    when(tenants.save(any(Tenant.class))).thenThrow(new IllegalArgumentException("boom"));

    CreateTenantRequest req = new CreateTenantRequest();
    req.setTenantKey("acme");
    req.setName("ACME Inc");

    mvc.perform(
            post("/api/v1/admin/tenants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("boom"))
        .andExpect(jsonPath("$.details").doesNotExist());
  }
}
