package saas.identity.platform.enums;

public enum UserStatus {
  ACTIVE,
  INVITED,
  SUSPENDED,
  DISABLED;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
