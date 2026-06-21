package io.github.agentframework.memory;

import io.github.agentframework.llm.ChatMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultMemory implements Memory {

    private final List<ChatMessage> messages = new ArrayList<ChatMessage>();

    @Override
    public void add(ChatMessage message) {
        messages.add(message);
    }

    @Override
    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public void clear() {
        messages.clear();
    }
}
