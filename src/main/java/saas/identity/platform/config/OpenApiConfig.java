package saas.identity.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * v0.1.13 起 Swagger UI 在线访问。
 *
 * <p>SSOT 是 <code>saas-identity-platform-shared/generated/openapi/openapi.yaml</code>。
 * openapi-generator 在 build time 把 spec 编成本仓 <code>src/main/java/.../shared/api/*.java</code> 12 个
 * controller 接口 (带完整 swagger v3 注解)，由本仓 9 个 controller 实现这些接口， 注解在 runtime 被 springdoc 自动扫描合并进
 * OpenAPI 3.1 doc。无双写。
 *
 * <p>对外公开路径 (SecurityConfig.java v0.1.13 同步 permitAll)：
 *
 * <ul>
 *   <li><code>https://saas-springboot.xiangru.uk/swagger-ui/index.html</code>
 *   <li><code>https://saas-springboot.xiangru.uk/v3/api-docs</code>
 *   <li><code>https://saas-springboot.xiangru.uk/v3/api-docs.yaml</code>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI saasOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("SaaS 多租户多应用身份平台 API")
                .version("v0.1.x")
                .description(
                    "Controller interfaces 由 saas-identity-platform-shared 的 OpenAPI spec codegen 而成，"
                        + "本仓手写 controller 实现这些接口，运行时 OpenAPI doc 由 springdoc 自动合并。")
                .contact(
                    new Contact()
                        .name("SaaS Identity Platform")
                        .url("https://saas-springboot.xiangru.uk")));
  }
}
