package io.github.agentframework.examples;

import io.github.agentframework.plugin.knowledge.Document;
import io.github.agentframework.plugin.knowledge.KnowledgeBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    @Autowired(required = false)
    private KnowledgeBase knowledgeBase;

    @PostMapping("/ingest")
    public String ingest(@RequestBody Map<String, String> body) {
        if (knowledgeBase == null) return "知识库未启用";
        String name = body.get("name");
        String content = body.get("content");
        if (name == null || content == null) return "请提供 name 和 content";
        knowledgeBase.ingest(new Document(name, "txt", content));
        return "已导入：" + name + "，当前共 " + knowledgeBase.getChunkCount() + " 个段落";
    }

    @GetMapping("/stats")
    public String stats() {
        if (knowledgeBase == null) return "知识库未启用";
        return "知识库共 " + knowledgeBase.getChunkCount() + " 个段落";
    }
}
