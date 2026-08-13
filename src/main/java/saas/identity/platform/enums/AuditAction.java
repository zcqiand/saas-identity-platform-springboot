package saas.identity.platform.enums;

public enum AuditAction {
  USER_CREATED,
  USER_UPDATED,
  USER_DELETED,
  ROLE_ASSIGNED,
  ROLE_REVOKED,
  LOGIN_SUCCESS,
  LOGIN_FAILED,
  OAUTH_TOKEN_ISSUED,
  API_KEY_CREATED,
  API_KEY_REVOKED;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
