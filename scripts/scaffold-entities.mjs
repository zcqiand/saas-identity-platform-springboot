// scripts/scaffold-entities.mjs — JDBC 反向工程从真库生成 JPA entity（DB-First, ADR-0025）
//
// 设计：
// - 取代原 hibernate-tools-maven-plugin 路径（与 Hibernate 6 集成不稳定）
// - 直接读 information_schema + pg_catalog，emit Java entity 类到
//   src/main/java/saas/identity/platform/entity/Generated/<Table>.java
// - 简化版输出：仅 @Entity + @Table + @Id + @Column；手写 @PreUpdate / @Convert 等
//   业务逻辑通过 extends Generated.<Entity> 叠加（与 ADR-0025 D7 一致）
//
// 用法：
//   node scripts/scaffold-entities.mjs                       # default saas_dev
//   DATABASE_URL=postgresql://... node scripts/scaffold-entities.mjs
//
// 前提：shared 仓已 db:migrate；saas_dev schema 与 src/db/schema.ts 一致

import { createRequire } from "node:module";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { writeFileSync, mkdirSync, existsSync } from "node:fs";

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, "..");

// 借 pg driver（springboot 是 Java 仓，没 node_modules；走 sibling nextjs）
const requireFromRuntime = createRequire(
  resolve("/app/node_modules", "pg/package.json"),
);
let pg;
try {
  pg = requireFromRuntime("pg");
} catch {
  const nextjsRoot = resolve(ROOT, "../saas-identity-platform-nextjs");
  const requireFromNext = createRequire(resolve(nextjsRoot, "package.json"));
  pg = requireFromNext("pg");
}
const { Client } = pg;

const DATABASE_URL =
  process.env.DATABASE_URL ??
  "postgresql://postgres:qiand68%2B%2B%2B@100.79.128.25:5432/saas_dev";

const OUTPUT_DIR = resolve(
  ROOT,
  "src/main/java/saas/identity/platform/entity/Generated",
);

const EXCLUDE_TABLES = new Set([
  // Drizzle tracking — 不是业务表
  "__drizzle_migrations",
  // Drizzle 在 __drizzle_migrations 旁的 journal 表也不该有，跳过
]);

// PG 类型 → Java 类型 + JdbcTypeCode 注解
// data_type 字段值：uuid / text / character varying / integer / bigint / boolean / jsonb / timestamp with time zone / ARRAY / USER-DEFINED
const PG_TYPE_MAP = {
  uuid: { java: "UUID", imports: ["java.util.UUID"] },
  text: { java: "String" },
  "character varying": { java: "String" },
  integer: { java: "Integer", box: true },
  bigint: { java: "Long", box: true },
  boolean: { java: "Boolean", box: true },
  jsonb: {
    java: "Map<String, Object>",
    imports: ["java.util.Map"],
    annotations: ['@JdbcTypeCode(SqlTypes.JSON)'],
    extraImports: ["org.hibernate.annotations.JdbcTypeCode", "org.hibernate.type.SqlTypes"],
  },
  "timestamp without time zone": {
    java: "OffsetDateTime",
    imports: ["java.time.OffsetDateTime"],
  },
  "timestamp with time zone": {
    java: "OffsetDateTime",
    imports: ["java.time.OffsetDateTime"],
    columnDef: "timestamptz",
  },
  date: { java: "LocalDate", imports: ["java.time.LocalDate"] },
};

// camelCase 转换：snake_case → camelCase
function snakeToCamel(s) {
  return s.replace(/_([a-z])/g, (_, c) => c.toUpperCase());
}

// PascalCase 转换：snake_case → PascalCase
function snakeToPascal(s) {
  const camel = snakeToCamel(s);
  return camel[0].toUpperCase() + camel.slice(1);
}

// 根据 PG 列类型生成 Java 字段
function mapJavaType(pgType, udtName) {
  // 数组类型
  if (pgType === "ARRAY") {
    // udtName 带 _ 前缀表示数组元素类型（如 _uuid 表示 uuid[]）
    const elemUdT = udtName.replace(/^_/, "");
    const elem = PG_TYPE_MAP[elemUdT];
    if (!elem) throw new Error(`Unsupported array elem udt: ${elemUdT}`);
    // PG enum 数组单独处理
    if (elemUdT.endsWith("_enum") || isEnumType(elemUdT)) {
      // 不展开；实际 scaffold 不太常见，简化：用 String 列表
      return {
        java: `List<String>`,
        imports: ["java.util.List"],
        columnType: `${elemUdT}[]`,
      };
    }
    return {
      java: `List<${elem.box ? "Long" : elem.java}>`,
      imports: ["java.util.List", ...(elem.imports ?? [])],
      columnType: `${elemUdT}[]`,
    };
  }
  // enum 类型
  if (pgType === "USER-DEFINED" || isEnumType(udtName)) {
    // scaffold 简化：用 String；手写层用 AttributeConverter 升级
    return {
      java: "String",
      columnType: udtName,
      isEnum: true,
    };
  }
  const m = PG_TYPE_MAP[pgType];
  if (!m) throw new Error(`Unsupported PG type: ${pgType} (${udtName})`);
  return {
    java: m.java,
    imports: m.imports ?? [],
    columnType: m.columnDef ?? pgType,
    extraAnnotations: m.annotations ?? [],
    extraImports: m.extraImports ?? [],
    box: m.box,
  };
}

const ENUM_TYPES = new Set([
  "tenant_status",
  "user_status",
  "membership_status",
  "api_key_status",
  "app_status",
  "oauth_grant_type",
  "menu_type",
  "menu_status",
  "audit_action",
]);

function isEnumType(udtName) {
  return ENUM_TYPES.has(udtName);
}

async function main() {
  const client = new Client({
    connectionString: DATABASE_URL,
    connectionTimeoutMillis: 10000,
  });
  await client.connect();
  console.log(`[scaffold-entities] 连接 ${DATABASE_URL.replace(/:[^:@/]+@/, ":***@")}`);

  // 1. 读所有表
  const { rows: tables } = await client.query(`
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    ORDER BY table_name
  `);
  const filtered = tables.filter((t) => !EXCLUDE_TABLES.has(t.table_name));
  console.log(`[scaffold-entities] 发现 ${filtered.length} 张表`);

  // 2. 读所有 enum
  const { rows: enums2 } = await client.query(`
    SELECT t.typname, ARRAY(
      SELECT e.enumlabel
      FROM pg_enum e
      WHERE e.enumtypid = t.oid
      ORDER BY e.enumsortorder
    ) AS labels
    FROM pg_type t
    WHERE t.typtype = 'e'
    ORDER BY t.typname
  `);
  for (const e of enums2) {
    ENUM_TYPES.add(e.typname);
    // 同步到 PG_TYPE_MAP（enum 用 String 即可）
    PG_TYPE_MAP[e.typname] = { java: "String", box: false };
  }

  // 3. 读所有列
  for (const tbl of filtered) {
    const { rows: cols } = await client.query(
      `
      SELECT column_name, data_type, udt_name, is_nullable, column_default,
             character_maximum_length
      FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = $1
      ORDER BY ordinal_position
    `,
      [tbl.table_name],
    );

    const className = snakeToPascal(tbl.table_name);

    // 收集 imports
    const importsSet = new Set([
      "jakarta.persistence.Entity",
      "jakarta.persistence.Table",
      "jakarta.persistence.Id",
      "jakarta.persistence.Column",
      "jakarta.persistence.GeneratedValue",
      "jakarta.persistence.GenerationType",
    ]);
    for (const c of cols) {
      const mapped = mapJavaType(c.data_type, c.udt_name);
      mapped.imports?.forEach((i) => importsSet.add(i));
      mapped.extraImports?.forEach((i) => importsSet.add(i));
    }
    const imports = Array.from(importsSet).sort();

    // 字段
    const fields = cols.map((c) => {
      const mapped = mapJavaType(c.data_type, c.udt_name);
      const camel = snakeToCamel(c.column_name);
      const nullable = c.is_nullable === "YES";

      let annotations = [];
      if (c.column_name === "id") {
        annotations.push("@Id");
        annotations.push(
          '@GeneratedValue(strategy = GenerationType.UUID)',
        );
      }
      let colDef = `name = "${c.column_name}"`;
      if (mapped.columnType) colDef += `, columnDefinition = "${mapped.columnType}"`;
      if (!nullable) colDef += ", nullable = false";
      if (mapped.extraAnnotations) annotations.push(...mapped.extraAnnotations);
      annotations.push(`@Column(${colDef})`);

      return `  ${annotations.join("\n  ")}\n  private ${mapped.java} ${camel};`;
    });

    // Getters / setters
    const accessors = cols
      .map((c) => {
        const camel = snakeToCamel(c.column_name);
        const mapped = mapJavaType(c.data_type, c.udt_name);
        const pascal = camel[0].toUpperCase() + camel.slice(1);
        return [
          `  public ${mapped.java} get${pascal}() {`,
          `    return ${camel};`,
          `  }`,
          ``,
          `  public void set${pascal}(${mapped.java} ${camel}) {`,
          `    this.${camel} = ${camel};`,
          `  }`,
        ].join("\n");
      })
      .join("\n\n");

    const java = `package saas.identity.platform.entity.Generated;

${imports.map((i) => `import ${i};`).join("\n")}

/**
 * DB-First scaffold：${tbl.table_name}（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends ${className} 叠加在 src/main/java/.../entity/${className}.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts ${snakeToCamel(tbl.table_name)}。
 */
@Entity
@Table(name = "${tbl.table_name}")
public class ${className} {

${fields.join("\n\n")}

${accessors}
}
`;

    const outPath = resolve(OUTPUT_DIR, `${className}.java`);
    mkdirSync(OUTPUT_DIR, { recursive: true });
    writeFileSync(outPath, java);
    console.log(`[scaffold-entities]   → ${tbl.table_name} → entity/Generated/${className}.java (${cols.length} 字段)`);
  }

  await client.end();
  console.log(`[scaffold-entities] OK — ${filtered.length} entity 类已生成到 ${OUTPUT_DIR}`);
}

main().catch((err) => {
  console.error("[scaffold-entities] FATAL:", err.message);
  console.error(err.stack);
  process.exit(1);
});
