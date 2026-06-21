package io.github.agentframework.plugin.knowledge.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.agentframework.plugin.knowledge.Document;
import io.github.agentframework.plugin.knowledge.KnowledgeBase;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolResult;
import io.github.agentframework.tool.ToolSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public class KnowledgeIngestTool implements Tool {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final KnowledgeBase knowledgeBase;

    public KnowledgeIngestTool(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    public String getName() { return "ingestDocument"; }

    @Override
    public String getDescription() { return "将文档导入知识库。参数：name(文档名称), content(文档内容)"; }

    @Override
    public ToolSpec getSpec() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        Map<String, Object> n = new LinkedHashMap<String, Object>();
        n.put("type", "string"); n.put("description", "文档名称");
        properties.put("name", n);
        Map<String, Object> c = new LinkedHashMap<String, Object>();
        c.put("type", "string"); c.put("description", "文档内容");
        properties.put("content", c);
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("type", "object");
        params.put("properties", properties);
        params.put("required", new String[]{"name", "content"});
        return new ToolSpec(getName(), getDescription(), params);
    }

    @Override
    public ToolResult execute(String argsJson) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> args = MAPPER.readValue(argsJson, Map.class);
            String name = (String) args.get("name");
            String content = (String) args.get("content");
            if (name == null || name.trim().isEmpty()) return ToolResult.error("缺少 name 参数");
            if (content == null || content.trim().isEmpty()) return ToolResult.error("缺少 content 参数");
            Document doc = new Document(name.trim(), "txt", content.trim());
            knowledgeBase.ingest(doc);
            return ToolResult.success("文档 '" + name + "' 已导入，当前共 " + knowledgeBase.getChunkCount() + " 个段落。");
        } catch (Exception e) {
            return ToolResult.error("导入失败：" + e.getMessage());
        }
    }
}
