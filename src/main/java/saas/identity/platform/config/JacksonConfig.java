package saas.identity.platform.config;

import com.fasterxml.jackson.databind.Module;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 5.123 —— openapi 生成物 JsonNullable 字段的 Jackson 模块注册。
 *
 * <p>5.112① regen 后 DTO 里出现 JsonNullable&lt;UUID&gt;（EffectiveMenuNode.parentId），但
 * JsonNullableModule 一直没注册进 Spring context：Jackson 把 JsonNullable 当普通 bean 序列化，wire 出
 * {"present":false}/{"present":true}（present 连 value 都丢），违反自家契约 （parentId =
 * string/uuid/nullable）。注册后 undefined → 整键省略（模块实测行为，非字面 null；CT 归一 M96.F01.I03 明文 null≡缺失，与 rails/aspnetcore 收敛）、present → 裸值， 即 MeController 5.112①
 * 注释声明的意图。Spring Boot 自动装配收集所有 Module bean 进 主 ObjectMapper（含 MockMvc / 生产同一条序列化路径）。
 */
@Configuration
public class JacksonConfig {

  @Bean
  public Module jsonNullableModule() {
    return new JsonNullableModule();
  }
}
