package saas.identity.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import saas.identity.shared.dto.TenantMemberStatus;

/**
 * MVC 层配置：query 参数 enum 绑定。
 *
 * <p>2026-09-12 修复：GET /tenants/{t}/members?status=active → 400 「No enum constant」。 Spring 默认用
 * {@link org.springframework.core.convert.support.StringToEnumConverterFactory}（按 enum 名 ACTIVE
 * 精确匹配），而 API 契约层是字符串值（active/disabled，DB 是 smallint，见 CLAUDE 家族约定）。 这里注册按 @JsonValue 值绑定的
 * Converter，委托生成 DTO 的 {@link TenantMemberStatus#fromValue}； 不认识的值抛 IllegalArgumentException →
 * GlobalExceptionHandler 出 400 ErrorResponse。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    // 命名静态内部类而非匿名类（SpotBugs SIC_INNER_SHOULD_BE_STATIC_ANON）
    registry.addConverter(new StringToTenantMemberStatusConverter());
  }

  /** query 参数字符串 → TenantMemberStatus（按 @JsonValue 值匹配）。 */
  static final class StringToTenantMemberStatusConverter
      implements Converter<String, TenantMemberStatus> {

    @Override
    public TenantMemberStatus convert(String source) {
      // fromValue 不认识时抛 IllegalArgumentException（含原始值，便于排障）
      return TenantMemberStatus.fromValue(source);
    }
  }
}
