package io.github.agentframework.agent;

/**
 * Agent 的回复结果。
 */
public class AgentReply {

    private final String content;
    private final boolean finished;

    public AgentReply(String content, boolean finished) {
        this.content = content;
        this.finished = finished;
    }

    public static AgentReply done(String content) {
        return new AgentReply(content, true);
    }

    public static AgentReply pending(String content) {
        return new AgentReply(content, false);
    }

    public String getContent() {
        return content;
    }

    public boolean isFinished() {
        return finished;
    }
}
