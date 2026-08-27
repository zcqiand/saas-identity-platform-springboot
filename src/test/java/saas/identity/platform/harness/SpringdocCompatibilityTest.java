package saas.identity.platform.harness;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springdoc.core.service.GenericResponseService;
import org.springframework.web.method.ControllerAdviceBean;

/**
 * 依赖二进制兼容性守卫（回归 2026-08-27 线上 /v3/api-docs 故障，两层）。
 *
 * <p>层 1: spring-web 6.2（Spring Boot 3.4 自带）删除了 {@code ControllerAdviceBean.<init>(Object)}，
 * springdoc &lt; 2.7.0 的 GenericResponseService 仍调用该构造器 → 运行时 NoSuchMethodError。
 *
 * <p>层 2: pom 显式钉的 swagger-annotations-jakarta 版本若低于 springdoc 传递的 swagger-core （nearest-wins 压制），
 * 缺失 Schema$SchemaResolution 等新内部类 → NoClassDefFoundError， 且被 ExceptionTranslationFilter 吞掉转成 401，
 * 极难排查（swagger-config 200 掩盖精确路径 401）。
 *
 * <p>两层都是运行时错误，编译期与单测无感知。本测试在类路径层面断言组合不可能出现。
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
            + "升级 springdoc-openapi-starter-webmvc-ui 到 >= 2.7.0。");
  }

  @Test
  void swaggerAnnotationsMustMatchSwaggerCore() {
    // Schema$SchemaResolution 在 swagger-annotations 2.2.22 之后引入。
    // 显式钉的 annotations 版本低于 springdoc 传递的 swagger-core 时（nearest-wins），
    // 该类缺失 → /v3/api-docs NoClassDefFoundError，被 ExceptionTranslationFilter 转成 401。
    String annotations =
        io.swagger.v3.oas.annotations.media.Schema.class.getPackage().getImplementationVersion();
    String core = io.swagger.v3.oas.models.OpenAPI.class.getPackage().getImplementationVersion();
    assertTrue(
        annotations != null && core != null && compareAtLeast(annotations, core),
        "swagger-annotations-jakarta "
            + annotations
            + " 低于 swagger-models/core "
            + core
            + "（springdoc 传递）。nearest-wins 会让旧 annotations 上 classpath，"
            + "/v3/api-docs 运行时 NoClassDefFoundError（Schema$SchemaResolution 缺失），"
            + "并被 ExceptionTranslationFilter 转成 401 掩盖。"
            + "把 pom 里 swagger-annotations-jakarta 对齐到 springdoc 的 swagger-core 版本。");
  }

  /** springdoc >= 2.7.0 视为支持 spring-web 6.2（语义化版本比较，只看数字段）。 */
  private static boolean supportsSpringWeb62(String version) {
    String normalized = version.startsWith("v") ? version.substring(1) : version;
    String[] parts = normalized.split("[-.]");
    int major = Integer.parseInt(parts[0]);
    int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    return major > 2 || (major == 2 && minor >= 7);
  }

  /** a >= b（语义化版本比较，只看数字段）。 */
  private static boolean compareAtLeast(String a, String b) {
    int[] va = parse(a);
    int[] vb = parse(b);
    for (int i = 0; i < 3; i++) {
      if (va[i] != vb[i]) return va[i] > vb[i];
    }
    return true;
  }

  private static int[] parse(String version) {
    String normalized = version.startsWith("v") ? version.substring(1) : version;
    String[] parts = normalized.split("[-.]");
    int[] out = new int[3];
    for (int i = 0; i < 3; i++) {
      out[i] = i < parts.length ? Integer.parseInt(parts[i]) : 0;
    }
    return out;
  }
}
