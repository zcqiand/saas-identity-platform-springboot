package saas.identity.platform.controller;

import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import saas.identity.platform.security.InvalidCredentialsException;
import saas.identity.shared.dto.ErrorResponse;

/**
 * 全局异常 → HTTP 状态映射（M04.F04 + ADR-0020 错误契约）。
 *
 * <p>- 资源不存在 → 404 - 参数非法 → 400 - 鉴权失败 → 403 - 其他 → 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("NOT_FOUND");
    err.setMessage(e.getMessage() == null ? "resource not found" : e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
  }

  /** M01.F04 — 凭据错误是鉴权失败语义，401 + INVALID_CREDENTIALS（不是 400）。 */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("INVALID_CREDENTIALS");
    err.setMessage(e.getMessage() == null ? "invalid credentials" : e.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("BAD_REQUEST");
    err.setMessage(e.getMessage() == null ? "invalid argument" : e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("FORBIDDEN");
    err.setMessage(e.getMessage() == null ? "access denied" : e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
  }

  /**
   * FK 违反（23503）等约束冲突 → 400。此前裸炸 500 {timestamp,status,error,path}（Spring Boot 默认 error body），与家族
   * ErrorResponse 形状分叉；非法 clientId 写 oauth_*_token 就是这个口子。
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("BAD_REQUEST");
    err.setMessage("constraint violation: " + e.getMostSpecificCause().getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
  }

  /** 请求体不可解析（缺 body / JSON 坏）→ 400，同样消灭默认 error body。 */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("BAD_REQUEST");
    err.setMessage("malformed request body");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
  }

  /**
   * 2026-09-12 live 4-way 修复（R5）：@Valid 失败此前没人接 → 走 Spring 默认 /error body
   * {timestamp,status,error,path}，normalize（剔 code/message/details）后与 msw oracle 的 {} 分叉 （I64 空
   * body POST /admin/clients）。收敛为家族 ErrorResponse {code,message}。
   */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      org.springframework.web.bind.MethodArgumentNotValidException e) {
    String detail =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .orElse("validation failed");
    ErrorResponse err = new ErrorResponse();
    err.setCode("BAD_REQUEST");
    err.setMessage(detail);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
  }

  /** 方法参数级校验（Spring 6.1 起 @RequestParam/@PathVariable 上的约束走这条）。 */
  @ExceptionHandler(
      org.springframework.web.method.annotation.HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleMethodValidation(
      org.springframework.web.method.annotation.HandlerMethodValidationException e) {
    ErrorResponse err = new ErrorResponse();
    err.setCode("BAD_REQUEST");
    err.setMessage("validation failed");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
  }
}
