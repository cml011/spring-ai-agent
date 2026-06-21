package io.github.agentframework.plugin.knowledge.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agentframework.plugin.knowledge.KnowledgeBase;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolResult;
import io.github.agentframework.tool.ToolSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识库检索工具。Agent 通过此工具搜索知识库获取上下文。
 */
public class KnowledgeSearchTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KnowledgeBase knowledgeBase;

    public KnowledgeSearchTool(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    public String getName() {
        return "searchKnowledgeBase";
    }

    @Override
    public String getDescription() {
        return "搜索知识库，获取与问题相关的文档段落。参数：query(搜索关键词)";
    }

    @Override
    public ToolSpec getSpec() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        Map<String, Object> queryProp = new LinkedHashMap<String, Object>();
        queryProp.put("type", "string");
        queryProp.put("description", "搜索关键词");
        properties.put("query", queryProp);

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("type", "object");
        params.put("properties", properties);
        params.put("required", new String[]{"query"});

        return new ToolSpec(getName(), getDescription(), params);
    }

    @Override
    public ToolResult execute(String argsJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = MAPPER.readValue(argsJson, Map.class);
            String query = (String) args.get("query");

            if (query == null || query.trim().isEmpty()) {
                return ToolResult.error("缺少 query 参数");
            }

            String result = knowledgeBase.search(query, 5);
            return ToolResult.success(result);

        } catch (Exception e) {
            return ToolResult.error("知识库查询失败：" + e.getMessage());
        }
    }
}
