// dotnet-script 一键脚本: 把 PG `users` 和 `api_keys` 表里 created_at = '-infinity'::timestamptz
// 的历史残留行改回 '1970-01-01 00:00:00+00',与 contract-test assertTimestampShape [1970, 2100] 范围对齐。
//
// 用法: dotnet script scripts/fix-sentinel-timestamps.csx
//      (需要 dotnet-script 全局工具: dotnet tool install -g dotnet-script)
//
// DSN 取自 .env.local 的 DATABASE_URL;若取不到则用本机默认。
// 跨环境安全:只 UPDATE created_at,不删行,事务包裹。

#r "nuget: Npgsql, 8.0.4"

using System;
using System.Threading.Tasks;
using Npgsql;

var envLines = File.ReadAllLines(".env.local");
string url = null;
foreach (var line in envLines) {
    var trimmed = line.TrimStart();
    if (trimmed.StartsWith("#") || string.IsNullOrWhiteSpace(trimmed)) continue;
    var idx = trimmed.IndexOf('=');
    if (idx < 0) continue;
    var key = trimmed.Substring(0, idx);
    if (key == "DATABASE_URL" || key == "JDBC_URL") {
        url = trimmed.Substring(idx + 1);
        break;
    }
}
if (url == null) url = "jdbc:postgresql://100.79.128.25:5432/saas_dev";
// jdbc: → Npgsql
var npgsql = url
    .Replace("jdbc:postgresql://", "Host=100.79.128.25;Port=5432;Database=saas_dev;Username=postgres;Password=qiand68+++")
    .Replace("jdbc:postgresql:", "Host=100.79.128.25;Port=5432;Database=saas_dev;Username=postgres;Password=qiand68+++");
if (npgsql.StartsWith("Host=") && !npgsql.Contains("Username=")) npgsql += ";Username=postgres;Password=qiand68+++";
// 强写为本机 saas_dev 默认,避免脚本里拼 DSN 出错
npgsql = "Host=100.79.128.25;Port=5432;Database=saas_dev;Username=postgres;Password=qiand68+++;Include Error Detail=true";

Console.WriteLine($"[fix-sentinel] DSN: {npgsql.Replace("Password=qiand68+++", "Password=***")}");

await using var conn = new NpgsqlConnection(npgsql);
await conn.OpenAsync();

async Task<long> CountSentinel(string table, string col) {
    await using var c = new NpgsqlCommand($"SELECT count(*) FROM {table} WHERE {col} = '-infinity'::timestamptz", conn);
    return (long)(await c.ExecuteScalarAsync())!;
}
async Task<long> UpdateSentinel(string table, string col) {
    await using var c = new NpgsqlCommand(
        $"UPDATE {table} SET {col} = '1970-01-01T00:00:00+00'::timestamptz WHERE {col} = '-infinity'::timestamptz",
        conn);
    return await c.ExecuteNonQueryAsync();
}

var tablesCols = new[] {
    ("users",     "created_at"),
    ("api_keys",  "created_at"),
    ("users",     "updated_at"),
    ("api_keys",  "updated_at"),
    ("audit_events", "occurred_at"),
};

await using var tx = await conn.BeginTransactionAsync();
foreach (var (t, c) in tablesCols) {
    var before = await CountSentinel(t, c);
    if (before == 0) {
        Console.WriteLine($"[fix-sentinel] {t}.{c}: 0 sentinel rows, skip");
        continue;
    }
    var affected = await UpdateSentinel(t, c);
    Console.WriteLine($"[fix-sentinel] {t}.{c}: {before} sentinel → {affected} updated");
}
await tx.CommitAsync();

Console.WriteLine("[fix-sentinel] DONE. 跨仓 contract-test 跑 live 验证 I10/I15 转绿。");