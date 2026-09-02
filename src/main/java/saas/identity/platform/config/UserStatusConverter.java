package saas.identity.platform.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import saas.identity.shared.dto.UserStatus;

/**
 * Query 参数 conversion —— @RequestParam 不走 Jackson，必须显式注册 Converter。
 *
 * <p>2026-09-01 contract-test I71：GET /tenants/{t}/users?status=active（小写，SSOT 契约值）。 Spring 默认
 * Enum.valueOf("active") 只认大写 ACTIVE → 400。同款模式见 {@link AuditActionConverter}（audit ?action=
 * 先例）；fromValue 吃 SSOT 的 snake_case/lowercase 值。
 */
@Component
public class UserStatusConverter implements Converter<String, UserStatus> {
  @Override
  public UserStatus convert(@NotNull String source) {
    return UserStatus.fromValue(source);
  }
}
