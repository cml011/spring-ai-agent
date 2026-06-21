package io.github.agentframework.plugin.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agentframework.agent.AgentContext;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolResult;
import io.github.agentframework.tool.ToolSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询工具。Agent 通过此工具执行 SELECT 查询。
 *
 * 安全措施：
 * - 只允许 SELECT 语句
 * - 自动限制最多返回 1000 行
 * - 结果转 JSON 返回给 LLM
 */
public class DatabaseQueryTool implements Tool {

    private static final Logger log = LoggerFactory.getLogger(DatabaseQueryTool.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int MAX_ROWS = 1000;

    private final DataSource dataSource;
    private final boolean tenantEnabled;

    public DatabaseQueryTool(DataSource dataSource) {
        this.dataSource = dataSource;
        this.tenantEnabled = false;
    }

    public DatabaseQueryTool(DataSource dataSource, boolean tenantEnabled) {
        this.dataSource = dataSource;
        this.tenantEnabled = tenantEnabled;
    }

    @Override
    public String getName() {
        return "queryDatabase";
    }

    @Override
    public String getDescription() {
        return "执行 SQL SELECT 查询并返回结果。需提供 sql 参数。仅支持 SELECT 语句。";
    }

    @Override
    public ToolSpec getSpec() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        Map<String, Object> sqlProp = new LinkedHashMap<String, Object>();
        sqlProp.put("type", "string");
        sqlProp.put("description", "要执行的 SQL SELECT 语句");
        properties.put("sql", sqlProp);

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("type", "object");
        params.put("properties", properties);
        params.put("required", new String[]{"sql"});

        return new ToolSpec(getName(), getDescription(), params);
    }

    @Override
    public ToolResult execute(String argsJson) {
        String tenantId = AgentContext.getCurrentTenantId();
        return executeSql(argsJson, tenantId);
    }

    private ToolResult executeSql(String argsJson, String tenantId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = MAPPER.readValue(argsJson, Map.class);
            String sql = (String) args.get("sql");

            if (sql == null || sql.trim().isEmpty()) {
                return ToolResult.error("缺少 sql 参数");
            }

            String trimmed = sql.trim().toUpperCase();
            if (!trimmed.startsWith("SELECT")) {
                return ToolResult.error("只允许执行 SELECT 查询");
            }

            if (tenantEnabled && tenantId != null && !tenantId.trim().isEmpty()) {
                if (!validateTenantId(tenantId)) {
                    return ToolResult.error("非法租户 ID");
                }
                sql = applyTenantFilter(sql.trim(), tenantId.trim());
                log.info("Tenant filter applied: tenant={}", tenantId);
            }

            String limitedSql = addRowLimit(sql);
            log.info("Executing query: {}", limitedSql);
            List<Map<String, Object>> rows = executeQuery(limitedSql);

            String jsonResult = MAPPER.writeValueAsString(rows);
            String message = "查询结果 (" + rows.size() + " 行)：\n" + jsonResult;
            return ToolResult.success(message);

        } catch (Exception e) {
            log.error("Query failed", e);
            return ToolResult.error("查询失败：" + e.getMessage());
        }
    }

    private List<Map<String, Object>> executeQuery(String sql) throws Exception {
        Connection conn = dataSource.getConnection();
        try {
            Statement stmt = conn.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(sql);
                try {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();
                    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

                    int count = 0;
                    while (rs.next() && count < MAX_ROWS) {
                        Map<String, Object> row = new LinkedHashMap<String, Object>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(meta.getColumnLabel(i), rs.getObject(i));
                        }
                        rows.add(row);
                        count++;
                    }

                    return rows;
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } finally {
            conn.close();
        }
    }

    private String addRowLimit(String sql) {
        String upper = sql.toUpperCase();
        if (upper.contains(" LIMIT ")) {
            return sql;
        }
        if (upper.contains(" LIMIT")) {
            return sql;
        }
        return sql + " LIMIT " + MAX_ROWS;
    }

    private String applyTenantFilter(String sql, String tenantId) {
        String trimmed = sql.trim();
        while (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        String[] clauses = { " ORDER BY ", " LIMIT ", " GROUP BY ", " HAVING " };
        int insertPos = trimmed.length();
        String upper = trimmed.toUpperCase();
        for (String clause : clauses) {
            int idx = upper.indexOf(clause);
            if (idx > 0 && idx < insertPos) {
                insertPos = idx;
            }
        }

        String prefix = trimmed.substring(0, insertPos);
        String suffix = trimmed.substring(insertPos);
        String filter = " tenant_id = '" + tenantId + "' ";

        if (prefix.toUpperCase().contains(" WHERE ")) {
            return prefix + " AND" + filter + suffix;
        } else {
            return prefix + " WHERE" + filter + suffix;
        }
    }

    private boolean validateTenantId(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return false;
        }
        for (int i = 0; i < tenantId.length(); i++) {
            char c = tenantId.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return false;
            }
        }
        return true;
    }
}
