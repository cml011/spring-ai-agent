package io.github.agentframework.llm;

import java.util.List;

/**
 * LLM 聊天接口抽象。所有底层模型（OpenAI、国产大模型、本地模型等）都实现此接口。
 */
public interface ChatLLM {

    /**
     * 发送消息列表并返回模型的文本回复。
     */
    String chat(List<ChatMessage> messages);

    /**
     * 支持流式输出的聊天接口，逐 chunk 回调 receiver。
     */
    void chatStream(List<ChatMessage> messages, java.util.function.Consumer<String> receiver);
}
