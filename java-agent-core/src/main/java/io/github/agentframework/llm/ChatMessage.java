package io.github.agentframework.llm;

import java.util.Objects;

public class ChatMessage {

    private final Role role;
    private final String content;

    public ChatMessage(Role role, String content) {
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(Role.SYSTEM, content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage(Role.USER, content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage(Role.ASSISTANT, content);
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return role + ": " + content;
    }
}
