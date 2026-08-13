package saas.identity.platform.enums;

// DB-side enum mirrors（与 shared SQL CREATE TYPE 1:1）。
// 见 saas-identity-platform-shared/sql/migrations/V001 + sql/README.md §PG enum。
//
// 命名约定：Java enum 用 UPPER_SNAKE，PG enum value 用 lower_snake（与 TypeSpec 一致）。
// 持久化时通过 @Enumerated(EnumType.STRING) + @JdbcTypeCode(SqlTypes.NAMED_ENUM) 映射。

public enum TenantStatus {
  ACTIVE,
  SUSPENDED,
  ARCHIVED;

  public String toDbValue() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
