package io.github.agentframework.agent;

import io.github.agentframework.llm.ChatLLM;
import io.github.agentframework.llm.ChatMessage;
import io.github.agentframework.llm.Role;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.Tool;
import io.github.agentframework.tool.ToolRegistry;
import io.github.agentframework.tool.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct（Reasoning + Acting）模式的 Agent。
 *
 * 不预定义 workflow，每次通过与 LLM 的多轮交互动态决策：
 * Thought → Action → Observation → Thought → ... → Answer。
 */
public class ReActAgent implements Agent {

    private static final int MAX_ITERATIONS = 10;

    private static final Pattern THOUGHT_PATTERN = Pattern.compile(
            "Thought:\\s*(.*?)(?=Action:|Answer:|$)", Pattern.DOTALL);
    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "Action:\\s*(\\S+)", Pattern.DOTALL);
    private static final Pattern ACTION_INPUT_PATTERN = Pattern.compile(
            "Action Input:\\s*(.*?)(?=Observation:|Thought:|Answer:|$)", Pattern.DOTALL);
    private static final Pattern ANSWER_PATTERN = Pattern.compile(
            "Answer:\\s*(.*?)$", Pattern.DOTALL);

    private final String name;
    private final String description;
    private final String systemPrompt;
    private final ChatLLM llm;
    private final ToolRegistry toolRegistry;

    private Consumer<String> streamCallback;

    public ReActAgent(String name, String description, String systemPrompt,
                       ChatLLM llm, ToolRegistry toolRegistry) {
        this.name = name;
        this.description = description;
        this.systemPrompt = systemPrompt;
        this.llm = llm;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    public void setStreamCallback(Consumer<String> callback) {
        this.streamCallback = callback;
    }

    @Override
    public AgentReply execute(AgentContext context, String userInput) {
        String prompt = buildFullPrompt(userInput);
        context.getMemory().add(ChatMessage.user(userInput));

        List<ChatMessage> messages = buildMessages(context);
        messages.add(ChatMessage.user(userInput));
        messages.add(ChatMessage.system(prompt));

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            StringBuilder streamBuf = new StringBuilder();
            if (streamCallback != null) {
                llm.chatStream(messages, chunk -> {
                    streamBuf.append(chunk);
                    streamCallback.accept(chunk);
                });
            } else {
                streamBuf.append(llm.chat(messages));
            }
            String response = streamBuf.toString();

            // 检查是否有 Answer
            Matcher answerMatcher = ANSWER_PATTERN.matcher(response);
            if (answerMatcher.find()) {
                String answer = answerMatcher.group(1).trim();
                context.getMemory().add(ChatMessage.assistant(answer));
                return AgentReply.done(answer);
            }

            // 检查是否有 Action
            Matcher actionMatcher = ACTION_PATTERN.matcher(response);
            if (actionMatcher.find()) {
                String actionName = actionMatcher.group(1).trim();
                Tool tool = toolRegistry.get(actionName);
                if (tool == null) {
                    String error = "工具 '" + actionName + "' 不存在";
                    messages.add(ChatMessage.assistant(response));
                    messages.add(ChatMessage.system("Observation: " + error));
                    continue;
                }

                Matcher inputMatcher = ACTION_INPUT_PATTERN.matcher(response);
                String actionInput = inputMatcher.find() ? inputMatcher.group(1).trim() : "{}";

                ToolResult result = tool.execute(actionInput);

                messages.add(ChatMessage.assistant(response));
                messages.add(ChatMessage.system("Observation: " + result.getMessage()));
            } else {
                // LLM 没有输出 Action 也没有输出 Answer，把它的回复当普通回答
                context.getMemory().add(ChatMessage.assistant(response));
                return AgentReply.done(response);
            }
        }

        String fallback = "抱歉，我无法完成你的请求，请试试重新描述。";
        context.getMemory().add(ChatMessage.assistant(fallback));
        return AgentReply.done(fallback);
    }

    /**
     * 构建完整提示词：系统提示 + 工具描述 + ReAct 格式说明。
     */
    protected String buildFullPrompt(String userInput) {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt).append("\n\n");

        if (toolRegistry.size() > 0) {
            sb.append("你可以使用以下工具：\n\n");
            for (Tool tool : toolRegistry.getAll()) {
                sb.append("- ").append(tool.getName()).append(": ")
                  .append(tool.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("请按以下格式回复：\n\n");
        sb.append("Thought: 你当前的想法和推理\n");
        sb.append("Action: 工具名称\n");
        sb.append("Action Input: {\"参数名\": \"参数值\"}\n\n");
        sb.append("当你拿到所有需要的信息后，用以下格式回复：\n");
        sb.append("Thought: 我已有足够信息\n");
        sb.append("Answer: 你的最终回答\n\n");
        sb.append("注意：\n");
        sb.append("- 每次只能调用一个 Action\n");
        sb.append("- 等待 Observation 后再决定下一步\n");
        sb.append("- 如果缺少必要信息，请通过 Answer 向用户追问\n");

        return sb.toString();
    }

    /**
     * 构建发送给 LLM 的消息列表（含历史对话和系统提示）。
     */
    protected List<ChatMessage> buildMessages(AgentContext context) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        messages.add(ChatMessage.system(systemPrompt));
        messages.addAll(context.getMemory().getMessages());
        return messages;
    }
}
