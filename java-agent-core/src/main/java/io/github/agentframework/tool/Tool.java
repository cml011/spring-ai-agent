package io.github.agentframework.tool;

/**
 * Agent 可调用的工具。每个 Tool 有名称、描述和参数规约，
 * LLM 根据这些信息决定何时调用哪个 Tool。
 */
public interface Tool {

    /** 工具名称，LLM 通过此名称引用该工具 */
    String getName();

    /** 工具描述，LLM 通过此描述理解工具的功能 */
    String getDescription();

    /** 工具的 JSON Schema 规约，描述参数结构 */
    ToolSpec getSpec();

    /**
     * 执行工具调用。
     * @param argsJson 参数的 JSON 字符串
     * @return 工具执行结果
     */
    ToolResult execute(String argsJson);
}
