package saas.identity.platform.enums;

public enum OAuthGrantType {
  AUTHORIZATION_CODE,
  REFRESH_TOKEN,
  CLIENT_CREDENTIALS,
  PASSWORD;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
