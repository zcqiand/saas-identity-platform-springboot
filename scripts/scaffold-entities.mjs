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

// DATABASE_URL 必填。CLAUDE.md §2「禁止 env 默认值兜底」：连接串含 secret，
// 不允许在脚本里写字面量兜底；缺则 fail-fast 让调用者补 env / .env.local。
const DATABASE_URL = process.env.DATABASE_URL;
if (!DATABASE_URL) {
  console.error("[scaffold-entities] FATAL: DATABASE_URL 未设（CLAUDE.md §2 禁字面量兜底）");
  console.error("[scaffold-entities]        dev 加载 .env.local；prod 由 deploy 脚本注入 env-file。");
  console.error("[scaffold-entities]        fix: export DATABASE_URL='postgresql://postgres:***@host:5432/dbname'");
  process.exit(2);
}

const OUTPUT_DIR = resolve(
  ROOT,
  "src/main/java/saas/identity/platform/entity/Generated",
);

const EXCLUDE_TABLES = new Set([
  // Drizzle tracking — 不是业务表
  "__drizzle_migrations",
  // Drizzle 在 __drizzle_migrations 旁的 journal 表也不该有，跳过
  // Rails tracking（REQ-2026-001 期间发现，2026-10-02）：saas_dev 由本族 rails 栈共用，
  // ActiveRecord 会在库里建自己的 bookkeeping 表；与 drizzle tracking 同类——不是家族业务
  // schema（真源 = shared src/db/schema.ts），springboot 不应为其生成 entity。
  "schema_migrations",
  "ar_internal_metadata",
]);

// PG 类型 → Java 类型 + JdbcTypeCode 注解
// data_type 字段值：uuid / text / character varying / integer / bigint / boolean / jsonb / timestamp with time zone / ARRAY / USER-DEFINED
const PG_TYPE_MAP = {
  uuid: { java: "UUID", imports: ["java.util.UUID"] },
  text: { java: "String" },
  "character varying": { java: "String" },
  integer: { java: "Integer", box: true },
  smallint: { java: "Short", box: true },
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
  // 3a. 查 composite PK（PG information_schema.table_constraints）
  //  - table_name → array of PK column names (in order)
  //  - 单列 PK 不需要 IdClass，scaffold 沿用现有 @GeneratedValue UUID 模式
  //  - 2+ 列 PK 需要生成 {Table}Id @IdClass
  const { rows: pkRows } = await client.query(`
    SELECT tc.table_name, kcu.column_name, kcu.ordinal_position
    FROM information_schema.table_constraints tc
    JOIN information_schema.key_column_usage kcu
      ON tc.constraint_name = kcu.constraint_name
     AND tc.table_schema = kcu.table_schema
    WHERE tc.table_schema = 'public'
      AND tc.constraint_type = 'PRIMARY KEY'
    ORDER BY tc.table_name, kcu.ordinal_position
  `);
  const compositePKs = new Map(); // tableName -> [columnName, ...]
  const singlePKCol = new Map();  // tableName -> columnName
  const pkColsByTable = new Map();
  for (const r of pkRows) {
    if (!pkColsByTable.has(r.table_name)) pkColsByTable.set(r.table_name, []);
    pkColsByTable.get(r.table_name).push(r.column_name);
  }
  for (const [tableName, cols] of pkColsByTable) {
    if (cols.length >= 2) compositePKs.set(tableName, cols);
    else if (cols.length === 1) singlePKCol.set(tableName, cols[0]);
  }

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
    const pkCols = compositePKs.get(tbl.table_name) ?? null;
    const isCompositePK = pkCols != null;

    // 收集 imports
    const importsSet = new Set([
      "jakarta.persistence.Entity",
      "jakarta.persistence.Table",
      "jakarta.persistence.Id",
      "jakarta.persistence.Column",
    ]);
    if (!isCompositePK) {
      importsSet.add("jakarta.persistence.GeneratedValue");
      importsSet.add("jakarta.persistence.GenerationType");
    }
    if (isCompositePK) {
      importsSet.add("jakarta.persistence.IdClass");
    }
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
      const isPKCol = isCompositePK && pkCols.includes(c.column_name);

      let annotations = [];
      if (!isCompositePK && c.column_name === singlePKCol.get(tbl.table_name)) {
        annotations.push("@Id");
        annotations.push(
          '@GeneratedValue(strategy = GenerationType.UUID)',
        );
      } else if (isPKCol) {
        annotations.push("@Id");
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

    const idClassAnnotation = isCompositePK
      ? `\n@IdClass(${className}Id.class)`
      : "";
    const classHeader = isCompositePK
      ? ` * Composite PK：${pkCols.join(" + ")} → 单独 ${className}Id 类。`
      : "";

    const java = `package saas.identity.platform.entity.Generated;

${imports.map((i) => `import ${i};`).join("\n")}

/**
 * DB-First scaffold：${tbl.table_name}（ADR-0025）。
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 手写业务逻辑（@PreUpdate、@Convert、@EntityListeners 等）
 * 通过 extends ${className} 叠加在 src/main/java/.../entity/${className}.java。
 *
 * 字段含义见 saas-identity-platform-shared/src/db/schema.ts ${snakeToCamel(tbl.table_name)}。
${classHeader}
 */
@Entity
@Table(name = "${tbl.table_name}")${idClassAnnotation}
public class ${className} {

${fields.join("\n\n")}

${accessors}
}
`;

    const outPath = resolve(OUTPUT_DIR, `${className}.java`);
    mkdirSync(OUTPUT_DIR, { recursive: true });
    writeFileSync(outPath, java);
    console.log(
      `[scaffold-entities]   → ${tbl.table_name} → entity/Generated/${className}.java (${cols.length} 字段${isCompositePK ? ", composite PK " + pkCols.join("+") : ""})`,
    );

    // composite PK → 额外生成 <Table>Id.java
    if (isCompositePK) {
      emitIdClass(tbl.table_name, className, pkCols, cols);
    }
  }

  await client.end();
  console.log(`[scaffold-entities] OK — ${filtered.length} entity 类已生成到 ${OUTPUT_DIR}`);
}

/**
 * emitIdClass — composite PK 表生成 {Table}Id.java（@IdClass 配套类）。
 *
 * - 实现 Serializable（JPA 要求）
 * - 实现 equals + hashCode（基于所有 PK 字段）
 * - 包含默认构造器 + 全参构造器 + 各字段 getter/setter
 * - 包路径与 entity 同：saas.identity.platform.entity.Generated
 */
function emitIdClass(tableName, entityClassName, pkColNames, allCols) {
  const pkColInfos = pkColNames.map((colName) => {
    const col = allCols.find((c) => c.column_name === colName);
    return {
      colName,
      javaField: snakeToCamel(colName),
      javaType: mapJavaType(col.data_type, col.udt_name).java,
      import: mapJavaType(col.data_type, col.udt_name).imports?.[0],
    };
  });

  const imports = new Set([
    "java.io.Serializable",
    "java.util.Objects",
  ]);
  for (const info of pkColInfos) {
    if (info.import) imports.add(info.import);
  }
  const sortedImports = Array.from(imports).sort();

  const fields = pkColInfos
    .map((i) => `  private ${i.javaType} ${i.javaField};`)
    .join("\n");

  const ctor = pkColInfos
    .map((i) => `${i.javaType} ${i.javaField}`)
    .join(", ");

  const accessors = pkColInfos
    .map((i) => {
      const pascal = i.javaField[0].toUpperCase() + i.javaField.slice(1);
      return [
        `  public ${i.javaType} get${pascal}() {`,
        `    return ${i.javaField};`,
        `  }`,
        ``,
        `  public void set${pascal}(${i.javaType} ${i.javaField}) {`,
        `    this.${i.javaField} = ${i.javaField};`,
        `  }`,
      ].join("\n");
    })
    .join("\n\n");

  const equalsBody = pkColInfos
    .map((i) => `        Objects.equals(${i.javaField}, that.${i.javaField})`)
    .join(" &&\n");

  const hashBody = pkColInfos
    .map((i) => `${i.javaField}`)
    .join(", ");

  const java = `package saas.identity.platform.entity.Generated;

${sortedImports.map((i) => `import ${i};`).join("\n")}

/**
 * DB-First scaffold：${tableName} composite PK（ADR-0025）。
 *
 * 由 scripts/scaffold-entities.mjs 从 saas_dev 真库反推生成。
 * 配套 @IdClass(${entityClassName}Id.class) 用在 ${entityClassName}。
 */
public class ${entityClassName}Id implements Serializable {

${fields}

  public ${entityClassName}Id() {}

  public ${entityClassName}Id(${ctor}) {
${pkColInfos
      .map((i) => `    this.${i.javaField} = ${i.javaField};`)
      .join("\n")}
  }

${accessors}

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ${entityClassName}Id)) return false;
    ${entityClassName}Id that = (${entityClassName}Id) o;
    return ${equalsBody};
  }

  @Override
  public int hashCode() {
    return Objects.hash(${hashBody});
  }
}
`;

  const outPath = resolve(OUTPUT_DIR, `${entityClassName}Id.java`);
  writeFileSync(outPath, java);
  console.log(
    `[scaffold-entities]   → ${tableName} → entity/Generated/${entityClassName}Id.java (PK class for ${pkColNames.join("+")})`,
  );
}

main().catch((err) => {
  console.error("[scaffold-entities] FATAL:", err.message);
  console.error(err.stack);
  process.exit(1);
});
