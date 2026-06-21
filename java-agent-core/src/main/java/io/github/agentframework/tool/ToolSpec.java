package io.github.agentframework.tool;

import java.util.Map;

/**
 * 工具的函数规约，描述工具的名称、描述和参数 JSON Schema，
 用于序列化成 LLM 可以理解的函数描述格式。
 */
public class ToolSpec {

    private final String name;
    private final String description;
    private final Map<String, Object> parameters;

    public ToolSpec(String name, String description, Map<String, Object> parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
