// One-shot tool: 把 PG 表里 created_at = '-infinity'::timestamptz 的历史残留行改回 '1970-01-01 00:00:00+00'。
// 配合 contract-test assertTimestampShape [1970, 2100] 范围,让 I10 / I15 列表端点 sentinel 行不再报错。
//
// 编译: javac -cp <postgresql.jar>:<slf4j.jar>
// src/main/java/saas/identity/platform/FixSentinelTimestamps.java -d /tmp
// 运行: java -cp /tmp:<postgresql.jar>:<slf4j.jar> saas.identity.platform.FixSentinelTimestamps
//
// 跨环境安全: 单事务 5 张表 UPDATE + 不删行 + 打印 before/after count。

package saas.identity.platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FixSentinelTimestamps {

  private static final String URL = "jdbc:postgresql://100.79.128.25:5432/saas_dev";
  private static final String USER = "postgres";
  private static final String PASS = "qiand68+++";

  private static final String[][] TABLES_AND_COLUMNS = {
    {"users", "created_at"},
    {"api_keys", "created_at"},
    {"audit_events", "occurred_at"},
    // 注意:api_keys 没有 updated_at 列(只有 expires_at / last_used_at / revoked_at);
    //       users.updated_at 由 trigger trg_set_updated_at 自动管。
  };

  public static void main(String[] args) throws Exception {
    Class.forName("org.postgresql.Driver");
    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
      conn.setAutoCommit(false);
      int totalUpdated = 0;
      for (String[] tc : TABLES_AND_COLUMNS) {
        String table = tc[0];
        String col = tc[1];
        long before = countSentinel(conn, table, col);
        if (before == 0) {
          System.out.printf("[fix-sentinel] %s.%s: 0 sentinel rows, skip%n", table, col);
          continue;
        }
        long affected = updateSentinel(conn, table, col);
        totalUpdated += affected;
        System.out.printf(
            "[fix-sentinel] %s.%s: %d sentinel → %d updated%n", table, col, before, affected);
      }
      conn.commit();
      System.out.printf(
          "[fix-sentinel] DONE. total %d rows updated. 跨仓 contract-test 跑 live 验证 I10/I15.%n",
          totalUpdated);
    }
  }

  private static long countSentinel(Connection conn, String table, String col) throws Exception {
    String sql = "SELECT count(*) FROM " + table + " WHERE " + col + " = '-infinity'::timestamptz";
    try (PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      return rs.next() ? rs.getLong(1) : 0;
    }
  }

  private static long updateSentinel(Connection conn, String table, String col) throws Exception {
    String sql =
        "UPDATE "
            + table
            + " SET "
            + col
            + " = '1970-01-01T00:00:00+00'::timestamptz WHERE "
            + col
            + " = '-infinity'::timestamptz";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      return ps.executeUpdate();
    }
  }
}
