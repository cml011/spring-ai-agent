package io.github.agentframework.plugin.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

/**
 * 从 JDBC 元数据读取数据库表结构，生成 LLM 友好的描述文本。
 *
 * 支持两种模式：
 * - 白名单模式：只暴露 includeTables 指定的表
 * - 全量发现模式：autoDiscover=true 时暴露所有表
 */
public class SchemaInspector {

    private final DataSource dataSource;
    private final Set<String> includeTables;
    private final boolean autoDiscover;
    private final String dialect;

    public SchemaInspector(DataSource dataSource) {
        this(dataSource, null, false, "h2");
    }

    public SchemaInspector(DataSource dataSource, List<String> includeTables, boolean autoDiscover) {
        this(dataSource, includeTables, autoDiscover, "h2");
    }

    public SchemaInspector(DataSource dataSource, List<String> includeTables, boolean autoDiscover, String dialect) {
        this.dataSource = dataSource;
        this.includeTables = includeTables != null
                ? new LinkedHashSet<String>(includeTables)
                : null;
        this.autoDiscover = autoDiscover;
        this.dialect = (dialect != null) ? dialect.toLowerCase() : "h2";
    }

    /**
     * 生成表结构描述文本，供 LLM 理解数据库结构。
     */
    public String inspect() {
        try {
            Connection conn = dataSource.getConnection();
            try {
                DatabaseMetaData meta = conn.getMetaData();
                StringBuilder sb = new StringBuilder();
                sb.append("以下是你可以查询的数据库表结构：\n\n");

                List<String> tables = resolveTables(meta, conn);
                if (tables.isEmpty()) {
                    sb.append("（当前没有可用的表）\n\n");
                    sb.append("注意：如果用户问的是数据库相关的问题，请告知用户暂无可用数据表。\n");
                    return sb.toString();
                }

                for (String tableName : tables) {
                    appendTableDescription(sb, meta, conn.getCatalog(), tableName);
                }

                sb.append("\n注意：只能执行 SELECT 查询，不能修改数据。");
                sb.append("查询结果最多返回 1000 行。\n");
                sb.append("\n").append("当前数据库方言：").append(dialect.toUpperCase()).append("\n");
                if ("mysql".equals(dialect)) {
                    sb.append("MySQL SQL 语法提示：\n");
                    sb.append("- 日期用 'YYYY-MM-DD' 格式字符串\n");
                    sb.append("- YEAR(date) 提取年份，MONTH(date) 提取月份\n");
                    sb.append("- CURDATE() 取当前日期，NOW() 取当前时间\n");
                    sb.append("- DATE_FORMAT(date, '%Y-%m-%d') 格式化日期\n");
                } else {
                    sb.append("H2 SQL 语法提示：\n");
                    sb.append("- 日期用 'YYYY-MM-DD' 格式字符串，不要用时间戳\n");
                    sb.append("- YEAR(date) 提取年份，MONTH(date) 提取月份\n");
                    sb.append("- CURRENT_DATE 取当前日期\n");
                    sb.append("- 不要使用 strftime、datetime 等函数\n");
                }

                return sb.toString();
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            return "(无法读取数据库结构：" + e.getMessage() + ")\n";
        }
    }

    private List<String> resolveTables(DatabaseMetaData meta, Connection conn) throws Exception {
        if (includeTables != null && !includeTables.isEmpty()) {
            return new ArrayList<String>(includeTables);
        }
        if (autoDiscover) {
            List<String> tables = new ArrayList<String>();
            ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"});
            try {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            } finally {
                rs.close();
            }
            return tables;
        }
        return Collections.emptyList();
    }

    private void appendTableDescription(
            StringBuilder sb, DatabaseMetaData meta, String catalog, String tableName)
            throws Exception {
        sb.append("Table: ").append(tableName).append("\n");

        ResultSet columns = meta.getColumns(catalog, null, tableName, "%");
        try {
            while (columns.next()) {
                String colName = columns.getString("COLUMN_NAME");
                String colType = columns.getString("TYPE_NAME");
                int colSize = columns.getInt("COLUMN_SIZE");
                String nullable = "YES".equals(columns.getString("IS_NULLABLE"))
                        ? "nullable" : "not null";
                String remarks = columns.getString("REMARKS");

                sb.append("  - ").append(colName)
                  .append(": ").append(colType).append("(").append(colSize).append(")")
                  .append(" [").append(nullable).append("]");
                if (remarks != null && !remarks.isEmpty()) {
                    sb.append(" (").append(remarks).append(")");
                }
                if ("DATE".equalsIgnoreCase(colType) || "DATETIME".equalsIgnoreCase(colType)
                        || "TIMESTAMP".equalsIgnoreCase(colType)) {
                    sb.append(" [用法: 'YYYY-MM-DD' 格式字符串]");
                }
                sb.append("\n");
            }
        } finally {
            columns.close();
        }
        sb.append("\n");
    }
}
