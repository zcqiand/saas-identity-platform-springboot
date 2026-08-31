package saas.identity.platform.controller;

import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
 *   <li>NoSuchElementException / EntityNotFoundException → 404 NOT_FOUND（M05.F01.I05 物理删幂等 —
 *       contract-test I21：重复 DELETE 已不存在的 keyId 必须 404 而非 500）
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

  /**
   * 资源不存在 → 404 NOT_FOUND。M05.F01.I05 物理删幂等场景：DELETE 已不存在的 keyId 抛 NoSuchElementException，原 Spring
   * 默认 500 空 body；contract-test I21 期望 404。
   */
  @ExceptionHandler({
    NoSuchElementException.class,
    jakarta.persistence.EntityNotFoundException.class
  })
  public ResponseEntity<Map<String, String>> notFound(Exception ex) {
    return body(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
  }

  // Spring 6.1 给 ResponseEntity.status() 加了 @NonNull HttpStatusCode 形参；
  // 本 helper 的所有调用方都传 HttpStatus.NOT_FOUND 等字面量，非空可知，
  // 但 IDE 静态分析没法跨过私有方法边界推这一点，触发 unchecked-conversion 告警。
  // 抑制：调用方全部传字面量，非空由调用方保证。
  @SuppressWarnings("null")
  private ResponseEntity<Map<String, String>> body(HttpStatusCode status, String code, String msg) {
    return ResponseEntity.status(status)
        .body(Map.of("error", code, "error_description", msg == null ? "unknown" : msg));
  }
}
