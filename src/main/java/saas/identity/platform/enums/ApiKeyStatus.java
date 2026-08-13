package saas.identity.platform.enums;

public enum ApiKeyStatus {
  ACTIVE,
  REVOKED,
  EXPIRED;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
