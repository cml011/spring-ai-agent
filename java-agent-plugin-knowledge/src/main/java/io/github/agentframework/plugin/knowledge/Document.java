package io.github.agentframework.plugin.knowledge;

import java.util.UUID;

/**
 * 知识库文档。可从文本、文件路径等创建。
 */
public class Document {

    private final String id;
    private final String name;
    private final String type;
    private final String content;

    public Document(String name, String type, String content) {
        this.id = UUID.randomUUID().toString().replace("-", "");
        this.name = name;
        this.type = type;
        this.content = content;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getContent() { return content; }
}
