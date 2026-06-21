package io.github.agentframework.agent;

import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolResult;
import io.github.agentframework.tool.ToolSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将子 Agent 包装为 Tool，供 OrchestratorAgent 调用实现动态委派。
 */
public class DelegationTool implements Tool {

    private final Agent targetAgent;

    public DelegationTool(Agent targetAgent) {
        this.targetAgent = targetAgent;
    }

    @Override
    public String getName() {
        return targetAgent.getName();
    }

    @Override
    public String getDescription() {
        return "将任务委托给 [" + targetAgent.getName() + "] 处理。"
             + targetAgent.getDescription();
    }

    @Override
    public ToolSpec getSpec() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        Map<String, Object> inputProp = new HashMap<String, Object>();
        inputProp.put("type", "string");
        inputProp.put("description", "要委托给该 Agent 的用户问题");
        properties.put("input", inputProp);

        Map<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("type", "object");
        params.put("properties", properties);
        params.put("required", new String[]{"input"});

        return new ToolSpec(getName(), getDescription(), params);
    }

    @Override
    public ToolResult execute(String argsJson) {
        // 简单解析：从 JSON 里提取 input 字段
        String input = argsJson;
        if (argsJson.contains("\"input\"")) {
            int start = argsJson.indexOf("\"input\"") + 8;
            start = argsJson.indexOf(':', start) + 1;
            start = argsJson.indexOf('"', start) + 1;
            int end = argsJson.indexOf('"', start);
            if (start > 0 && end > start) {
                input = argsJson.substring(start, end);
            }
        }

        AgentReply reply = targetAgent.execute(AgentContext.create(), input);
        return ToolResult.success(reply.getContent());
    }
}
