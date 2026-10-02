package saas.identity.platform.controller;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 裸 /health 基础设施探针（REQ-2026-001 对齐项，用户已批准）。
 *
 * <p>家族统一 JSON 形状 {"status":"ok"}（aspnetcore 双仓先例），匿名 200 纯探针；@Hidden 不进 openapi 文档面；不进功能树（/health
 * 从未进树，rails/fastapi 同款先例）。DB 探活由 /actuator/health 与 gate L4 真库链负责。
 *
 * <p>仓内铁律「Controller 由 codegen 全覆盖」对本类例外：基础设施端点在 shared 契约外（REQ §4 已裁定）， codegen 无从产生，故手写——与 family
 * 各仓 health 端点同款手写先例。
 */
@Hidden
@RestController
public class HealthController {

  /** 家族统一健康探针 JSON 形状：{"status":"ok"}。 */
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}
