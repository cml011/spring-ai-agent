package io.github.agentframework.agent;

/**
 * Agent 接口。所有 Agent（包括 ReActAgent、OrchestratorAgent）都实现此接口。
 */
public interface Agent {

    /** 返回 Agent 的名称 */
    String getName();

    /** 返回 Agent 的描述，用于被其他 Agent 路由时参考 */
    String getDescription();

    /**
     * 执行一次对话。
     * @param context 执行上下文
     * @param userInput 用户输入
     * @return Agent 的回复
     */
    AgentReply execute(AgentContext context, String userInput);
}
