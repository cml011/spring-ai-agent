package io.github.agentframework.memory;

import io.github.agentframework.llm.ChatMessage;
import java.util.List;

/**
 * 对话记忆接口，管理 Agent 与用户的对话历史。
 */
public interface Memory {

    /**
     * 添加一条消息到对话历史。
     */
    void add(ChatMessage message);

    /**
     * 获取历史消息列表。
     */
    List<ChatMessage> getMessages();

    /**
     * 清空对话历史。
     */
    void clear();
}
