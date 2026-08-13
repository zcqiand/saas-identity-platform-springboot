package saas.identity.platform.enums;

public enum MenuStatus {
  ACTIVE,
  DISABLED;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
