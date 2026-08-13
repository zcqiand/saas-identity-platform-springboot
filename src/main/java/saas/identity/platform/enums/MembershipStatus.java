package saas.identity.platform.enums;

public enum MembershipStatus {
  ACTIVE,
  INVITED,
  REMOVED;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
