package saas.identity.platform.security;

/**
 * 用户名/密码错误（M01.F04）。
 *
 * <p>必须独立于 IllegalArgumentException：后者被 GlobalExceptionHandler 映射 400， 而凭据错误的契约语义是 401（错密码 login →
 * 401 INVALID_CREDENTIALS， 对齐 nextjs/aspnetcore 家族行为）。failed_attempts/lockedUntil 计数逻辑不受影响。
 */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("invalid credentials");
  }

  public InvalidCredentialsException(String message) {
    super(message);
  }
}
