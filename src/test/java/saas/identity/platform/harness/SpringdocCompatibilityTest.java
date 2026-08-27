package saas.identity.platform.harness;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springdoc.core.service.GenericResponseService;
import org.springframework.web.method.ControllerAdviceBean;

/**
 * 依赖二进制兼容性守卫（回归 2026-08-27 线上 /v3/api-docs 500）。
 *
 * <p>spring-web 6.2（Spring Boot 3.4 自带）删除了 {@code ControllerAdviceBean.<init>(Object)}， springdoc
 * &lt; 2.7.0 的 GenericResponseService 仍调用该构造器 → 运行时 NoSuchMethodError。 编译期无感知（NoSuchMethodError
 * 是运行时错误），MockMvc/单测也覆盖不到，只有真打 /v3/api-docs 才炸。本测试在类路径层面断言这个组合不可能出现。
 *
 * <p>不挂 @Fn：这是依赖矩阵守卫，不是业务功能。
 */
class SpringdocCompatibilityTest {

  @Test
  void springdocMustSupportSpringWeb6_2ControllerAdviceBean() {
    boolean hasLegacyCtor =
        Arrays.stream(ControllerAdviceBean.class.getConstructors())
            .anyMatch(c -> c.getParameterCount() == 1 && c.getParameterTypes()[0] == Object.class);

    if (hasLegacyCtor) {
      // spring-web < 6.2：旧构造器还在，任何 springdoc 2.x 都兼容。
      return;
    }

    // spring-web >= 6.2：旧构造器已删，springdoc 必须 >= 2.7.0（#2687 修复版本）。
    String springdocVersion = GenericResponseService.class.getPackage().getImplementationVersion();
    assertTrue(
        springdocVersion != null && supportsSpringWeb62(springdocVersion),
        "spring-web "
            + ControllerAdviceBean.class.getPackage().getImplementationVersion()
            + " 删除了 ControllerAdviceBean.<init>(Object)，但 classpath 上的 springdoc 是 "
            + springdocVersion
            + "（< 2.7.0）。/v3/api-docs 会在运行时抛 NoSuchMethodError。"
            + "升级 springdoc-openapi-starter-webmvc-ui 到 >= 2.7.0（推荐 2.8.x）。");
  }

  /** springdoc >= 2.7.0 视为支持 spring-web 6.2（语义化版本比较，只看数字段）。 */
  private static boolean supportsSpringWeb62(String version) {
    String normalized = version.startsWith("v") ? version.substring(1) : version;
    String[] parts = normalized.split("[-.]");
    int major = Integer.parseInt(parts[0]);
    int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    return major > 2 || (major == 2 && minor >= 7);
  }
}
