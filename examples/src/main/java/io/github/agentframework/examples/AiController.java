package io.github.agentframework.examples;

import io.github.agentframework.agent.Agent;
import io.github.agentframework.agent.AgentContext;
import io.github.agentframework.agent.AgentReply;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private Agent agent;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostMapping("/chat")
    public AgentReply chat(@RequestBody Map<String, String> body,
                            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return AgentReply.done("请说点什么吧。");
        }
        AgentContext context = AgentContext.create();
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            context.setTenantId(tenantId.trim());
        }
        return agent.execute(context, message);
    }

    @GetMapping("/chat")
    public String help() {
        return "请使用 POST 方式发送对话。示例：\n"
             + "curl -X POST http://localhost:8080/ai/chat \\n"
             + "  -H \"Content-Type: application/json\" \\n"
             + "  -H \"X-Tenant-Id: tenant_001\" \\n"
             + "  -d '{\"message\":\"帮我统计今年的销售额\"}'";
    }

    @GetMapping("/chat/sse")
    public SseEmitter chatSse(@RequestParam String message,
                               @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId) {
        SseEmitter emitter = new SseEmitter(120000L);
        executor.execute(() -> {
            try {
                AgentContext context = AgentContext.create();
                if (tenantId != null && !tenantId.trim().isEmpty()) {
                    context.setTenantId(tenantId.trim());
                }
                if (agent instanceof io.github.agentframework.agent.ReActAgent) {
                    ((io.github.agentframework.agent.ReActAgent) agent).setStreamCallback(token -> {
                        try { emitter.send(SseEmitter.event().data(token)); } catch (Exception ignored) {}
                    });
                }
                AgentReply reply = agent.execute(context, message);
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            }
        });
        return emitter;
    }
}
