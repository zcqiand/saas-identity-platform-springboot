package saas.identity.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * REQ-2026-001：后端根路径默认跳转 Swagger UI。
 *
 * <p>基础设施配置（不进业务 controller、不进功能树，REQ §4）。302 匿名跳转只占根路径， /api/v1/* 契约面零回归；view-controller
 * 不产 @RequestMapping 文档项，跳转端点不进 openapi 文档面。
 */
@Configuration
public class RootRedirectConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui.html");
  }
}
