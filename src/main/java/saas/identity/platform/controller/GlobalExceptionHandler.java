package saas.identity.platform.controller;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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
}
