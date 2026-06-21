package io.github.agentframework.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具注册中心，管理所有 Agent 可用的 Tool。
 */
public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<String, Tool>();

    public ToolRegistry() {
    }

    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public void registerAll(Collection<? extends Tool> tools) {
        for (Tool tool : tools) {
            register(tool);
        }
    }

    public Tool get(String name) {
        return tools.get(name);
    }

    public Collection<Tool> getAll() {
        return tools.values();
    }

    public boolean contains(String name) {
        return tools.containsKey(name);
    }

    public int size() {
        return tools.size();
    }
}
