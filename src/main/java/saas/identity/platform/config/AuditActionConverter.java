package saas.identity.platform.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import saas.identity.shared.dto.AuditAction;

/**
 * Query 参数 conversion —— @RequestParam 不走 Jackson，必须显式注册 Converter。否则 Spring 用
 * Enum.valueOf("api_key_created") 失败 → 400 INVALID_REQUEST。
 *
 * <p>生成自 shared OpenAPI 的 AuditAction（snake_case 字符串）。同款模式：所有 snake_case enum query 参数都需要这种
 * converter。
 */
@Component
public class AuditActionConverter implements Converter<String, AuditAction> {
  @Override
  public AuditAction convert(@NotNull String source) {
    return AuditAction.fromValue(source);
  }
}
