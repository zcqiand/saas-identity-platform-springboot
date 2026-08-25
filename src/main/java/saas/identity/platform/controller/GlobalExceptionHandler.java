package saas.identity.platform.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import saas.identity.platform.service.OauthService;

/**
 * OAuth 端点参数校验错误 → 4xx JSON（Phase 6 follow-up 落地）。
 *
 * <p>此前 InvalidScopeException 等未处理 → 500 空 body，lab 后端 EnsureSuccessStatusCode 只见裸 500，排障无从区分（曾把
 * scope 不匹配当网络/DB 故障查了一轮）。
 *
 * <p>错误码映射（对齐 saas-aspnetcore Program.cs UseExceptionHandler 与 saas-nextjs OAuthError）：
 *
 * <ul>
 *   <li>InvalidClientException / InvalidGrantException → 401 UNAUTHORIZED
 *   <li>InvalidRedirectUriException / InvalidScopeException / IllegalArgumentException → 400
 *       INVALID_REQUEST
 *   <li>其余 RuntimeException → 500 INTERNAL_ERROR（Spring 默认，不在此拦）
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
    OauthService.InvalidClientException.class,
    OauthService.InvalidGrantException.class
  })
  public ResponseEntity<Map<String, String>> unauthorized(Exception ex) {
    return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
  }

  @ExceptionHandler({
    OauthService.InvalidRedirectUriException.class,
    OauthService.InvalidScopeException.class,
    IllegalArgumentException.class
  })
  public ResponseEntity<Map<String, String>> badRequest(Exception ex) {
    return body(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage());
  }

  private ResponseEntity<Map<String, String>> body(HttpStatus status, String code, String msg) {
    return ResponseEntity.status(status)
        .body(Map.of("error", code, "error_description", msg == null ? "unknown" : msg));
  }
}
