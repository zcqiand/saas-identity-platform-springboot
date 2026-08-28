package saas.identity.platform.harness;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.array.UUIDArrayType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hibernate.annotations.Type;
import org.junit.jupiter.api.Test;
import saas.identity.platform.entity.ApiKeyEntity;

/**
 * 数组 UserType 映射守卫（回归 2026-08-28 线上 GET /api-keys 500）。
 *
 * <p>hypersistence-utils 的 StringArrayType / UUIDArrayType 只支持真数组属性（String[] / UUID[]）；用在 {@code
 * List<String>} 属性上编译期无感，读回时 ArrayUtil.unwrapArray → Array.newInstance(null, len)
 * NPE。ApiKeyEntity.scopes 曾因此 500（修法 ListArrayType）。 AppEntity 时代把它误诊为「hypersistence 空/非空数组
 * bug」用 @Transient 绕过，本案证伪。
 *
 * <p>本测试扫描全部 entity：凡 @Type 是数组 UserType 的字段，属性必须是匹配的类型。 类路径层断言，不需要 DB，堵住「单测全 mock、H2 镜像不了
 * text[]」留下的洞。
 *
 * <p>不挂 @Fn：这是映射层守卫，不是业务功能（同 SpringdocCompatibilityTest）。
 */
class ArrayUserTypeMappingGuardTest {

  private static final Set<Class<?>> ARRAY_USER_TYPES =
      Set.of(StringArrayType.class, UUIDArrayType.class);

  @Test
  void arrayUserTypesMustMatchFieldType() throws Exception {
    List<Class<?>> entities = KnownEntities.all();
    StringBuilder violations = new StringBuilder();
    assertTrue(entities.size() > 0, "KnownEntities 扫描不到 entity（类路径异常）");
    for (Class<?> entity : entities) {
      for (Field f : entity.getDeclaredFields()) {
        Type t = f.getAnnotation(Type.class);
        if (t == null) continue;
        boolean isArrayUserType = ARRAY_USER_TYPES.contains(t.value());
        boolean isListArrayType = t.value() == ListArrayType.class;
        if (!isArrayUserType && !isListArrayType) continue;
        boolean fieldIsArray = f.getType().isArray();
        boolean fieldIsList = List.class.isAssignableFrom(f.getType());
        if (isArrayUserType && fieldIsList) {
          violations.append(
              String.format(
                  "%n  %s.%s: %s 只支持数组属性，List 属性请用 ListArrayType",
                  entity.getSimpleName(), f.getName(), t.value().getSimpleName()));
        }
        if (isListArrayType && fieldIsArray) {
          violations.append(
              String.format(
                  "%n  %s.%s: ListArrayType 用于 List 属性，数组属性请用数组 UserType",
                  entity.getSimpleName(), f.getName()));
        }
        if (!fieldIsArray && !fieldIsList) {
          violations.append(
              String.format(
                  "%n  %s.%s: 数组 UserType 挂在非数组非 List 字段上", entity.getSimpleName(), f.getName()));
        }
      }
    }
    assertTrue(violations.length() == 0, "数组 UserType / 属性类型不匹配:" + violations);
  }

  @Test
  void apiKeyScopesMustUseListArrayType() throws Exception {
    // 直接点名 ApiKeyEntity.scopes —— 本次 500 的案发字段，显式断言防止将来回退。
    Field scopes = ApiKeyEntity.class.getDeclaredField("scopes");
    Type t = scopes.getAnnotation(Type.class);
    assertTrue(
        t != null && t.value() == ListArrayType.class,
        "ApiKeyEntity.scopes 必须用 ListArrayType（StringArrayType 在 List<String> 上读回 NPE）");
    assertTrue(List.class.isAssignableFrom(scopes.getType()));
  }
}

/** classpath 扫描辅助：扫描 saas.identity.platform.entity 包下全部 class（不引第三方库）。 */
final class KnownEntities {

  private KnownEntities() {}

  static List<Class<?>> all() throws Exception {
    String pkg = "saas.identity.platform.entity";
    String path = pkg.replace('.', '/');
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    java.net.URL url = cl.getResource(path);
    if (url == null) throw new IllegalStateException("entity package not on classpath: " + pkg);
    // 测试跑在 target/classes（目录协议）；jar 协议不出现于本仓测试切片。
    java.nio.file.Path dir = java.nio.file.Paths.get(url.toURI());
    List<Class<?>> out = new ArrayList<>();
    try (var stream = java.nio.file.Files.list(dir)) {
      for (java.nio.file.Path p : stream.toList()) {
        if (!p.toString().endsWith(".class") || p.getFileName().toString().contains("$")) continue;
        String cn = pkg + "." + p.getFileName().toString().replace(".class", "");
        out.add(Class.forName(cn));
      }
    }
    return out;
  }
}
