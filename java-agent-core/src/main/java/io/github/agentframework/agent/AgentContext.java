package io.github.agentframework.agent;

import io.github.agentframework.memory.DefaultMemory;
import io.github.agentframework.memory.Memory;

/**
 * Agent 执行上下文，持有对话记忆等状态。
 * 每次用户对话可以创建一个新的 Context，也可以复用已有 Context 实现多轮对话。
 */
public class AgentContext {

    private final Memory memory;
    private String tenantId;

    private static final ThreadLocal<String> TENANT_HOLDER = new ThreadLocal<String>();

    public AgentContext() {
        this.memory = new DefaultMemory();
    }

    public AgentContext(Memory memory) {
        this.memory = memory;
    }

    public static AgentContext create() {
        return new AgentContext();
    }

    public static AgentContext create(Memory memory) {
        return new AgentContext(memory);
    }

    public Memory getMemory() {
        return memory;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
        TENANT_HOLDER.set(tenantId);
    }

    public static String getCurrentTenantId() {
        return TENANT_HOLDER.get();
    }

    public static void clearTenantId() {
        TENANT_HOLDER.remove();
    }
}
