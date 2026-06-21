package io.github.agentframework.agent;

/**
 * 系统提示词贡献者接口。插件（如 DatabasePlugin）通过实现此接口
 * 向 Agent 的 system prompt 追加内容（如表结构描述）。
 */
public interface SystemPromptContributor {

    /**
     * 返回要追加到系统提示词中的内容。
     */
    String contribute();
}
