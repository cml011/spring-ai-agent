package io.github.agentframework.agent;

import io.github.agentframework.llm.ChatLLM;
import io.github.agentframework.tool.ToolRegistry;

/**
 * 编排型 Agent：本身是一个 ReActAgent，同时拥有子 Agent 的委派能力。
 *
 * 子 Agent 通过 DelegationTool 包装后注册到 ToolRegistry，
 * LLM 根据用户问题动态决定交给哪个子 Agent 处理。
 */
public class OrchestratorAgent extends ReActAgent {

    public OrchestratorAgent(String name, String description, String systemPrompt,
                              ChatLLM llm, ToolRegistry toolRegistry,
                              java.util.List<? extends Agent> subAgents) {
        super(name, description, buildOrchestratorPrompt(systemPrompt, subAgents),
              llm, buildOrchestratorTools(toolRegistry, subAgents));
    }

    private static String buildOrchestratorPrompt(
            String basePrompt, java.util.List<? extends Agent> subAgents) {
        StringBuilder sb = new StringBuilder();
        sb.append(basePrompt).append("\n\n");
        sb.append("你是一个编排型助手，你可以自己回答问题，");
        sb.append("也可以将任务委托给以下专业 Agent：\n\n");

        for (Agent agent : subAgents) {
            sb.append("- ").append(agent.getName()).append(": ")
              .append(agent.getDescription()).append("\n");
        }

        sb.append("\n如果用户的问题需要调用专业 Agent，请使用对应的工具名进行委托。");
        sb.append("如果自己可以回答，直接回答。可以同时或依次委托多个 Agent。");

        return sb.toString();
    }

    private static ToolRegistry buildOrchestratorTools(
            ToolRegistry baseTools, java.util.List<? extends Agent> subAgents) {
        ToolRegistry registry = new ToolRegistry();
        if (baseTools != null) {
            for (io.github.agentframework.tool.Tool tool : baseTools.getAll()) {
                registry.register(tool);
            }
        }
        for (Agent agent : subAgents) {
            registry.register(new DelegationTool(agent));
        }
        return registry;
    }
}
