package saas.identity.platform.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import saas.identity.platform.config.OpenApiConfig;
import saas.identity.platform.config.RootRedirectConfig;
import saas.identity.platform.config.SecurityConfig;

/**
 * REQ-2026-001 后端根路径默认跳转 Swagger —— 集成面测试（匿名 + 真实 SecurityFilterChain + 真实 springdoc）。
 *
 * <p>基础设施端点，不挂 @Fn、不进功能树（REQ §4，ADR-0027 同款判定）。 测试用最小 TestApp：只装 SecurityConfig +
 * RootRedirectConfig + HealthController + springdoc 自动配置， 不拉主应用全量 ComponentScan（DevDataFixer 等
 * 启动期写库 bean 会碰真库；lab 兄弟仓同款切割）。
 *
 * <p>断言面：AC-1 根路径 302 → /swagger-ui.html；AC-4 UI/docs 匿名 200；裸 /health 家族形状
 * {"status":"ok"}（aspnetcore 双仓先例）。全程不带 Authorization（匿名）。
 */
@SpringBootTest(classes = RootRedirectIntegrationTest.TestApp.class)
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      // SecurityConfig.jwtDecoder fail-fast：JWT_SIGNING_KEY 缺/弱(<32B) 抛 ISE 阻断启动
      "JWT_SIGNING_KEY=test-signing-key-32-bytes-minimum-padding!!",
    })
class RootRedirectIntegrationTest {

  /**
   * 最小 web 切片：全量 auto-config（springdoc 走 classpath 自动装配）但排除 DB 面。 用普通 @Configuration
   * 而非 @SpringBootConfiguration——后者会被同包 @WebMvcTest 的向上搜索捞走当根配置（AuthControllerLoginTest 等 4 个 slice
   * 因此 404），plain @Configuration 只有显式 classes= 引用的本测试会用。
   */
  @Configuration
  @EnableAutoConfiguration(
      exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
      })
  @Import({
    SecurityConfig.class,
    RootRedirectConfig.class,
    HealthController.class,
    OpenApiConfig.class
  })
  static class TestApp {}

  @Autowired MockMvc mvc;

  @Test
  void rootRedirectsAnonymouslyToSwaggerUiHtml() throws Exception {
    // AC-1：匿名 GET / → 302，Location = /swagger-ui.html（跳转端点本身，不跟随）
    mvc.perform(get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/swagger-ui.html"));
  }

  @Test
  void swaggerUiIsReachableAnonymously() throws Exception {
    // AC-4：springdoc 的 /swagger-ui.html 是 302 → /swagger-ui/index.html（静态资源）→ 200 text/html
    mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/swagger-ui/index.html"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
  }

  @Test
  void apiDocsExposeOpenapiDocumentAnonymously() throws Exception {
    // AC-4：/v3/api-docs 匿名 200 且是 OpenAPI 文档
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("\"openapi\"")));
  }

  @Test
  void healthEndpointReturnsFamilyShapeAnonymously() throws Exception {
    // 对齐项：裸 /health 匿名 200，家族统一 JSON 形状（aspnetcore 双仓 {"status":"ok"} 先例）
    mvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"));
  }
}
