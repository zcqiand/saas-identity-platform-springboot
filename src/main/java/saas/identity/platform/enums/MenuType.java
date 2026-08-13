package saas.identity.platform.enums;

public enum MenuType {
  GROUP,
  PAGE,
  ACTION;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
